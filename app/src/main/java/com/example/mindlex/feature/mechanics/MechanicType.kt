package com.example.mindlex.feature.mechanics

/** Режимы обучения на экране выбора механик. */
enum class MechanicType {
    ACTIVE_RECALL,
    CLOZE,
    RUSH,
    SYNONYM_CHAIN
}

enum class MechanicStatus {
    AVAILABLE,
    LOCKED
}
