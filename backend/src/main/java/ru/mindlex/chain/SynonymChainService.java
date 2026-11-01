package ru.mindlex.chain;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mindlex.common.BusinessException;
import ru.mindlex.common.ConflictException;
import ru.mindlex.common.NotFoundException;
import ru.mindlex.common.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * CRUD над цепочками синонимов вместе с шагами.
 * Цепочки — общий контент приложения, поэтому проверок владельца здесь нет:
 * редактировать справочник может любой аутентифицированный пользователь.
 */
@Service
public class SynonymChainService {

    private final SynonymChainRepository synonymChainRepository;
    private final SynonymChainStepRepository synonymChainStepRepository;

    public SynonymChainService(SynonymChainRepository synonymChainRepository,
                               SynonymChainStepRepository synonymChainStepRepository) {
        this.synonymChainRepository = synonymChainRepository;
        this.synonymChainStepRepository = synonymChainStepRepository;
    }

    /** Список цепочек: при заданном языке — только этого языка, иначе все, по алфавиту. */
    @Transactional(readOnly = true)
    public List<SynonymChainResponse> list(String language) {
        List<SynonymChain> chains = language == null || language.isBlank()
                ? synonymChainRepository.findAll(Sort.by(Sort.Direction.ASC, "title"))
                : synonymChainRepository.findByLanguageOrderByTitleAsc(language.trim().toLowerCase(Locale.ROOT));
        return chains.stream()
                .map(chain -> SynonymChainResponse.from(chain, stepsOf(chain.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SynonymChainResponse get(UUID chainId) {
        SynonymChain chain = require(chainId);
        return SynonymChainResponse.from(chain, stepsOf(chainId));
    }

    @Transactional
    public SynonymChainResponse create(SynonymChainRequest request) {
        SynonymChain chain = new SynonymChain();
        apply(chain, request);
        SynonymChain saved = synonymChainRepository.save(chain);
        List<SynonymChainStep> steps = synonymChainStepRepository.saveAll(buildSteps(saved.getId(), request.steps()));
        return SynonymChainResponse.from(saved, steps);
    }

    /**
     * Обновляет поля цепочки и полностью заменяет её шаги.
     * Старые шаги удаляются и сбрасываются на диск до вставки новых,
     * иначе уникальное ограничение {@code uq_chain_step} сработает на паре (chain_id, step_number).
     */
    @Transactional
    public SynonymChainResponse update(UUID chainId, SynonymChainRequest request) {
        SynonymChain chain = require(chainId);
        apply(chain, request);
        SynonymChain saved = synonymChainRepository.save(chain);
        List<SynonymChainStep> steps = buildSteps(chainId, request.steps());
        synonymChainStepRepository.deleteAll(stepsOf(chainId));
        synonymChainStepRepository.flush();
        List<SynonymChainStep> savedSteps = synonymChainStepRepository.saveAll(steps);
        return SynonymChainResponse.from(saved, savedSteps);
    }

    @Transactional
    public void delete(UUID chainId) {
        SynonymChain chain = require(chainId);
        synonymChainStepRepository.deleteAll(stepsOf(chainId));
        synonymChainStepRepository.flush();
        synonymChainRepository.delete(chain);
    }

    @Transactional(readOnly = true)
    public SynonymChain require(UUID chainId) {
        return synonymChainRepository.findById(chainId)
                .orElseThrow(() -> NotFoundException.of("Цепочка синонимов", chainId));
    }

    private List<SynonymChainStep> stepsOf(UUID chainId) {
        return synonymChainStepRepository.findByChainIdOrderByStepNumberAsc(chainId);
    }

    /**
     * Готовит шаги к сохранению: сортирует по номеру, запрещает повторяющиеся номера
     * и пустой список синонимов.
     */
    private List<SynonymChainStep> buildSteps(UUID chainId, List<SynonymChainStepRequest> stepRequests) {
        if (stepRequests == null || stepRequests.isEmpty()) {
            throw new BusinessException("Цепочка должна содержать хотя бы один шаг");
        }
        List<SynonymChainStepRequest> ordered = new ArrayList<>(stepRequests);
        ordered.sort(Comparator.comparingInt(SynonymChainStepRequest::stepNumber));
        Set<Integer> usedNumbers = new HashSet<>();
        List<SynonymChainStep> steps = new ArrayList<>(ordered.size());
        for (SynonymChainStepRequest stepRequest : ordered) {
            if (!usedNumbers.add(stepRequest.stepNumber())) {
                throw new ConflictException("Номер шага " + stepRequest.stepNumber() + " повторяется в цепочке");
            }
            steps.add(buildStep(chainId, stepRequest));
        }
        return steps;
    }

    private SynonymChainStep buildStep(UUID chainId, SynonymChainStepRequest request) {
        String promptWord = request.promptWord() == null ? "" : request.promptWord().trim();
        if (promptWord.isEmpty()) {
            throw new BusinessException("Укажите слово-подсказку для шага " + request.stepNumber());
        }
        String acceptedSynonyms = TextUtils.joinCsv(request.acceptedSynonyms());
        if (acceptedSynonyms == null) {
            throw new BusinessException("Укажите хотя бы один синоним для шага " + request.stepNumber());
        }
        SynonymChainStep step = new SynonymChainStep();
        step.setChainId(chainId);
        step.setStepNumber(request.stepNumber());
        step.setPromptWord(promptWord);
        step.setAcceptedSynonyms(acceptedSynonyms);
        step.setHint(request.hint());
        return step;
    }

    private void apply(SynonymChain chain, SynonymChainRequest request) {
        chain.setTitle(request.title().trim());
        chain.setLanguage(request.language() == null || request.language().isBlank()
                ? "en"
                : request.language().trim().toLowerCase(Locale.ROOT));
        chain.setCategory(request.category());
    }
}
