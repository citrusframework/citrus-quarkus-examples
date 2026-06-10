# Project Standards

This file documents the build tools, commands, and code conventions used by the Citrus Quarkus Examples project.

- **Build tool:** Maven
- **Build command:** `mvn verify`
- **Test command:** `mvn test`
- **Test with coverage command:** `mvn verify`
- **Format command:** `mvn process-sources -Pformat`
- **Module-specific build:** yes (multi-module Maven project)
- **Parallelized Maven:** no
- **Code style restrictions:**
  - Do NOT use Lombok (unless already present in the file)
  - Records are allowed for internal/non-API classes; do NOT convert existing public API classes to Records
  - Do NOT change public API signatures without justification
  - Do NOT add new dependencies without justification
  - Follow standard Java code conventions
  - Use Maven code formatting profile if available
  - Maintain backwards compatibility for public APIs
  - Maintain consistency with existing code style in the project

## Version
941f5393a52f28ce7c8beb19c406c3b270d03342
