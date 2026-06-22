# Apache Camel CXF SOAP WebService Integration Testing with Citrus

This example demonstrates how to test a SOAP WebService implemented via Apache Camel CXF using the Citrus framework. The service exposes a FruitService SOAP API defined in a WSDL contract. Citrus acts as a SOAP client to invoke operations and validate the SOAP response messages.

## What You'll Learn

By the end of this guide, you'll understand:

- How to implement a SOAP WebService using the Apache Camel CXF component in Quarkus
- How to define a WSDL-based service contract with document/literal style
- How to route SOAP operations to business logic using Camel's content-based router
- How to use Citrus SOAP client to test WebService endpoints
- How to validate XML response messages with Citrus
- How to test multiple SOAP operations (addFruit, deleteFruit, listFruits)

## The Application Under Test

The Quarkus application uses the Apache Camel CXF SOAP component to expose a FruitService WebService:

```
POST /soap/fruits  (SOAP)
  - addFruit     -> Adds a fruit and returns the updated list
  - deleteFruit  -> Deletes a fruit and returns the updated list
  - listFruits   -> Returns the current list of all fruits
```

### WSDL Contract

The service contract is defined in `src/main/resources/wsdl/FruitService.wsdl` with the target namespace `http://server.it.cxf.quarkiverse.io/`. It defines:

- **Fruit type**: An object with `name` and `description` string properties
- **addFruit**: Accepts a `Fruit` parameter and returns a list of all fruits
- **deleteFruit**: Accepts a `Fruit` parameter and returns the remaining fruits
- **listFruits**: Returns all fruits in the service

### Service Endpoint Interface (SEI)

The `FruitService` interface defines the JAX-WS annotated service contract:

```java
@WebService(
        targetNamespace = "http://server.it.cxf.quarkiverse.io/",
        name = "FruitService"
)
public interface FruitService {
    List<Fruit> addFruit(Fruit fruit);
    List<Fruit> deleteFruit(Fruit fruit);
    List<Fruit> listFruits();
}
```

### Apache Camel CXF Route

The `FruitServiceRoutes` class configures the CXF SOAP endpoint and routes operations:

```java
from("cxf:/fruits?serviceClass=org.acme.FruitService&dataFormat=PAYLOAD")
        .choice()
            .when(header(CxfConstants.OPERATION_NAME).isEqualTo("addFruit"))
                .process(/* extract fruit from XML, add to list, build XML response */)
            .when(header(CxfConstants.OPERATION_NAME).isEqualTo("deleteFruit"))
                .process(/* extract fruit from XML, remove from list, build XML response */)
            .when(header(CxfConstants.OPERATION_NAME).isEqualTo("listFruits"))
                .process(/* build XML response with all fruits */);
```

Key aspects:
- **CXF endpoint** (`cxf:/fruits`) exposes the SOAP service under the CXF servlet path
- **PAYLOAD data format** works with the SOAP body as XML DOM elements, giving full control over request/response XML
- **Content-based routing** uses the `operationName` header to dispatch to the correct handler

### Understanding the Architecture

**1. CXF SOAP Component**: The Camel CXF component integrates Apache CXF with Camel routing. It handles SOAP envelope processing and endpoint management.

**2. PAYLOAD Data Format**: In PAYLOAD mode, the exchange body contains the SOAP body content as XML DOM elements. The route parses incoming XML to extract data and constructs XML response documents directly.

**3. Operation Dispatching**: The `CxfConstants.OPERATION_NAME` header identifies which WSDL operation was invoked. The Camel `choice()` routes each operation to its handler.

**4. In-Memory Store**: Fruits are stored in a `LinkedList` seeded with Apple and Orange as initial data.

## Understanding the Citrus Test

The test class `FruitSoapServiceTest` demonstrates how to test Camel CXF SOAP endpoints using Citrus's WebService client.

### Test Setup

