# AGENTS.md

## Scope
- Chronicle Logger is a multi-module Maven repo covering logger-core, logger-slf4j, logger-slf4j-2, logger-log4j-2, logger-jcl, logger-jul, logger-tools, and benchmark.
- Key docs live in `README.adoc` and `src/main/docs/`.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Module-scoped example:
  - `mvn -pl <module> -am verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -pl <module> -Dtest=<TestClass> test -l logs/mvn-test.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Repo map
- Decision logs and requirements live under `src/main/docs/`.
- Code review playbook: `src/main/docs/code-review-playbook.adoc`.

## Constraints
- Java baseline: 8 (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation in hot paths.

## Docs and review checklist
- Keep AsciiDoc, tests, and code in sync; update docs for new requirements or behaviour.
- Javadoc must add behaviour, edge cases, thread safety, units, or performance notes.
- For large mechanical changes, declare the transformation rule and keep it consistent.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and AsciiDoc conventions.
- `src/main/docs/decision-log.adoc` and `src/main/docs/project-requirements.adoc`.
