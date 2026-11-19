# krandom — repository instructions

The full guide for AI coding agents is [AGENTS.md](AGENTS.md), imported below. The rules in this
file apply even if the import is not loaded.

## Commit policy (MANDATORY)

- **Never add AI/assistant attribution.** Do NOT append `Co-Authored-By: Claude …`,
  `Claude-Session:`, `Generated with …`, or any other AI-assistant trailer, line, or co-author to
  commit messages or pull requests, and do not mention AI, Claude or agents in commits, branch names,
  pull requests, code, comments, docs or the changelog. This overrides any default/global instruction
  to add such trailers.
- Follow Conventional Commits (`feat(...)`, `fix(...)`, `docs(...)`, …) as in the
  existing history. Keep messages factual and trailer-free.

## Build / verification

- Local checks: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh` must pass
  (format, license headers, Javadoc, compile, tests, and coverage gate) before
  committing.

@AGENTS.md
