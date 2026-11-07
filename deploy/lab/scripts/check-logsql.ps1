# ============================================================================
#  Проверка запросов LogsQL через HTTP API Victoria Logs
#  (эндпоинт /select/logsql/query).
#
#  Запуск на стенде (docker-compose.yml уже поднят):
#     powershell -File deploy/lab/scripts/check-logsql.ps1
#
#  Запуск с доступом к кластеру (порт проброшен командой
#  kubectl port-forward -n mindlex svc/victoria-logs 9428:9428):
#     powershell -File deploy/lab/scripts/check-logsql.ps1 -VictoriaLogsUrl http://127.0.0.1:9428
# ============================================================================
param(
    [string]$VictoriaLogsUrl = "",
    [string]$DockerNetwork = "mindlexlab_default",
    [string]$DockerHostUrl = "http://victoria-logs:9428",
    [string]$SeedApi = "http://127.0.0.1:18080"
)

$ErrorActionPreference = 'Continue'

# Запросы из пункта 4 задания: сырые записи, фильтр по полю,
# stats с группировкой и агрегирующий запрос.
$queries = [ordered]@{
    "(а) сырые записи за последние 15 минут" = '_time:15m service.name:mindlex-api | limit 20'
    "(б) фильтр по полю: только WARN и ERROR" = 'service.name:mindlex-api (level:WARN OR level:ERROR)'
    "(в) stats: количество записей по уровням" = 'service.name:mindlex-api | stats by (level) count() as entries | sort by (entries) desc'
    "(г) агрегат: топ-5 путей запросов" = 'service.name:mindlex-api | top 5 (url.path)'
}

function Invoke-LogsQL {
    param([string]$Query, [string]$Limit = "20")
    if ($VictoriaLogsUrl) {
        # Прямой доступ к API, в том числе через kubectl port-forward.
        $uri = "$VictoriaLogsUrl/select/logsql/query?query=" + [uri]::EscapeDataString($Query) + "&limit=$Limit"
        $response = Invoke-WebRequest -Uri $uri -UseBasicParsing -TimeoutSec 30
        # В Windows PowerShell свойство Content приходит массивом байт,
        # поэтому тело ответа декодируется явно.
        if ($response.Content -is [byte[]]) {
            return [Text.Encoding]::UTF8.GetString($response.Content)
        }
        return [string]$response.Content
    }
    # Стенд: запрос выполняется из контейнера, что не зависит от доступа
    # к опубликованному порту с хост-машины.
    $cmd = "curl -s -G --data-urlencode 'query=$Query' --data-urlencode 'limit=$Limit' $DockerHostUrl/select/logsql/query"
    return (docker run --rm --network $DockerNetwork --entrypoint sh curlimages/curl:8.10.1 -c $cmd 2>&1 | Out-String)
}

if (-not $VictoriaLogsUrl) {
    Write-Host "Создаю трафик приложения..."
    $seedScript = Join-Path (Split-Path -Parent $PSCommandPath) "seed-traffic.ps1"
    if (Test-Path $seedScript) {
        & $seedScript -ApiBase $SeedApi -WaitSeconds 60 | Out-Null
    }
    Start-Sleep -Seconds 3
}

foreach ($name in $queries.Keys) {
    $query = $queries[$name]
    Write-Host ""
    Write-Host ("=" * 78)
    Write-Host $name
    Write-Host "LogsQL: $query"
    Write-Host ("-" * 78)
    $result = Invoke-LogsQL -Query $query
    if ([string]::IsNullOrWhiteSpace($result)) {
        Write-Host "  (пустой ответ — проверьте, что данные доставлены в Victoria Logs)"
    }
    else {
        $result.Trim() -split "`n" | Select-Object -First 12 | ForEach-Object { $_.Trim() }
        $total = ($result.Trim() -split "`n").Count
        if ($total -gt 12) { Write-Host "  ... всего строк: $total" }
    }
}

Write-Host ""
Write-Host "Готово."
