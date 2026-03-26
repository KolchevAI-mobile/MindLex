# MindLex 

**Intelligent Vocabulary Learning App** — приложение для изучения иностранной лексики с использованием геймификации.

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-blue.svg)](https://developer.android.com/jetpack/compose)

---

## Описание

MindLex — Android-приложение для эффективного изучения словарного запаса (английский, немецкий, французский, испанский). Использует 4 механики обучения, основанные на научных исследованиях.

---

## 4 Механики Обучения

| Механика | Описание | Для чего |
|----------|----------|----------|
| **Active Recall** | Перевод слов с родного на иностранный | Базовое запоминание |
| **Contextual Cloze** | Заполнение пропусков в предложениях | Контекстное понимание |
| **Timed Rush** | Перевод на скорость за 90 секунд | Беглость и автоматизм |
| **Synonym Chain** | Построение цепочек синонимов | Семантические связи |

---

## Архитектура

**Clean Architecture + MVVM**

Presentation (Compose + ViewModel)
↓
Domain (Use Cases + Models)
↓
Data (Room + Supabase)


**Tech Stack:**
- Jetpack Compose, Material Design 3
- Hilt (DI), Coroutines, Flow
- Room (локальный кэш)
- Supabase (облачная БД)

---

## Структура Проекта

<img width="618" height="211" alt="image" src="https://github.com/user-attachments/assets/d875cfe0-0a89-4aa5-86c4-eab6f311ec64" />

---

## База Данных

**Таблицы:**
- `words` — основные слова
- `cloze_exercises` — контекстные упражнения
- `synonym_chains` — цепочки синонимов
- `word_progress` — прогресс изучения

---

## Запуск

1. Клонировать репозиторий
2. Открыть в Android Studio
3. Собрать и запустить

```bash
git clone https://github.com/YOUR_USERNAME/mindlex.git
./gradlew assembleDebug
```

## ВКР
Разработано в рамках выпускной квалификационной работы:
"Разработка мобильного приложения для изучения иностранной лексики с геймификацией и персонализирванным графиком"
