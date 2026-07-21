# Apache Camel Knative Eventing with Citrus

This example demonstrates how to test an Apache Camel route that produces CloudEvents to a Knative eventing broker, running entirely on a local machine — no Kubernetes cluster required.

## What You'll Learn

By the end of this guide, you'll understand:

- **Citrus local Knative broker**: How Citrus starts a lightweight HTTP server that acts as a Knative broker, making Knative eventing tests possible without any Kubernetes infrastructure
- **CloudEvent verification**: How to assert CloudEvent headers (`ce-id`, `ce-type`, `ce-source`) and event data with Citrus
- **Camel Knative integration**: How the Camel Knative producer component formats and sends CloudEvents
- **`ClusterType.LOCAL`**: The Citrus concept that switches Knative operations from Kubernetes API calls to local HTTP server management

## The Application Under Test

The Quarkus application uses Apache Camel to produce CloudEvents on a timer and send them to a Knative broker:

```
Timer (kamelet:timer-source) → CloudEvent transform → Knative broker (HTTP POST)
```

### Apache Camel Route

The route is defined in [`Routes.java`](src/main/java/org/acme/Routes.java):

```java
from("kamelet:timer-source?period=5000&message={{timer.message:Hello Knative!}}")
        .transformDataType(new DataType("http:application-cloudevents"))
        .to("knative:event/org.apache.camel.event.messages?kind=Broker&name=default");
```

**Route breakdown:**

1. **`kamelet:timer-source`**: Fires a message every 5 seconds. The message body is configurable via the `timer.message` property.
2. **`transformDataType("http:application-cloudevents")`**: Transforms the exchange into a proper CloudEvent, setting the mandatory CloudEvent attributes (`ce-type`, `ce-source`, `ce-specversion`) from Camel's routing metadata.
3. **`knative:event/org.apache.camel.event.messages`**: Sends the CloudEvent as an HTTP POST to the configured Knative broker. The event type `org.apache.camel.event.messages` becomes the `ce-type` header.

### Camel Knative Configuration

The Camel Knative component reads a JSON environment file that describes the Knative services. This is configured in [`application.properties`](src/main/resources/application.properties):

```properties
camel.component.knative.environment-path=classpath:knative.json
```

The [`knative.json`](src/main/resources/knative.json) file describes the broker sink, using `{{k.sink}}` as a placeholder for the broker URL:

```json
{
  "services": [
    {
      "type": "event",
      "name": "default",
      "url": "{{k.sink}}",
      "endpointKind": "sink",
      "objectApiVersion": "eventing.knative.dev/v1",
      "objectKind": "Broker",
      "objectName": "default"
    }
  ]
}
```

The `{{k.sink}}` placeholder follows the Knative [Sink Binding](https://knative.dev/docs/eventing/custom-event-source/sinkbinding/) convention — the broker URL is injected via the `k.sink` property. In the test profile ([`src/test/resources/application.properties`](src/test/resources/application.properties)), this is set to the Citrus local broker:

```properties
k.sink=http://localhost:8080
```

In a real Kubernetes deployment, the Knative operator would inject `K_SINK` automatically via a SinkBinding resource.

## Understanding the Citrus Test

The test class [`QuarkusApplicationTest`](src/test/java/org/acme/QuarkusApplicationTest.java) demonstrates how to verify CloudEvents end-to-end without a Kubernetes cluster.

### Key Annotations

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest implements TestActionSupport, KnativeTestActionSupport {
```

- `@QuarkusTest`: Starts the full Quarkus application (Camel routes included) in test mode.
- `@CitrusSupport`: Enables the Citrus framework integration with the Quarkus test lifecycle.
- `KnativeTestActionSupport`: Provides the `knative()` fluent DSL method for Knative test actions.

### Creating a Local Knative Broker

```java
runner.given(
    knative()
        .brokers()
        .create("default")
        .clusterType(ClusterType.LOCAL)
);
```

`ClusterType.LOCAL` is the key detail. When Citrus encounters this, instead of making Kubernetes API calls to create a real Knative Broker resource, it:

1. Starts a local Jetty HTTP server on port 8080.
2. Registers it under the name `"default"` in the Citrus context.
3. Stores the broker port as a test variable so the `receive()` action knows which server to read from.

This local HTTP server listens for the CloudEvent HTTP POST requests that the Camel route sends to `http://localhost:8080`.

### Receiving and Verifying CloudEvents

```java
runner.then(
    knative()
        .event()
        .receive()
        .serviceName("default")
        .eventData("${timer.message}")
        .attribute("ce-id", "@notNull()@")
        .attribute("ce-type", "org.apache.camel.event.messages")
        .attribute("ce-source", "org.apache.camel")
        .attribute("Content-Type", "text/plain")
);
```

The `receive()` action waits on the local HTTP server for an incoming CloudEvent and validates:

- **`eventData`**: The message body must equal the value of the `timer.message` test variable (`"Hello Knative!"`).
- **`ce-id`**: Must be present and non-null (Camel generates a UUID per event).
- **`ce-type`**: Must be `"org.apache.camel.event.messages"`, matching the Knative endpoint path.
- **`ce-source`**: Must be `"org.apache.camel"`, set by the `transformDataType` step.
- **`Content-Type`**: Must be `"text/plain"` as produced by the timer source.

### Test Flow

```
1. Citrus sets variable: timer.message = "Hello Knative!"
2. Citrus starts local HTTP server on :8080 (the "default" Knative broker)
3. Quarkus starts the Camel route (timer fires every 5s)
4. Camel posts a CloudEvent to http://localhost:8080
5. Citrus receives the HTTP POST and validates headers + body
6. Test passes — Citrus sends HTTP 202 Accepted back to Camel
```

## Running the Tests

```bash
./mvnw verify
```

**Expected output:**

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

As with any JUnit Jupiter test, you can also run it directly from your IDE.

## Key Dependencies

```xml
<!-- Camel Knative producer for Quarkus -->
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-knative-producer</artifactId>
</dependency>

<!-- Kamelet support + timer-source kamelet definitions -->
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-kamelet</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.kamelets</groupId>
    <artifactId>camel-kamelets</artifactId>
</dependency>

<!-- YAML DSL needed to load .kamelet.yaml definitions at runtime -->
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-yaml-dsl</artifactId>
</dependency>

<!-- Citrus Knative test support -->
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-knative</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Knative Eventing](https://knative.dev/docs/eventing/) — Knative event-driven architecture concepts
- [Apache Camel Knative Component](https://camel.apache.org/components/latest/knative-component.html) — Camel Knative component reference
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/) — Pre-built Camel source/sink connectors
- [Knative SinkBinding](https://knative.dev/docs/eventing/custom-event-source/sinkbinding/) — How `K_SINK` is injected in real Kubernetes deployments
- [CloudEvents Specification](https://cloudevents.io/) — CloudEvents standard

---

**Next Steps**: Extend the test to verify multiple CloudEvent types using trigger filters, or add a consumer side to see how Citrus can also simulate a Knative subscriber receiving events from the broker.
