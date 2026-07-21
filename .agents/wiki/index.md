# Wiki — Citrus Quarkus Examples

Concise reference for humans and AI agents working on this repository.
Read this file first; follow links only when relevant to your task.

## Files

| File | Contents |
|------|----------|
| [project.md](project.md) | What this project is, goals, modules, tech stack, CI |
| [preferences.md](preferences.md) | Working standards, code conventions, AI interaction rules |

## Quick facts

- **Repo:** `citrusframework/citrus-quarkus-examples`
- **Purpose:** Community demo and best-practice reference for writing Citrus integration tests on top of Quarkus
- **Build:** `mvn verify` (runs all tests)
- **Fast Build:** `mvn verify -DskipTests` (skips all tests)
- **CI:** GitHub Actions on every push/PR to `main`
- **Key versions:** Java 21 · Apache Camel 4.20.0 · Quarkus 3.36.2 · Citrus 5.0.0-M2
- **Current modules:** `apache-camel` (14 sub-examples) · `event-driven-jms` · `event-driven-kafka`
- **Test DSL in use:** Java (JUnit Jupiter); YAML / XML / Groovy / Cucumber DSLs are not yet covered — see [project.md](project.md#known-gaps)
