package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.WordRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAllWords @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(): List<Word> = wordRepository.getAllWords()
}
