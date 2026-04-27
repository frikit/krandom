#!/usr/bin/env bash
# Validate docs-site internal links that are expected to survive GitHub Pages project-page deployment.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCS_DIR="${ROOT_DIR}/docs-site"

python3 - "${DOCS_DIR}" <<'PY'
from pathlib import Path
import re
import sys

docs_dir = Path(sys.argv[1])
config = (docs_dir / "_config.yml").read_text(encoding="utf-8")
errors = []

if not re.search(r"^baseurl:\s*[\"']?/krandom[\"']?\s*$", config, flags=re.MULTILINE):
    errors.append("docs-site/_config.yml must set baseurl: /krandom")

link_re = re.compile(r"\[[^\]]+\]\(([^)]+)\)")
relative_url_re = re.compile(r"\{\{\s*'([^']+)'\s*\|\s*relative_url\s*\}\}")
permalink_re = re.compile(r"^permalink:\s*(\S+)\s*$")

def front_matter(text):
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        return []
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            return lines[1:index]
    return []

def normalize_site_path(path):
    if not path:
        return path
    if path.startswith("/krandom/"):
        path = path[len("/krandom"):]
    if path == "/krandom":
        path = "/"
    if not path.startswith("/"):
        path = "/" + path
    return path

permalinks = set()
for md_file in docs_dir.rglob("*.md"):
    text = md_file.read_text(encoding="utf-8")
    for line in front_matter(text):
        match = permalink_re.match(line.strip())
        if not match:
            continue
        permalink = normalize_site_path(match.group(1).strip('"\''))
        permalinks.add(permalink)
        if permalink != "/" and permalink.endswith("/"):
            permalinks.add(permalink[:-1])
        elif permalink != "/":
            permalinks.add(permalink + "/")

for md_file in sorted(docs_dir.rglob("*.md")):
    rel_file = md_file.relative_to(docs_dir)
    text = md_file.read_text(encoding="utf-8")
    for match in link_re.finditer(text):
        raw_url = match.group(1).strip()
        if not raw_url or raw_url.startswith(("#", "http://", "https://", "mailto:", "tel:")):
            continue
        liquid_matches = list(relative_url_re.finditer(raw_url))
        if liquid_matches:
            for liquid_match in liquid_matches:
                target = normalize_site_path(liquid_match.group(1).split("#", 1)[0].split("?", 1)[0])
                if target not in permalinks:
                    errors.append(f"{rel_file}: unknown relative_url target {liquid_match.group(1)}")
            continue
        target = raw_url.split("#", 1)[0].split("?", 1)[0]
        if target.endswith(".md"):
            errors.append(f"{rel_file}: use a permalink-safe relative_url link instead of {raw_url}")
            continue
        if target.startswith("/"):
            normalized = normalize_site_path(target)
            if normalized not in permalinks:
                errors.append(f"{rel_file}: unknown site path {raw_url}")

if errors:
    print("Docs-site link validation failed:", file=sys.stderr)
    for error in errors:
        print(f" - {error}", file=sys.stderr)
    sys.exit(1)

print(f"Docs-site link validation passed ({len(permalinks)} known permalink forms).")
PY
