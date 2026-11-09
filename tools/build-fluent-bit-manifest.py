#!/usr/bin/env python3
"""Сборка манифеста Fluent Bit из файлов конфигурации.

Конфигурация сборщика (fluent-bit.conf, parsers_extra.conf, Lua-скрипт) лежит
в deploy/observability/fluent-bit/ и используется также стендом проверки. ConfigMap
для кластера собирается из этих же файлов, поэтому отдельной копии конфигурации
в манифесте не появляется.

Запуск из корня репозитория:
    python tools/build-fluent-bit-manifest.py
"""

from pathlib import Path

import yaml

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "deploy" / "observability" / "fluent-bit"
OUT = ROOT / "k8s" / "observability" / "70-fluent-bit.yaml"

NAMESPACE = "mindlex"
NAME = "fluent-bit"


def block_scalar(text: str) -> str:
    """Возвращает текст пригодным для YAML block scalar (|).

    Убираем завершающие пробелы, нормализуем переводы строк и гарантируем
    перевод строки в конце — иначе YAML-парсер склеит последнюю строку.
    """
    lines = [line.rstrip() for line in text.replace("\r\n", "\n").split("\n")]
    while lines and lines[-1] == "":
        lines.pop()
    return "\n".join(lines) + "\n"


def main() -> None:
    conf = block_scalar((SRC / "fluent-bit.conf").read_text(encoding="utf-8"))
    parsers = block_scalar((SRC / "parsers_extra.conf").read_text(encoding="utf-8"))
    lua = block_scalar((SRC / "scripts" / "prepare_record.lua").read_text(encoding="utf-8"))

    documents = [
        {
            "apiVersion": "v1",
            "kind": "ServiceAccount",
            "metadata": {
                "name": NAME,
                "namespace": NAMESPACE,
                "labels": {"app.kubernetes.io/name": NAME, "app.kubernetes.io/component": "logging"},
            },
        },
        {
            # Фильтр kubernetes обращается к API-серверу за метаданными подов.
            # Нужен только доступ на чтение в пределах кластера.
            "apiVersion": "rbac.authorization.k8s.io/v1",
            "kind": "ClusterRole",
            "metadata": {"name": f"{NAME}-read"},
            "rules": [
                {
                    "apiGroups": [""],
                    "resources": ["pods", "namespaces"],
                    "verbs": ["get", "list", "watch"],
                }
            ],
        },
        {
            "apiVersion": "rbac.authorization.k8s.io/v1",
            "kind": "ClusterRoleBinding",
            "metadata": {"name": f"{NAME}-read"},
            "roleRef": {
                "apiGroup": "rbac.authorization.k8s.io",
                "kind": "ClusterRole",
                "name": f"{NAME}-read",
            },
            "subjects": [
                {"kind": "ServiceAccount", "name": NAME, "namespace": NAMESPACE}
            ],
        },
        {
            "apiVersion": "v1",
            "kind": "ConfigMap",
            "metadata": {
                "name": f"{NAME}-config",
                "namespace": NAMESPACE,
                "labels": {"app.kubernetes.io/name": NAME, "app.kubernetes.io/component": "logging"},
            },
            # Ключи повторяют раскладку каталогов образа Fluent Bit, поэтому в DaemonSet
            # файлы монтируются в /fluent-bit/etc/ и правки путей не требуются.
            "data": {
                "fluent-bit.conf": conf,
                "conf/parsers_extra.conf": parsers,
                "conf/scripts/prepare_record.lua": lua,
            },
        },
        {
            "apiVersion": "apps/v1",
            "kind": "DaemonSet",
            "metadata": {
                "name": NAME,
                "namespace": NAMESPACE,
                "labels": {"app.kubernetes.io/name": NAME, "app.kubernetes.io/component": "logging"},
            },
            "spec": {
                "selector": {"matchLabels": {"app.kubernetes.io/name": NAME}},
                # Сборщик должен работать на каждом узле: только так читаются журналы
                # всех подов, которые kubelet складывает в /var/log/pods локально.
                "template": {
                    "metadata": {
                        "labels": {"app.kubernetes.io/name": NAME, "app.kubernetes.io/component": "logging"}
                    },
                    "spec": {
                        "serviceAccountName": NAME,
                        "tolerations": [
                            # Не собирать журналы управляющих узлов не нужно, но и мешать
                            # планировщику тоже: стандартные taint'ы узлов не трогаем.
                        ],
                        "containers": [
                            {
                                "name": NAME,
                                "image": "cr.fluentbit.io/fluent/fluent-bit:3.2.6",
                                "imagePullPolicy": "IfNotPresent",
                                # Дополнительные парсеры подключаются из самого конфига
                                # (Parsers_File conf/parsers_extra.conf), поэтому аргумент
                                # командной строки нужен только для основного файла.
                                "args": [
                                    "--config=/fluent-bit/etc/fluent-bit.conf",
                                ],
                                "ports": [
                                    {"name": "http", "containerPort": 2020}
                                ],
                                "env": [
                                    {"name": "POD_NAME", "valueFrom": {"fieldRef": {"fieldPath": "metadata.name"}}},
                                    {"name": "NODE_NAME", "valueFrom": {"fieldRef": {"fieldPath": "spec.nodeName"}}},
                                ],
                                "resources": {
                                    "requests": {"cpu": "50m", "memory": "64Mi"},
                                    "limits": {"memory": "256Mi"},
                                },
                                "volumeMounts": [
                                    {"name": "config", "mountPath": "/fluent-bit/etc/fluent-bit.conf",
                                     "subPath": "fluent-bit.conf", "readOnly": True},
                                    {"name": "config", "mountPath": "/fluent-bit/etc/conf/parsers_extra.conf",
                                     "subPath": "conf/parsers_extra.conf", "readOnly": True},
                                    {"name": "config", "mountPath": "/fluent-bit/etc/conf/scripts/prepare_record.lua",
                                     "subPath": "conf/scripts/prepare_record.lua", "readOnly": True},
                                    # Журналы контейнеров узла: /var/log/containers — симлинки
                                    # на файлы в /var/log/pods, куда пишет kubelet.
                                    {"name": "varlog", "mountPath": "/var/log", "readOnly": True},
                                    # Рабочий каталог с файлом позиций чтения.
                                    {"name": "state", "mountPath": "/var/lib/fluent-bit"},
                                ],
                            }
                        ],
                        "volumes": [
                            {"name": "config", "configMap": {"name": f"{NAME}-config"}},
                            {
                                "name": "varlog",
                                "hostPath": {"path": "/var/log"},
                            },
                            {
                                # Позиции чтения на узле: перезапуск пода не приводит
                                # к повторной отправке уже собранных записей.
                                "name": "state",
                                "hostPath": {"path": "/var/lib/fluent-bit", "type": "DirectoryOrCreate"},
                            },
                        ],
                    },
                },
            },
        },
    ]

    header = (
        "# ============================================================================\n"
        "#  Fluent Bit: сбор журналов подов MindLex и доставка в Victoria Logs.\n"
        "#\n"
        "#  ФАЙЛ СГЕНЕРИРОВАН: tools/build-fluent-bit-manifest.py\n"
        "#  Исходные файлы конфигурации — deploy/observability/fluent-bit/;\n"
        "#  после их правки манифест пересобирается той же командой:\n"
        "#      python tools/build-fluent-bit-manifest.py\n"
        "#\n"
        "#  DaemonSet выбран вместо сайдкара: одна копия сборщика на узел вместо копии\n"
        "#  в каждом поде приложения, сборщик не делит лимиты памяти с приложением\n"
        "#  и не требует правки манифестов подов. Источник данных — те же журналы\n"
        "#  kubelet в /var/log/containers, что читал бы сайдкар.\n"
        "# ============================================================================\n"
    )

    with OUT.open("w", encoding="utf-8", newline="\n") as fh:
        fh.write(header)
        for doc in documents:
            fh.write("---\n")
            fh.write(
                yaml.safe_dump(
                    doc,
                    allow_unicode=True,
                    sort_keys=False,
                    default_flow_style=False,
                    width=1000,
                )
            )

    print(f"Записан {OUT.relative_to(ROOT)} ({OUT.stat().st_size} байт)")


if __name__ == "__main__":
    main()
