# Apache Camel Kafka Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes in Quarkus applications using Citrus framework with Kafka integration. The project showcases a simple Camel route that transforms incoming Kafka messages to uppercase and publishes them to an output topic.

## What You'll Learn

By the end of this guide, you'll understand:

- How to create Apache Camel routes in Quarkus for Kafka message processing
- How to configure Citrus endpoints for testing Camel Kafka routes
- How to write integration tests for Camel-based applications
- How to share the Kafka bootstrap servers between Quarkus, Camel, and Citrus using `@BindToRegistry`
- How to use Quarkus test resources to automatically provision a Kafka broker
- The Gherkin-style test DSL (Given-When-Then) in Citrus for testing Camel routes
- How Apache Camel simplifies integration patterns in Quarkus

## The Application Under Test

The Quarkus application uses Apache Camel to implement a simple integration route with Kafka:

```
Kafka Topic (words-in) → Apache Camel Route (Transform) → Kafka Topic (words-out)
```

### Apache Camel Route

The application consists of a single, elegant Camel route defined in `Routes.java`:

```java
public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(kafka("words-in").autoOffsetReset("earliest"))
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to(kafka("words-out"));
    }
}
```

**Route Breakdown:**

1. **from(kafka("words-in").autoOffsetReset("earliest"))**: Camel consumer that listens to the `words-in` Kafka topic
   - Uses the Endpoint DSL for type-safe route configuration
   - `.autoOffsetReset("earliest")` ensures the consumer reads from the beginning of the topic
2. **.setBody(...)**: Transformation step that:
   - Retrieves the message body from the exchange
   - Converts it to uppercase
   - Prefixes it with `">> "`
3. **to(kafka("words-out"))**: Camel producer that sends the transformed message to the `words-out` Kafka topic

### Why Apache Camel?

Apache Camel provides:

- **Concise Route Definition**: Complex integration flows in minimal code
- **Rich Component Library**: 300+ connectors for various protocols and systems
- **Enterprise Integration Patterns (EIP)**: Built-in implementations of proven patterns
- **Routing & Mediation**: Message routing, transformation, and orchestration
- **Testing Support**: Integration-friendly testing with Citrus

In this example, the entire integration flow (consume → transform → produce) is expressed in just 3 lines of fluent API code, compared to the multi-class approach required with raw reactive messaging.

### Camel Kafka Component Configuration

Quarkus automatically configures the Camel Kafka component to use the Kafka bootstrap servers. The `kafka:` prefix in the route definition (`from("kafka:words-in")`) references this pre-configured component, which:

- Uses the Kafka bootstrap servers configured in application.properties
- Supports automatic reconnection and error handling
- Integrates seamlessly with Camel's error handling
- Requires no additional configuration code

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to verify the end-to-end Camel route flow using Citrus framework.

### Test Setup: Kafka Container and Topic Creation

The test uses Citrus Testcontainers support to provision a Kafka broker and create topics:

```java
@KafkaContainerSupport(
    port = 9092, 
    version = "4.2.0", 
    containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {

    public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
        @Override
        public Map<String, String> started(KafkaContainer container) {
            try (Admin adminClient = Admin.create(...)) {
                adminClient.createTopics(Set.of(
                    new NewTopic("words-in", 1, (short) 1),
                    new NewTopic("words-out", 1, (short) 1)
                ));
            }
            return Collections.emptyMap();
        }
    }
}
```

**What's happening here:**

- `@KafkaContainerSupport`: Citrus annotation that starts a Kafka testcontainer with the specified version
- `containerLifecycleListener`: Custom listener that runs after the container starts
- `KafkaConfigurer`: Creates the required Kafka topics (`words-in` and `words-out`) before tests run
- The Kafka bootstrap servers are automatically configured and shared between:
  - Quarkus for application configuration
  - Apache Camel for the `kafka:` component  
  - Citrus for test endpoints
- This ensures all three frameworks communicate with the same Kafka broker instance

### Test Setup: Endpoint Configuration