```java
@QuarkusTest
@CitrusSupport
class FruitSoapServiceTest {

    @CitrusEndpoint
    @WebServiceClientConfig(defaultUri = "http://localhost:8081/soap/fruits")
    WebServiceClient soapClient;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

- `@QuarkusTest`: Starts the Quarkus application with CXF routes on the test port (8081)
- `@CitrusSupport`: Enables Citrus framework integration
- `@CitrusEndpoint` + `@WebServiceClientConfig`: Configures a Citrus SOAP client pointing to the CXF endpoint
- `@CitrusResource`: Injects the Citrus test action runner

### Test: List All Fruits

```java
@Test
void shouldListFruits() {
    runner.when(
            soap().client(soapClient)
                    .send()
                    .message()
                    .soapAction("")
                    .body("<ns:listFruits xmlns:ns=\"...\"/>")
    );

    runner.then(
            soap().client(soapClient)
                    .receive()
                    .message()
                    .body("<ns:listFruitsResponse xmlns:ns=\"...\">" +
                            "<return>...</return>" +
                            "</ns:listFruitsResponse>")
    );
}
```

Sends a `listFruits` SOAP request and validates the response contains the initial fruits (Apple and Orange).

### Test: Add a Fruit

```java
@Test
void shouldAddFruit() {
    runner.when(
            soap().client(soapClient)
                    .send()
                    .message()
                    .soapAction("")
                    .body("<ns:addFruit xmlns:ns=\"...\">" +
                            "<fruit><description>Tropical fruit</description><name>Mango</name></fruit>" +
                            "</ns:addFruit>")
    );

    runner.then(
            soap().client(soapClient)
                    .receive()
                    .message()
                    .body("<ns:addFruitResponse xmlns:ns=\"...\">" +
                            "<return>...</return>" +
                            "</ns:addFruitResponse>")
    );
}
```

Sends an `addFruit` SOAP request with a Mango fruit and validates the response contains all three fruits.

### Test: Delete a Fruit

```java
@Test
void shouldDeleteFruit() {
    runner.when(
            soap().client(soapClient)
                    .send()
                    .message()
                    .soapAction("")
                    .body("<ns:deleteFruit xmlns:ns=\"...\">" +
                            "<fruit>...</fruit>" +
                            "</ns:deleteFruit>")
    );

    runner.then(
            soap().client(soapClient)
                    .receive()
                    .message()
                    .body("<ns:deleteFruitResponse xmlns:ns=\"...\">" +
                            "<return>...</return>" +
                            "</ns:deleteFruitResponse>")
    );
}
```

Sends a `deleteFruit` SOAP request to remove Apple and validates the response contains only Orange.

## Key Testing Concepts

### 1. Citrus SOAP Client

Citrus provides a fluent DSL for SOAP client operations:

```java
@CitrusEndpoint
@WebServiceClientConfig(defaultUri = "http://localhost:8081/soap/fruits")
WebServiceClient soapClient;
```

The client handles SOAP envelope wrapping/unwrapping automatically. You provide only the SOAP body content.

### 2. XML Body Validation

With the `citrus-validation-xml` dependency, Citrus validates XML responses structurally. The validation ensures that the SOAP response body matches the expected XML content.

### 3. SOAP Action

Each test specifies a `soapAction("")` matching the WSDL binding. The SOAP action is sent as an HTTP header to identify the operation.

### 4. Test Annotations

- `@QuarkusTest`: Starts the application in test mode on port 8081
- `@CitrusSupport`: Activates Citrus integration with the Quarkus test lifecycle

## Running the Tests

Execute the tests using Maven:

```bash
cd apache-camel/camel-cxf-soap
mvn verify
```

**What happens during test execution:**

1. Quarkus starts the application in test mode (port 8081)
2. Apache Camel CXF SOAP endpoint is deployed
3. Citrus SOAP client connects to the CXF service endpoint
4. Each test sends SOAP requests and validates the XML responses
5. Application shuts down after tests complete

**Expected output:**
```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality for Quarkus
- **camel-quarkus-cxf-soap**: Apache Camel CXF SOAP component for Quarkus
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-ws**: Adds SOAP WebService client/server support to Citrus
- **citrus-validation-xml**: XML-aware message validation
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/citrus/reference/html/)
- [Citrus SOAP WebService Module](https://citrusframework.org/docs/endpoints/soap/) - SOAP endpoint reference for Citrus
- [Apache Camel CXF Component](https://camel.apache.org/components/latest/cxf-component.html) - Official Camel CXF documentation
- [Quarkus CXF Extension](https://docs.quarkiverse.io/quarkus-cxf/dev/) - Quarkiverse CXF extension for Quarkus
- [Apache CXF](https://cxf.apache.org/) - Apache CXF project
