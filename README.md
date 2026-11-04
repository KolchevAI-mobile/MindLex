# MindLex

**Приложение для изучения иностранной лексики с игровыми механиками.**
Android-клиент на Kotlin + Jetpack Compose и собственный REST-бэкенд на Java (Spring Boot) с PostgreSQL.

> Подробное описание доменной области, сущностей, архитектуры и соответствия методологии
> 12-факторных приложений — в файле [Отчёт.md](Отчёт.md).

---

## Что умеет приложение

Четыре игровые механики, вся логика которых выполняется на сервере:

| Механика | Суть | Особенности |
|---|---|---|
| **Active Recall** | Перевод слова с родного языка на изучаемый | Подсказка, серия правильных ответов, очки |
| **Contextual Cloze** | Заполнение пропуска в предложении | Таймер 30–45 секунд на задание |
| **Timed Rush** | Перевод на скорость | Раунд 60–90 секунд, комбо ×1.5 / ×2 / ×3, бонус за оставшееся время |
| **Synonym Chain** | Построение цепочки синонимов | Пошаговое прохождение, подсказки из синонимов |

Дополнительно: интервальное повторение (SM-2), серия занятий (стрик), дневная цель,
личные рекорды, CRUD колод и слов, настройки тренировок.

---

## Стек

**Бэкенд:** Java 17 · Spring Boot 3.5 · Spring Security + JWT · Spring Data JPA · PostgreSQL 16 ·
Flyway · HikariCP · springdoc-openapi · Maven Wrapper · Docker

**Android:** Kotlin 2.1 · Jetpack Compose (Material 3) · MVVM + StateFlow · Hilt · Retrofit + OkHttp ·
kotlinx.serialization · DataStore · Navigation Compose · minSdk 26 / targetSdk 36

---

## Быстрый старт

### Вариант 1: всё в Docker

```bash
cp .env.example .env      # измените MINDLEX_JWT_SECRET
docker compose up --build
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Состояние: <http://localhost:8080/actuator/health>

### Вариант 2: бэкенд локально, PostgreSQL в Docker

```bash
docker compose up -d db
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

### Android-клиент

```bash
cd android
./gradlew assembleDebug          # Windows: gradlew.bat assembleDebug
```

По умолчанию клиент обращается к `http://10.0.2.2:8080/` (это хост-машина при запуске из эмулятора).
Для реального устройства укажите адрес при сборке:

```bash
./gradlew assembleDebug -Pmindlex.apiUrl=http://192.168.1.10:8080/
```

Установка на подключённое устройство или эмулятор:

```bash
./gradlew installDebug
```

---

## Демонстрационный доступ

При первом запуске автоматически создаётся пользователь и системная колода:

- **e-mail:** `demo@mindlex.local`
- **пароль:** `demo1234`
- **колода:** «Английский: базовый словарь» — 40 слов, 24 упражнения cloze, 4 цепочки синонимов

Отключается переменной `MINDLEX_DEMO_USER_ENABLED=false`.

---

## Тесты

```bash
cd backend
./mvnw test
```

53 теста, включая интеграционные проверки REST-API на настоящем PostgreSQL, который поднимается
внутри тестов (`embedded-postgres`) — Docker для тестов не нужен.

---

## Структура репозитория

```
mindlex/
├── Отчёт.md                 отчёт по заданию (домен, стек, сущности, 12 факторов)
├── docker-compose.yml       PostgreSQL + API
├── .env.example             переменные окружения
├── backend/                 Java-бэкенд (Spring Boot, Maven Wrapper, Dockerfile)
└── android/                 Android-клиент (Kotlin, Compose)
```

---

## API (кратко)

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/v1/auth/register` · `/login` | регистрация и вход (JWT) |
| GET | `/api/v1/dashboard` | сводка главного экрана |
| GET/POST/PUT/DELETE | `/api/v1/decks` … | CRUD колод |
| GET/POST/PUT/DELETE | `/api/v1/decks/{id}/words`, `/api/v1/words/{id}` | CRUD слов |
| GET/POST/PUT/DELETE | `/api/v1/cloze-exercises` … | CRUD упражнений с пропусками |
| GET/POST/PUT/DELETE | `/api/v1/synonym-chains` … | CRUD цепочек синонимов |
| GET/POST/PUT/DELETE | `/api/v1/reminders` … | CRUD напоминаний |
| GET/DELETE | `/api/v1/progress` … | прогресс изучения |
| POST | `/api/v1/sessions` … | игровые сессии и ответы |
| GET/PUT | `/api/v1/settings` | настройки тренировок |

Полное описание — в Swagger UI после запуска бэкенда.

---

## Конфигурация

Все параметры задаются переменными окружения (см. `.env.example`):

| Переменная | По умолчанию | Назначение |
|---|---|---|
| `MINDLEX_DB_URL` | `jdbc:postgresql://localhost:5432/mindlex` | Подключение к PostgreSQL |
| `MINDLEX_DB_USER` / `MINDLEX_DB_PASSWORD` | `mindlex` / `mindlex` | Учётные данные БД |
| `MINDLEX_JWT_SECRET` | значение для разработки | Секрет подписи JWT (не короче 32 байт) |
| `MINDLEX_JWT_TTL` | `PT12H` | Время жизни токена |
| `MINDLEX_CORS_ORIGINS` | `http://localhost:3000,…` | Разрешённые источники |
| `PORT` | `8080` | HTTP-порт |
| `MINDLEX_LOG_LEVEL` | `INFO` | Уровень логирования |
| `MINDLEX_DEMO_USER_ENABLED` | `true` | Создавать демо-пользователя |