Citrus uses annotations to declaratively configure Kafka endpoints:

```java
@CitrusEndpoint
@KafkaEndpointConfig(topic = "words-in")
KafkaEndpoint wordsIn;

@CitrusEndpoint
@KafkaEndpointConfig(topic = "words-out")
KafkaEndpoint wordsOut;
```

**What's happening here:**

- `@CitrusEndpoint`: Marks fields as Citrus test endpoints that will be automatically configured
- `@KafkaEndpointConfig`: Specifies the Kafka topic name
- The bootstrap servers are automatically resolved from the Citrus registry (registered via `@BindToRegistry`)
- Two endpoints are defined: one for sending messages to the Camel route (`wordsIn`) and one for receiving results (`wordsOut`)

### Test Annotations

The test class uses several key annotations:

```java
@QuarkusTest
@CitrusSupport
@KafkaContainerSupport(
    port = 9092, 
    version = "4.2.0", 
    containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {
    // ...
}
```

**What's happening here:**

- `@QuarkusTest`: Starts the Quarkus application (including Camel routes) in test mode
- `@CitrusSupport`: Enables Citrus framework integration with Quarkus
- `@KafkaContainerSupport`: Automatically starts a Kafka testcontainer (version 4.2.0) for testing
- `containerLifecycleListener`: Specifies a custom listener to run after container startup (used to create topics)
- No manual broker setup required
- Automatic cleanup after test execution

### Test Execution: Gherkin-Style DSL

The actual test uses Citrus's Gherkin-style test runner for readable Given-When-Then syntax:

```java
@Test
void shouldHandleEvents() {
    runner.when(
        send()
            .endpoint(wordsIn)
            .message()
            .body("Howdy")
    );

    runner.then(
        receive()
            .endpoint(wordsOut)
            .message()
            .body(">> HOWDY")
    );
}
```

**Test Flow Breakdown:**

1. **WHEN**: Send a message with body `"Howdy"` to the `words-in` topic
2. The Camel route automatically consumes the message from `words-in`
3. The route transforms the message to `">> HOWDY"`
4. The route produces the message to the `words-out` topic
5. **THEN**: Verify that a message with body `">> HOWDY"` is received from the `words-out` topic

The test validates:
- The Camel route successfully consumes messages from the Kafka topic
- The transformation logic (uppercase conversion + prefix) works correctly
- The Camel route successfully produces messages to the output topic
- End-to-end integration between Kafka, Camel, and the application

## Key Testing Concepts

### 1. Integration with Citrus Testcontainers Kafka Support

The test uses Citrus's built-in Kafka testcontainer support via the `@KafkaContainerSupport` annotation:

This ensures:
- No manual Kafka broker setup required
- Tests run against a real Kafka broker (Apache Kafka 4.2.0 via Testcontainers)
- Automatic topic creation via container lifecycle listener
- Automatic cleanup after test execution
- Isolation between test runs
- Bootstrap servers automatically configured for Quarkus, Camel, and Citrus

### 2. Citrus Kafka Module

The `citrus-kafka` Maven dependency is added to the Maven POM:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-kafka</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module adds:
- Kafka endpoint implementations
- Message producer and consumer capabilities
- Kafka message header and property validation
- Integration with Citrus test framework

### 3. Apache Camel Quarkus Dependencies

