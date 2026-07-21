# Project Overview

## What this project is

`citrus-quarkus-examples` is a community-facing reference repository that demonstrates how to write **Citrus integration tests** for **Quarkus** applications. It serves two audiences equally:

- **Humans** — developers learning how to test Quarkus services with Citrus
- **AI agents** — a structured source of best practices and patterns to follow when generating or extending Citrus tests

Each sub-project is a self-contained, runnable example that showcases a specific Citrus capability or integration protocol. Together they form a living catalogue of howto guides.

## Main goals and objectives

1. Cover every significant Citrus capability and integration protocol with at least one working example.
2. Provide best-practice patterns for structuring Citrus tests on Quarkus — layout, configuration, assertions, test lifecycle.
3. Grow the catalogue continuously; new modules should be easy to add and immediately verified by CI.
4. Serve as a reliable reference that AI agents can read to generate correct, idiomatic Citrus tests.

## Key stakeholders and users

- **Citrus / Quarkus community** — primary audience; developers evaluating or adopting Citrus
- **AI coding agents** — consume this repo as a knowledge base for generating test code
- **Project maintainers** — keep examples current, consistent, and well-documented

## Technology stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Quarkus Platform | 3.36.2 |
| Citrus Framework | 5.0.0-M2 |
| Apache Camel (Quarkus) | 4.20.0 |
| Quarkus Artemis JMS | 3.12.1 |
| Maven | (wrapper per module) |
| Maven Compiler Plugin | 3.15.0 |
| Maven Surefire Plugin | 3.5.4 |

> **Version discipline:** All modules must use the same versions for shared libraries (Citrus, Quarkus, Camel, Maven plugins). Never update a version in one module without updating it everywhere.

## Repository structure

```
citrus-quarkus-examples/
├── pom.xml                  # Root aggregator POM (multi-module)
├── apache-camel/            # Aggregator for 15 Apache Camel sub-examples
│   ├── camel-direct/
│   ├── camel-direct-http/
│   ├── camel-rest-dsl/
│   ├── camel-openapi-server/
│   ├── camel-openapi-client/
│   ├── camel-file-inbox/
│   ├── camel-file-outbox/
│   ├── camel-jms/
│   ├── camel-kafka/
│   ├── camel-knative/
│   ├── camel-mqtt/
│   ├── camel-postgresql/
│   ├── camel-cxf-soap/
│   ├── camel-cxf-soap-client/
│   ├── camel-aws-s3-kafka/
│   └── camel-rest-to-soap/
├── event-driven-jms/        # JMS (ActiveMQ Artemis) event-driven example
└── event-driven-kafka/      # Kafka event-driven example
```

### Standard module layout

Every example module follows this layout:

```
<module>/
├── pom.xml
├── README.md                          # Concept explanation + code snippets
├── src/
│   ├── main/
│   │   ├── java/org/acme/            # Application code
│   │   ├── resources/
│   │   │   └── application.properties
│   │   └── docker/                   # Dockerfile variants (jvm, native, etc.)
│   └── test/
│       ├── java/org/acme/            # Citrus test classes
│       └── resources/
│           └── application.properties
└── mvnw / mvnw.cmd                   # Maven wrapper
```

## Current modules and what they demonstrate

### event-driven-jms
Word transformation pipeline (`words-in` queue → uppercase → `words-out` queue) using ActiveMQ Artemis and SmallRye Reactive Messaging. Demonstrates Citrus JMS endpoint testing with an embedded test broker.

### event-driven-kafka
Same word transformation pattern over Kafka topics, using `@Incoming` / `@Outgoing` reactive messaging. Demonstrates Citrus Kafka endpoint testing with a Quarkus dev-services broker.

### camel-openapi-client
Petstore API **client** built with the `camel-quarkus-rest-openapi` component. The Camel routes call an external HTTP service driven from an OpenAPI spec; Citrus acts as the simulated server. Demonstrates two complementary test styles in the same module:
- **`PetstoreOpenApiClientTest`** — explicit `http()` server actions with exact path/body/content-type validation and hand-crafted response payloads
- **`PetstoreOpenApiSpecTest`** — `openapi().server()` actions that derive request validation and response generation automatically from the spec

Key patterns introduced: `@CitrusConfiguration` shared endpoint config, `AfterSuite` server teardown, `camel().send().fork(true)` for deadlock-safe Camel route triggering, Quarkus `%test.` profile override for the service URL.

### camel-knative
Timer-driven Camel route that produces CloudEvents to a Knative eventing broker (`kamelet:timer-source` → `transformDataType("http:application-cloudevents")` → `knative:event/…`). Demonstrates Citrus Knative eventing tests without any Kubernetes cluster.

Key patterns:
- **`KnativeTestActionSupport`** — mixin interface that provides the `knative()` fluent DSL in Citrus tests.
- **`ClusterType.LOCAL`** — passed on `knative().brokers().create("default").clusterType(ClusterType.LOCAL)`. Citrus starts a local Jetty HTTP server (port 8080) as the broker instead of calling the Kubernetes API.
- **CloudEvent validation** — `knative().event().receive()` with `.attribute("ce-type", …)`, `.attribute("ce-source", …)`, `.attribute("ce-id", "@notNull()@")`, and `.eventData(…)`.
- **`transformDataType("http:application-cloudevents")`** — required in the Camel route to set `ce-source=org.apache.camel`; without it the source defaults to the Camel route ID.
- **`classpath:knative.json`** — use `classpath:` (not `file:`) for `camel.component.knative.environment-path` so the config resolves from the JAR in both tests and production.
- **`k.sink` property** — `knative.json` uses `{{k.sink}}` as the broker URL placeholder. The test profile sets `k.sink=http://localhost:8080`. In a real Kubernetes deployment the Knative operator injects `K_SINK` via a SinkBinding.
- **Extra dependencies needed for kamelets**: `camel-kamelets` (YAML definitions) and `camel-quarkus-yaml-dsl` (YAML DSL loader) must both be on the classpath; `camel-quarkus-kamelet` alone is not sufficient.

### apache-camel (sub-modules)
A growing set of examples each targeting a specific Apache Camel + Quarkus routing pattern and the corresponding Citrus test setup. Protocols covered include HTTP REST, SOAP/CXF, JMS, Kafka, Knative/CloudEvents, MQTT, file I/O, PostgreSQL, AWS S3, and OpenAPI (server and client).

## CI / CD

- **Workflow:** `.github/workflows/build.yml`
- **Trigger:** push and pull_request to `main`
- **Job:** `ubuntu-latest`, JDK 21 (Temurin), `mvn -B verify`
- **Policy:** Every module must build and pass all tests in CI. New modules must be wired into the root aggregator POM so they are picked up automatically.

## Known gaps

- **Citrus test DSLs:** Only the **Java DSL** (JUnit Jupiter) is currently demonstrated. Citrus also supports YAML, XML, Groovy, and Cucumber DSLs — none of these are covered yet. This is an open issue: future modules or a dedicated section should demonstrate each DSL to give users and AI agents full coverage.
- **Version centralisation:** Versions are currently managed per-module rather than in a single root BOM. Keeping them in sync across modules is a manual, error-prone process and a known pain point.

## Roadmap themes

- Add examples for remaining Citrus capabilities (HTTP REST standalone, SOAP standalone, gRPC, FTP/SFTP, TCP/IP, database validation, etc.)
- Introduce additional DSL examples (YAML, XML, Groovy, Cucumber)
- Centralise dependency version management in the root POM
