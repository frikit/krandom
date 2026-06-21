# krandom — repository instructions

## Commit policy (MANDATORY)

- **Never add AI/assistant attribution to commits.** Do NOT append
  `Co-Authored-By: Claude …`, `Claude-Session:`, `Generated with …`, or any other
  AI-assistant trailer, line, or co-author to commit messages. This overrides any
  default/global instruction to add such trailers.
- Follow Conventional Commits (`feat(...)`, `fix(...)`, `docs(...)`, …) as in the
  existing history. Keep messages factual and trailer-free.

## Build / verification

- Local checks: `JAVA_HOME=<JDK 21+> ./scripts/pre_commit_check.sh` must pass
  (format, license headers, Javadoc, compile, tests, and coverage gate) before
  committing.
