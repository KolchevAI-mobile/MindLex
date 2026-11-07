# ============================================================================
#  Съём скриншотов vmui (веб-интерфейс Victoria Logs) через Chrome DevTools
#  Protocol. Используется Edge/Chrome в headless-режиме: отдельные библиотеки
#  (Playwright/Puppeteer) не требуются — только встроенный веб-сокет .NET.
#
#  Запуск:
#     powershell -File deploy/lab/scripts/capture-vmui.ps1
#
#  Результат: PNG-файлы в docs/screenshots/ (путь задаётся параметром -OutDir).
# ============================================================================
param(
    [string]$VmuiUrl = "http://127.0.0.1:19428/select/vmui/",
    [string]$OutDir = "docs/screenshots",
    [int]$DebugPort = 19222,
    [int]$Width = 1600,
    [int]$Height = 1000
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

# --- Запросы: подпись -> текст LogsQL ----------------------------------------
# Имена файлов продолжают нумерацию скриншотов интерфейса (01…09).
$shots = [ordered]@{
    "10-vmui-сырые-записи-15-минут"  = '_time:15m service.name:mindlex-api | limit 100'
    "11-vmui-фильтр-по-уровню"       = 'service.name:mindlex-api (level:WARN OR level:ERROR) | limit 100'
    "12-vmui-статистика-по-уровням"  = 'service.name:mindlex-api | stats by (level) count() as entries | sort by (entries) desc'
    "13-vmui-топ-путей-запросов"     = 'service.name:mindlex-api url.path:* | top 5 (url.path)'
    "14-vmui-уникальные-запросы"     = 'service.name:mindlex-api | stats count_uniq(request_id) as unique_requests'
}

function Find-Browser {
    $candidates = @(
        "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
        "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
        "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe",
        "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
        "$env:LOCALAPPDATA\Google\Chrome\Application\chrome.exe"
    )
    foreach ($c in $candidates) { if (Test-Path $c) { return $c } }
    throw "Не найден Chrome или Edge"
}

# --- Минимальный клиент CDP поверх ClientWebSocket ---------------------------
class CdpClient {
    [System.Net.WebSockets.ClientWebSocket]$Ws
    [int]$Id = 0

    CdpClient([string]$wsUrl) {
        $this.Ws = [System.Net.WebSockets.ClientWebSocket]::new()
        $this.Ws.ConnectAsync([uri]$wsUrl, [Threading.CancellationToken]::None).GetAwaiter().GetResult()
    }

    [void] Send([string]$method, [hashtable]$params) {
        $this.Id++
        $payload = @{ id = $this.Id; method = $method; params = $params } | ConvertTo-Json -Depth 12 -Compress
        $bytes = [Text.Encoding]::UTF8.GetBytes($payload)
        $seg = [ArraySegment[byte]]::new($bytes)
        $this.Ws.SendAsync($seg, [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [Threading.CancellationToken]::None).GetAwaiter().GetResult()
    }

    # Читает сообщения до ответа с нужным id (уведомления событий пропускаются).
    [string] Wait([int]$wantedId, [int]$timeoutSec = 60) {
        $buffer = New-Object byte[] 1048576
        $segment = [ArraySegment[byte]]::new($buffer)
        $deadline = (Get-Date).AddSeconds($timeoutSec)
        while ((Get-Date) -lt $deadline) {
            $sb = [Text.StringBuilder]::new()
            do {
                $task = $this.Ws.ReceiveAsync($segment, [Threading.CancellationToken]::None)
                if (-not $task.Wait(15000)) { throw "Таймаут ожидания ответа CDP" }
                $res = $task.Result
                [void]$sb.Append([Text.Encoding]::UTF8.GetString($buffer, 0, $res.Count))
            } while (-not $res.EndOfMessage)
            $text = $sb.ToString()
            if ($text -match '"id"') {
                $obj = $text | ConvertFrom-Json
                if ($obj.id -eq $wantedId) { return $text }
            }
        }
        throw "Не получен ответ на команду $wantedId"
    }

    # Выполняет команду и возвращает результат (result).
    [object] Call([string]$method, [hashtable]$params, [int]$timeoutSec = 60) {
        $this.Send($method, $params)
        $raw = $this.Wait($this.Id, $timeoutSec)
        return ($raw | ConvertFrom-Json).result
    }

    [void] Close() {
        try { $this.Ws.Dispose() } catch { }
    }
}

# --- Запуск браузера ---------------------------------------------------------
$browser = Find-Browser
$profile = Join-Path $env:TEMP ("vmui-capture-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $profile | Out-Null
Write-Host "Браузер: $browser"

$proc = Start-Process -FilePath $browser -PassThru -ArgumentList @(
    "--headless=new",
    "--remote-debugging-port=$DebugPort",
    "--remote-allow-origins=*",
    "--user-data-dir=$profile",
    "--no-first-run", "--no-default-browser-check", "--disable-gpu",
    "--hide-scrollbars",
    "--window-size=$Width,$Height",
    "about:blank"
)

try {
    # Ждём готовности отладочного порта.
    $target = $null
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Milliseconds 700
        try {
            $list = Invoke-RestMethod -Uri "http://127.0.0.1:$DebugPort/json/list" -TimeoutSec 5
            $target = $list | Where-Object { $_.type -eq 'page' } | Select-Object -First 1
            if ($target) { break }
        } catch { }
    }
    if (-not $target) { throw "Не удалось получить вкладку браузера по CDP" }

    $wsUrl = $target.webSocketDebuggerUrl
    $cdp = [CdpClient]::new($wsUrl)

    [void]$cdp.Call("Page.enable", @{}, 90)
    [void]$cdp.Call("Runtime.enable", @{}, 90)
    [void]$cdp.Call("Emulation.setDeviceMetricsOverride", @{
        width = $Width; height = $Height; deviceScaleFactor = 1.5; mobile = $false
    }, 90)

    Write-Host "Открываю $VmuiUrl ..."
    [void]$cdp.Call("Page.navigate", @{ url = $VmuiUrl }, 90)
    Start-Sleep -Seconds 6

    # Проверяем, что приложение отрисовалось и поле ввода доступно.
    $ready = $cdp.Call("Runtime.evaluate", @{
        expression = "document.querySelectorAll('textarea').length + ':' + (document.body.innerText.indexOf('LogsQL') >= 0)"
        returnByValue = $true
    }, 90)
    Write-Host "Состояние страницы (textarea:есть LogsQL) = $($ready.result.value)"
    if ($ready.result.value -notmatch '^[1-9]') {
        throw "Поле ввода LogsQL не найдено — интерфейс не загрузился"
    }

    if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Force -Path $OutDir | Out-Null }
    $outFull = (Resolve-Path $OutDir).Path

    # Диагностика: перечисляем управляющие элементы, чтобы понять разметку vmui
    # (селекторы зависят от версии интерфейса).
    $diag = $cdp.Call("Runtime.evaluate", @{
        expression = "(function(){var r=[];document.querySelectorAll('button,[role=combobox],[role=button],input').forEach(function(e,i){var t=(e.innerText||'').trim().slice(0,40);var a=e.getAttribute('aria-label')||'';if(r.length<12){r.push(i+' | aria='+a+' | text='+t);}});return r.join('\n');})()"
        returnByValue = $true
    }, 90)
    Write-Host "--- элементы страницы ---"
    Write-Host $diag.result.value

    # Диапазон времени: по умолчанию vmui показывает «Last 5 minutes», а запросы в отчёте
    # сформулированы на 15 минут — приводим окно к тому же виду, чтобы числа совпадали.
    [void]$cdp.Call("Runtime.evaluate", @{
        expression = "(function(){var b=document.querySelector('button[aria-label=""time range controls""]');b.dispatchEvent(new MouseEvent('mousedown',{bubbles:true}));b.dispatchEvent(new MouseEvent('mouseup',{bubbles:true}));b.click();return 'opened';})()"
        returnByValue = $true
    }, 90)
    Start-Sleep -Seconds 3
    $picked = $cdp.Call("Runtime.evaluate", @{
        expression = "(function(){var all=document.querySelectorAll('div,span,li,button,label');for(var i=0;i<all.length;i++){var t=(all[i].innerText||'').trim();if(t==='Last 15 minutes'){all[i].click();return 'preset-clicked';}}return 'not-found';})()"
        returnByValue = $true
    }, 90)
    Start-Sleep -Seconds 1
    [void]$cdp.Call("Runtime.evaluate", @{
        expression = "(function(){var all=document.querySelectorAll('button');for(var i=0;i<all.length;i++){if((all[i].innerText||'').trim()==='Apply'){all[i].click();return 'applied';}}return 'no-apply';})()"
        returnByValue = $true
    }, 90)
    Start-Sleep -Seconds 3
    $rangeText = $cdp.Call("Runtime.evaluate", @{
        expression = "document.querySelector('button[aria-label=""time range controls""]').innerText.trim()"
        returnByValue = $true
    }, 90)
    Write-Host "Выбор диапазона: $($picked.result.value); текущий диапазон: $($rangeText.result.value)"

    foreach ($name in $shots.Keys) {
        $query = $shots[$name]
        Write-Host "Снимаю: $name"

        # Вводим запрос как пользователь: через вставку в поле и нажатие Enter.
        # Значение ставится нативным сеттером, иначе React не увидит изменение.
        $escaped = $query.Replace('\', '\\').Replace("'", "\'").Replace('"', '\"')
        $js = @"
(() => {
  const ta = document.querySelector('textarea');
  if (!ta) { return 'no-textarea'; }
  const setter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
  setter.call(ta, "$escaped");
  ta.dispatchEvent(new Event('input', { bubbles: true }));
  ta.focus();
  return 'input-set';
})()
"@
        $res = $cdp.Call("Runtime.evaluate", @{ expression = $js; returnByValue = $true; awaitPromise = $false }, 90)
        if ($res.result.value -ne 'input-set') { throw "Не удалось установить запрос: $($res.result.value)" }
        Start-Sleep -Milliseconds 500

        # Enter запускает выполнение запроса в vmui.
        foreach ($type in @('keyDown', 'keyUp')) {
            [void]$cdp.Call("Input.dispatchKeyEvent", @{
                type = $type; key = 'Enter'; code = 'Enter'; windowsVirtualKeyCode = 13; nativeVirtualKeyCode = 13
            }, 90)
        }
        Start-Sleep -Seconds 5

        # Убираем лишние подсказки, если они перекрывают таблицу результатов.
        [void]$cdp.Call("Runtime.evaluate", @{
            expression = "document.activeElement && document.activeElement.blur(); true"
            returnByValue = $true
        }, 90)
        Start-Sleep -Seconds 1

        $shot = $cdp.Call("Page.captureScreenshot", @{ format = "png"; captureBeyondViewport = $true }, 90)
        $path = Join-Path $outFull "$name.png"
        [IO.File]::WriteAllBytes($path, [Convert]::FromBase64String($shot.data))
        $size = (Get-Item $path).Length
        Write-Host ("  сохранено: {0} ({1:N0} КБ)" -f $path, ($size / 1KB))
    }

    $cdp.Close()
}
finally {
    if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue }
    Get-Process msedge, chrome -ErrorAction SilentlyContinue |
        Where-Object { $_.Path -eq $browser } |
        ForEach-Object { if ($_.StartTime -gt (Get-Date).AddMinutes(-10)) { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue } }
    Write-Host "Готово."
}
