package ru.mindlex.chain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SynonymChainStepRepository extends JpaRepository<SynonymChainStep, UUID> {

    List<SynonymChainStep> findByChainIdOrderByStepNumberAsc(UUID chainId);

    Optional<SynonymChainStep> findByChainIdAndStepNumber(UUID chainId, int stepNumber);

    long countByChainId(UUID chainId);

    /** Случайная цепочка, у которой есть хотя бы один шаг. */
    @Query(value = "select chain_id from synonym_chain_steps group by chain_id order by random() limit 1", nativeQuery = true)
    UUID findRandomChainId();

    @Query("select s from SynonymChainStep s where s.chainId = :chainId and s.stepNumber > :stepNumber order by s.stepNumber asc")
    List<SynonymChainStep> findNextSteps(@Param("chainId") UUID chainId, @Param("stepNumber") int stepNumber);

    @Query("select min(s.stepNumber) from SynonymChainStep s where s.chainId = :chainId")
    Integer findFirstStepNumber(@Param("chainId") UUID chainId);
}
