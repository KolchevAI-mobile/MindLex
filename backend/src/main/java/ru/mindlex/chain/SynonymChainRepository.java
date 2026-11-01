package ru.mindlex.chain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SynonymChainRepository extends JpaRepository<SynonymChain, UUID> {

    List<SynonymChain> findByLanguageOrderByTitleAsc(String language);
}
