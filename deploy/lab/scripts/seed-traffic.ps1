# ============================================================================
#  Генерация трафика MindLex: наполняет журнал записями всех уровней.
#
#  Зачем: чтобы LogsQL-запросы из отчёта выполнялись на осмысленных данных —
#  с успешными запросами, отказами в доступе, ошибками валидации и конфликтами.
#
#  Использование:
#     pwsh -File scripts/seed-traffic.ps1 [-ApiBase http://127.0.0.1:18080]
# ============================================================================
param(
    [string]$ApiBase = "http://127.0.0.1:18080",
    [int]$WaitSeconds = 90
)

$ErrorActionPreference = 'Continue'
$api = "$ApiBase/api/v1"
$json = @{ 'Content-Type' = 'application/json' }

function Wait-Api {
    param([int]$Timeout)
    $deadline = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Uri "$ApiBase/actuator/health" -TimeoutSec 5
            if ($health.status -eq 'UP') { return $true }
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    return $false
}

Write-Host "Ожидание готовности API на $ApiBase ..."
if (-not (Wait-Api -Timeout $WaitSeconds)) {
    Write-Error "API не поднялся за $WaitSeconds секунд"
    exit 1
}
Write-Host "API готов."

# --- 1. Успешный вход ------------------------------------------------------
$login = Invoke-RestMethod -Uri "$api/auth/login" -Method Post -ContentType 'application/json' `
    -Body '{"email":"demo@mindlex.local","password":"demo1234"}'
$token = $login.accessToken
$auth = @{ Authorization = "Bearer $token" }
Write-Host "Вход выполнен: $($login.user.email)"

# --- 2. Чтение данных ------------------------------------------------------
$dashboard = Invoke-RestMethod -Uri "$api/dashboard" -Headers $auth
Write-Host "Дашборд получен: слов в системе $($dashboard.totalWords)"
Invoke-RestMethod -Uri "$api/decks" -Headers $auth | Out-Null
Invoke-RestMethod -Uri "$api/settings" -Headers $auth | Out-Null
Invoke-RestMethod -Uri "$api/progress" -Headers $auth | Out-Null

# --- 3. Создание данных ----------------------------------------------------
$deckName = "Логи и наблюдаемость"
$deck = Invoke-RestMethod -Uri "$api/decks" -Method Post -ContentType 'application/json' -Headers $auth `
    -Body (@{ name = $deckName; language = 'en'; description = 'колода для проверки доставки логов' } | ConvertTo-Json)
Write-Host "Создана колода: $($deck.id)"

$words = @(
    @{ term = 'telemetry';    translation = 'телеметрия';       partOfSpeech = 'NOUN' },
    @{ term = 'throughput';   translation = 'пропускная способность'; partOfSpeech = 'NOUN' },
    @{ term = 'resilient';    translation = 'устойчивый';       partOfSpeech = 'ADJECTIVE' },
    @{ term = 'to scrape';    translation = 'собирать метрики'; partOfSpeech = 'VERB' }
)
foreach ($w in $words) {
    Invoke-RestMethod -Uri "$api/decks/$($deck.id)/words" -Method Post -ContentType 'application/json' `
        -Headers $auth -Body ($w | ConvertTo-Json) | Out-Null
}
Write-Host "Добавлено слов: $($words.Count)"

# --- 4. Игровая сессия -----------------------------------------------------
try {
    $session = Invoke-RestMethod -Uri "$api/sessions" -Method Post -ContentType 'application/json' -Headers $auth `
        -Body (@{ deckId = $deck.id; mechanic = 'ACTIVE_RECALL' } | ConvertTo-Json)
    Write-Host "Сессия начата: $($session.id)"
    $question = Invoke-RestMethod -Uri "$api/sessions/$($session.id)/question" -Headers $auth
    if ($question) {
        Invoke-RestMethod -Uri "$api/sessions/$($session.id)/answers" -Method Post -ContentType 'application/json' `
            -Headers $auth -Body (@{
                questionId = $question.questionId
                answer     = $question.prompt
                elapsedMs  = 1500
            } | ConvertTo-Json) | Out-Null
        Write-Host "Ответ отправлен"
        Invoke-RestMethod -Uri "$api/sessions/$($session.id)/finish" -Method Post -Headers $auth | Out-Null
        Write-Host "Сессия завершена"
    }
} catch {
    Write-Host "Сессия: $($_.Exception.Message)"
}

# --- 5. Ошибочные и запрещённые операции -----------------------------------
Write-Host "Генерация отказов и ошибок..."

# 5.1 Неверный пароль -> 401 и предупреждение в журнале
try {
    Invoke-RestMethod -Uri "$api/auth/login" -Method Post -ContentType 'application/json' `
        -Body '{"email":"demo@mindlex.local","password":"неверный-пароль"}' | Out-Null
} catch { Write-Host "  неверный пароль -> $($_.Exception.Response.StatusCode.value__)" }

# 5.2 Битый токен -> 401
try {
    Invoke-RestMethod -Uri "$api/decks" -Headers @{ Authorization = 'Bearer invalid.token.value' } | Out-Null
} catch { Write-Host "  битый токен -> $($_.Exception.Response.StatusCode.value__)" }

# 5.3 Запрос без токена -> 401
try {
    Invoke-RestMethod -Uri "$api/users/me" | Out-Null
} catch { Write-Host "  без токена -> $($_.Exception.Response.StatusCode.value__)" }

# 5.4 Пустое имя колоды -> 400 (ошибка валидации)
try {
    Invoke-RestMethod -Uri "$api/decks" -Method Post -ContentType 'application/json' -Headers $auth `
        -Body '{"name":"","language":"en"}' | Out-Null
} catch { Write-Host "  пустое имя -> $($_.Exception.Response.StatusCode.value__)" }

# 5.5 Несуществующая колода -> 404
try {
    Invoke-RestMethod -Uri "$api/decks/11111111-1111-1111-1111-111111111111" -Headers $auth | Out-Null
} catch { Write-Host "  нет колоды -> $($_.Exception.Response.StatusCode.value__)" }

# 5.6 Повтор имени колоды и повтор слова -> 409 (нарушение целостности)
try {
    Invoke-RestMethod -Uri "$api/decks" -Method Post -ContentType 'application/json' -Headers $auth `
        -Body (@{ name = $deckName; language = 'en' } | ConvertTo-Json) | Out-Null
} catch { Write-Host "  дубль колоды -> $($_.Exception.Response.StatusCode.value__)" }
try {
    Invoke-RestMethod -Uri "$api/decks/$($deck.id)/words" -Method Post -ContentType 'application/json' -Headers $auth `
        -Body '{"term":"telemetry","translation":"дубль","partOfSpeech":"NOUN"}' | Out-Null
} catch { Write-Host "  дубль слова -> $($_.Exception.Response.StatusCode.value__)" }

# 5.7 Регистрация занятого e-mail -> 409
try {
    Invoke-RestMethod -Uri "$api/auth/register" -Method Post -ContentType 'application/json' `
        -Body '{"email":"demo@mindlex.local","password":"demo1234","displayName":"Дубль"}' | Out-Null
} catch { Write-Host "  занятый e-mail -> $($_.Exception.Response.StatusCode.value__)" }

# 5.8 Ещё немного успешных запросов, чтобы окно «последние минуты» было плотным
1..5 | ForEach-Object {
    Invoke-RestMethod -Uri "$api/dashboard" -Headers $auth | Out-Null
    Start-Sleep -Milliseconds 400
}

Write-Host "Трафик сгенерирован."