The application uses Camel Quarkus extensions:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-kafka</artifactId>
</dependency>
```

This provides:
- Apache Camel Kafka component for Quarkus
- Automatic configuration of Camel with Quarkus CDI
- Native image support for Camel routes
- Integration with Quarkus lifecycle management

The `quarkus-camel-bom` in dependency management ensures compatible versions of all Camel Quarkus extensions.

### 4. Container Lifecycle Listener

The test uses a custom container lifecycle listener to create Kafka topics:

```java
public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
    @Override
    public Map<String, String> started(KafkaContainer container) {
        try (Admin adminClient = Admin.create(
                Collections.singletonMap(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, 
                    container.getBootstrapServers()))) {
            
            CreateTopicsResult result = adminClient.createTopics(Set.of(
                new NewTopic("words-in", 1, (short) 1),
                new NewTopic("words-out", 1, (short) 1)
            ));
            
            result.all().get();
        }
        return Collections.emptyMap();
    }
}
```

**What's happening here:**

- `ContainerLifecycleListener<KafkaContainer>`: Interface that allows custom logic after container startup
- `started()` method: Called after the Kafka container has started
- Uses Kafka Admin client to create the required topics programmatically
- Creates both `words-in` and `words-out` topics with 1 partition and replication factor of 1
- This ensures topics exist before the Camel routes and Citrus tests start
- Returns empty map (no additional system properties needed)

### 5. Apache Camel Endpoint DSL

The route uses Camel's type-safe Endpoint DSL:

- `EndpointRouteBuilder` instead of `RouteBuilder` enables the endpoint DSL
- `kafka("words-in")` provides type-safe access to Kafka endpoint configuration
- `.autoOffsetReset("earliest")` ensures the consumer reads from the beginning
- Eliminates string-based URIs and provides IDE auto-completion
- Any class extending `EndpointRouteBuilder` or `RouteBuilder` in the classpath is automatically discovered by Quarkus
- Routes are started when the Quarkus application starts
- In test mode, routes are fully functional and process real messages

### 6. Kafka Configuration

The application configuration for production:

**Production** (`application.properties`):
```properties
kafka.bootstrap.servers=localhost:9092
```

The test configuration uses `@KafkaContainerSupport` which automatically configures the bootstrap servers to point to the test Kafka broker. No test-specific configuration is needed - the annotation handles everything.

### 7. Synchronous Testing of Asynchronous Camel Routes

While Camel routes process messages asynchronously, the Citrus test runs synchronously:

1. The `send()` action publishes a message to the Kafka topic
2. Camel asynchronously consumes, transforms, and produces the message
3. The `receive()` action blocks until a message arrives on the output topic (or timeout occurs)
4. This approach ensures deterministic test execution and proper verification

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw clean test
```

**What happens during test execution:**

1. `@KafkaContainerSupport` starts a Kafka testcontainer (Apache Kafka 4.2.0)
2. The `KafkaConfigurer` lifecycle listener creates the `words-in` and `words-out` topics
3. Quarkus starts the application in test mode with bootstrap servers pointing to the test Kafka
4. Apache Camel routes are discovered and started
5. The Camel Kafka consumer starts reading from the `words-in` topic (from earliest offset)
6. Citrus Kafka endpoints are configured with the same bootstrap servers
7. The test sends a message with body `"Howdy"` to the `words-in` topic
8. The Camel route consumes, transforms (uppercase + prefix), and produces the message to `words-out` topic
9. Citrus receives and validates the output message matches `">> HOWDY"`
10. The test completes, and the Kafka container is automatically stopped and cleaned up

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-kafka**: Apache Camel Kafka component for Quarkus
- **quarkus-messaging-kafka**: Quarkus Kafka client and configuration support
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-kafka**: Adds Kafka endpoint support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

Note: The Kafka testcontainer support is provided by Citrus through the `@KafkaContainerSupport` annotation, so no separate testcontainer dependency is required.

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Apache Camel Quarkus Kafka Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/kafka.html) - Camel Kafka component reference
- [Apache Camel Quarkus Core Guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html) - Camel core functionality
- [Quarkus Kafka Guide](https://quarkus.io/guides/kafka) - Getting started with Kafka in Quarkus
- [Citrus Kafka Module](https://citrusframework.org/docs/endpoints/kafka/) - Kafka endpoint reference
- [Apache Camel Documentation](https://camel.apache.org/) - Official Camel documentation

---

**Next Steps**: Try adding more complex Camel patterns like content-based routing, message filters, or aggregation. Explore combining Camel with other Citrus endpoints (HTTP, file system) to test multi-protocol integration scenarios. Learn about Camel's error handling and retry mechanisms and how to test them with Citrus.
