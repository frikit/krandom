#!/usr/bin/env bash
# Validate repository-local targets in Markdown links.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

python3 - "${REPO_ROOT}" <<'PY'
from pathlib import Path
from urllib.parse import unquote
import re
import sys

root = Path(sys.argv[1]).resolve()
excluded_parts = {".git", ".gradle", ".idea", "build", "node_modules"}
link_pattern = re.compile(r"!?\[[^\]]*\]\(([^)]+)\)")
errors = []
checked = 0

for markdown in sorted(root.rglob("*.md")):
    relative = markdown.relative_to(root)
    if any(part in excluded_parts for part in relative.parts):
        continue

    text = markdown.read_text(encoding="utf-8")
    for match in link_pattern.finditer(text):
        raw_target = match.group(1).strip()
        if not raw_target or raw_target.startswith(("#", "http://", "https://", "mailto:", "tel:")):
            continue
        if "{{" in raw_target or "}}" in raw_target:
            continue

        target = raw_target
        if target.startswith("<") and ">" in target:
            target = target[1:target.index(">")]
        else:
            target = target.split(maxsplit=1)[0]
        target = unquote(target.split("#", 1)[0].split("?", 1)[0])
        if not target:
            continue

        resolved = (root / target.lstrip("/")) if target.startswith("/") else (markdown.parent / target)
        checked += 1
        if not resolved.exists():
            errors.append(f"{relative}: missing target {raw_target}")

if errors:
    print("Markdown link validation failed:", file=sys.stderr)
    for error in errors:
        print(f" - {error}", file=sys.stderr)
    sys.exit(1)

print(f"Markdown link validation passed ({checked} repository-local targets checked).")
PY
