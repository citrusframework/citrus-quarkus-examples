# Citrus Quarkus Examples

This repository is a collection of example projects showing how Citrus helps to perform fresh integration testing for Quarkus applications.

Each folder represents a separate Quarkus project that has automated Citrus tests to verify the application.
The Citrus tests can be run as arbitrary JUnit Jupiter tests both via Maven CLI and your favorite Java IDE (e.g. IntelliJ, Eclipse, VSCode).

Navigate to a subfolder and follow these steps to run the Quarkus application and its integration tests:

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _uber-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _uber-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _uber-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Running the Citrus tests

You can run the integration tests as part of the Maven build using:

```shell script
./mvnw verify
```

In your favorite IDE open a test class provided in `src/test/java` and run the test directly from the IDE. 
This will automatically start the Quarkus application as part of the test.

## Related Guides

- If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>
- Citrus ([guide](https://github.com/christophd/citrus-demo-quarkus)): Add Citrus support to your Quarkus tests. Citrus is an Open Source Java integration testing framework supporting a wide range of message protocols and data formats (Kafka, Http REST, JMS, TCP/IP, SOAP, FTP/SFTP, XML, Json, and more)
- Apache Camel Quarkus ([guide](https://quarkus.io/guides/camel)): Connect Quarkus with Apache Camel
