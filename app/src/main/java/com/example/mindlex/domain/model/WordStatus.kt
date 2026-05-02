package com.example.mindlex.domain.model

enum class WordStatus(val level: Int) {
    NEW(0),
    LEARNING(1),
    KNOWN(2),
    REVIEW(3)
}
