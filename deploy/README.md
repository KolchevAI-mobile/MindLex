# Развёртывание MindLex и доставка журналов в Victoria Logs (ДЗ №2)

Состав каталога:

| Каталог | Назначение |
|---|---|
| `k8s/` | манифесты для minikube: приложение из ДЗ №1, Victoria Logs, Fluent Bit |
| `deploy/lab/` | стенд на Docker для проверки доставки журналов без кластера |
| `deploy/observability/fluent-bit/` | конфигурация сборщика (используется и в кластере, и на стенде) |
| `tools/build-fluent-bit-manifest.py` | собирает ConfigMap Fluent Bit из файлов конфигурации |

Конфигурация сборщика хранится в одном месте: `fluent-bit.conf`, `parsers_extra.conf` и
`scripts/prepare_record.lua`. Из них скриптом собирается ConfigMap для Kubernetes, они же
монтируются в контейнер на стенде. Отличие стенда от кластера — путь к журналам
(`/var/log/mindlex/*.log` против `/var/log/containers/*.log`) и отсутствие фильтра `kubernetes`,
вместо которого на стенде работает фильтр `modify` с теми же именами полей.

---

## 1. Развёртывание в minikube

Требуется: `minikube`, `kubectl`, собранный образ приложения.

```bash
# 1. Кластер. 4 ГБ памяти хватает для PostgreSQL, приложения,
#    Victoria Logs и Fluent Bit.
minikube start --memory=4096 --cpus=2

# 2. Образ приложения собирается внутри Docker-демона minikube,
#    поэтому публиковать его в реестр не нужно (imagePullPolicy: IfNotPresent).
minikube image build -t mindlex-api:1.0.0 ./backend

# 3. Пространство имён, секреты, PostgreSQL, приложение.
kubectl apply -f k8s/app/

# 4. Victoria Logs.
kubectl apply -f k8s/observability/60-victoria-logs.yaml

# 5. Сборщик журналов (ConfigMap, RBAC, DaemonSet).
python tools/build-fluent-bit-manifest.py
kubectl apply -f k8s/observability/70-fluent-bit.yaml

# 6. Алерт по журналам через vmalert (бонус задания).
kubectl apply -f k8s/observability/80-vmalert-bonus.yaml

# 7. Состояние.
kubectl -n mindlex get pods,svc,pvc
```

Ожидаемый результат:

```
NAME                             READY   STATUS    RESTARTS
mindlex-api-...                  1/1     Running   0
mindlex-db-...                   1/1     Running   0
victoria-logs-...                1/1     Running   0
fluent-bit-xxxxx                 1/1     Running   0        <- по одному поду на узел
vmalert-...                      1/1     Running   0        <- если применён п. 6
```

---

## 2. Доступ к vmui

```bash
kubectl -n mindlex port-forward svc/victoria-logs 9428:9428
# http://127.0.0.1:9428/select/vmui/
```

Проверка доставки журналов без браузера:

```bash
kubectl -n mindlex port-forward svc/victoria-logs 9428:9428 &

curl -s -G http://127.0.0.1:9428/select/logsql/query \
  --data-urlencode 'query=service.name:mindlex-api' \
  --data-urlencode 'limit=5'
```

Поле `_msg` в ответе должно содержать текст события. Значение
`missing _msg field` означает, что хранилище не нашло поле, указанное в `_msg_field`,
то есть JSON приложения не разобран:

```bash
curl -s -G http://127.0.0.1:9428/select/logsql/query \
  --data-urlencode 'query=level:WARN' | head -3
```

---

## 3. Запросы из задания

Выполняются в vmui (поле `Log query`) либо через API. Описание запросов, их результаты
и скриншоты — в `Отчёт-VictoriaLogs.md`, раздел 5.

| Пункт | LogsQL |
|---|---|
| (а) сырые записи за 15 минут | `_time:15m service.name:mindlex-api \| limit 100` |
| (б) фильтр по полю | `service.name:mindlex-api (level:WARN OR level:ERROR) \| limit 100` |
| (в) stats с группировкой | `service.name:mindlex-api \| stats by (level) count() as entries \| sort by (entries) desc` |
| (г) агрегирующий | `service.name:mindlex-api url.path:* \| top 5 (url.path)` |
| (г) дополнительно | `service.name:mindlex-api \| stats count_uniq(request_id) as unique_requests` |

Проверка этих же запросов скриптом — для стенда и для кластера через port-forward:

