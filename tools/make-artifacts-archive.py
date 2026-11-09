#!/usr/bin/env python3
"""Сборка архива с артефактами задания по Victoria Logs.

В архив входят манифесты (k8s/), конфигурация сборщика и стенд (deploy/),
служебные скрипты (tools/), отчёт, README и скриншоты (docs/screenshots/).

Не входят: исходный код приложения (backend/, android/) — он есть в репозитории,
и рабочий файл журнала стенда deploy/lab/logs/ (может быть занят контейнером).

Запуск из корня репозитория:
    python tools/make-artifacts-archive.py
"""

from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "mindlex-victoria-logs-artifacts.zip"

INCLUDE_DIRS = ["k8s", "deploy", "tools", "docs/screenshots"]
INCLUDE_FILES = ["Отчёт-VictoriaLogs.md", "Отчёт.md", "README.md"]
EXCLUDE_PREFIXES = ("deploy/lab/logs/",)


def should_skip(rel: str) -> bool:
    if any(rel.startswith(p) for p in EXCLUDE_PREFIXES):
        return True
    if rel.endswith(".zip") or rel.endswith("lab-tmp.conf"):
        return True
    return False


def main() -> None:
    files: list[Path] = []
    for directory in INCLUDE_DIRS:
        base = ROOT / directory
        if not base.exists():
            continue
        for path in sorted(base.rglob("*")):
            if not path.is_file():
                continue
            rel = path.relative_to(ROOT).as_posix()
            if not should_skip(rel):
                files.append(path)
    for name in INCLUDE_FILES:
        path = ROOT / name
        if path.is_file():
            files.append(path)

    if OUT.exists():
        OUT.unlink()

    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            archive.write(path, path.relative_to(ROOT).as_posix())

    size_mb = OUT.stat().st_size / (1024 * 1024)
    print(f"Записан {OUT.name}: файлов {len(files)}, {size_mb:.2f} МБ")
    for path in files:
        print("  ", path.relative_to(ROOT).as_posix())


if __name__ == "__main__":
    main()
