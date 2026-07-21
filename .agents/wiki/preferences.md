# Working Preferences & Standards

## Code conventions

- **No Lombok.** Use plain Java; no annotation-based code generation.
- **No Records for public APIs.** Records are acceptable for internal/test-only data classes.
- **Java conventions.** Follow standard Java naming and formatting conventions throughout.
- **No backward-compatibility breaks.** Changes to existing examples must remain runnable; do not silently break anything that was working.
- **Consistent module layout.** Every example module must follow the standard layout defined in [project.md](project.md#standard-module-layout). Do not deviate without discussion.
- **Consistent style across modules.** Test class structure, package names (`org.acme`), configuration patterns, and Maven plugin usage should look the same in every module.

## Dependency and version management

- **Always ask before adding a new dependency** — do not add libraries to any `pom.xml` without explicit approval.
- **Always ask before changing a library version** — version bumps must be discussed first, and must be applied to *all* modules simultaneously. Never update a version in one module while leaving others behind.
- **Prefer existing managed dependencies.** Check what is already declared before introducing something new.

## Build and test

- **Build tool:** Maven only (no Gradle).
- **Build command:** `mvn verify` (runs all tests including integration tests).
- **Test command:** `mvn test` (unit tests only).
- **Module-specific builds:** When changes are scoped to one module, run Maven from that module's directory.
- **Do not parallelise Maven** (`-T` flag) — modules can be resource-intensive and parallel builds are not safe here.
- **CI gate:** Every module must pass `mvn -B verify` in GitHub Actions. Do not merge changes that break CI.

## Git and PR workflow

- **Branch naming:**
  - `fix/<ISSUE_NUMBER>-<slug>` — bug fixes with a known issue
  - `feature/<ISSUE_NUMBER>-<slug>` — features with a known issue
  - `bugfix/<ISSUE_NUMBER>` — alternative bug fix prefix
  - `quick-fix/<slug>` — small fixes without an issue
  - `ci-issue/<slug>` — CI-related changes
- **Commit format:** `fix(#<N>): <description>` · `chore: <description>` · `ci: <description>`
- **Always create a PR** — direct pushes to `main` are not the norm.

## Documentation standards

- **Every module must have a `README.md`** that explains:
  - The Citrus capability or concept being demonstrated
  - Key objectives and what a reader will learn
  - Code snippets illustrating the most important patterns
  - Instructions for running the application and the tests
- **Balance code and prose.** Do not dump raw code without explanation; do not write walls of text without grounding them in code. Aim for a tutorial feel.
- **Keep README and code in sync.** If code changes, the README must be updated in the same PR. This is a known pain point — be deliberate about it.

## AI working preferences

### Before acting
- **Read the wiki first** (`.agents/wiki/index.md`) at the start of each task and follow relevant links.
- **Ask before adding dependencies or changing versions** — this is a hard rule, not a preference.
- **Clarify scope ambiguity** before making wide-reaching changes. When unsure whether a change should apply to one module or all modules, ask.

### While working
- **Minimal, targeted changes.** Do not refactor surrounding code that is unrelated to the task.
- **Explain key concepts.** When writing or editing README files, explain the *why* behind Citrus patterns, not just the *how*.
- **Preserve style.** Match the indentation, naming, and structure of the existing module closest to the one being worked on.
- **Suggest before doing** for large or structural changes (e.g. adding a new module, restructuring a POM, changing CI).

### After acting
- **Offer to update the wiki** if the task produced durable knowledge (new patterns, architecture decisions, version changes, new modules) — but wait for approval before writing.
- **Run validation.** After code changes, confirm the affected module builds and its tests pass (`mvn verify` in that module's directory).

## Communication preferences

- Be direct and technical. No filler phrases.
- When explaining Citrus/Quarkus concepts, assume the reader is a competent Java developer but may be new to Citrus.
- Highlight when something is a **known gap** (e.g. non-Java DSLs, centralised version management) rather than presenting partial coverage as complete.