```powershell
powershell -File deploy/lab/scripts/check-logsql.ps1 -VictoriaLogsUrl http://127.0.0.1:9428
```

---

## 4. Стенд на Docker

Стенд поднимает тот же конвейер «приложение → сборщик → хранилище → LogsQL» на четырёх
контейнерах: PostgreSQL, приложение, Victoria Logs, Fluent Bit.

```bash
# 1. Собрать jar (в образ копируется готовый файл, см. deploy/lab/Dockerfile).
cd backend && ./mvnw -o -DskipTests package && cd ..

# 2. Запустить стенд.
docker compose -f deploy/lab/docker-compose.yml -p mindlexlab up -d --build

# 3. Создать трафик: успешные запросы, отказы, ошибки валидации.
powershell -File deploy/lab/scripts/seed-traffic.ps1

# 4. Проверить запросы.
powershell -File deploy/lab/scripts/check-logsql.ps1

# 5. Открыть vmui: http://127.0.0.1:19428/select/vmui/
```

Скриншоты для отчёта снимаются скриптом, который запускает Chrome в headless-режиме,
открывает vmui, выставляет диапазон времени 15 минут, вводит каждый запрос, нажимает Enter
и сохраняет PNG в `docs/screenshots/`. Внешние библиотеки (Playwright, Puppeteer) не нужны —
используется Chrome DevTools Protocol через веб-сокет из .NET:

```powershell
powershell -File deploy/lab/scripts/capture-vmui.ps1
```

### Особенности стенда

* **Права на каталог журналов.** Docker создаёт пустой том с владельцем `root`, поэтому
  приложение, работающее непривилегированным пользователем, в него писать не может.
  На стенде используется bind-mount каталога `deploy/lab/logs`. В Kubernetes этой проблемы
  нет: файлы в `/var/log/pods` создаёт kubelet.
* **Позиция чтения.** Fluent Bit хранит её в `TAIL_DB`, поэтому перезапуск пода не приводит
  к повторной отправке уже собранных записей. При пустом файле позиций существующие журналы
  читаются с начала, то есть после первого развёртывания в хранилище попадает и история.
  Чтобы перечитать файл заново, нужно удалить состояние сборщика:
  `docker volume rm mindlexlab_fluent-bit-state`.

### Ограничения Fluent Bit, влияющие на конфигурацию

Найдены при отладке; часть из них не описана в документации явно:

* в конфигурации поддерживаются только переменные вида `${VAR}`; значение по умолчанию
  (`${VAR:default}`) не подставляется — переменная остаётся пустой;
* `Flush_timeout` в multiline-парсере меньше 60 приводит к отказу запуска сборщика;
* плагин `es` при заданном `Pipeline` формирует путь `.../_bulk/` с косой чертой,
  а Victoria Logs такой путь отклоняет ответом `400 unsupported path requested`. Поэтому
  параметры приёма (`_msg_field`, `_time_field`, `_stream_fields`) записаны прямо в опцию
  `Path`, а `Pipeline` и `Logstash_Format` не используются: в режиме `Logstash_Format`
  путь формируется как `.../_bulk/<prefix>-<date>`;
* `Write_Operation upsert` требует `Generate_Id` или `Id_Key`. Выбран `create`:
  Victoria Logs читает только строки с документами, метаданные действия ей не нужны;
* дополнительные парсеры подключаются из самого конфига строкой
  `Parsers_File conf/parsers_extra.conf`: путь разрешается относительно каталога
  `/fluent-bit/etc`, в котором лежит основной файл. Флага командной строки нет:
  в контейнере образа Fluent Bit нет оболочки, а нужный ключ (`-R/--parser`) легко
  перепутать с `-p/--prop` — при ошибке в имени Fluent Bit печатает справку и выходит,
  вместо того чтобы сообщить о неверном аргументе.

---

## 5. Переменные окружения приложения, влияющие на журнал

| Переменная | Значение | Назначение |
|---|---|---|
| `MINDLEX_LOG_LEVEL` | `INFO` | уровень корневого регистратора |
| `MINDLEX_APP_LOG_LEVEL` | `INFO` | уровень регистратора `ru.mindlex` |
| `SPRING_PROFILES_ACTIVE` | `json` | JSON-формат журнала; `plain` возвращает текстовый шаблон |
| `MINDLEX_DEMO_USER_ENABLED` | `true` | создаёт демонстрационного пользователя |
