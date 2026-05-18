# Citrus Quarkus Examples

This repository is a collection of example projects showing how Citrus helps to perform fresh integration testing for Quarkus applications.

Each folder represents a separate Quarkus project that has automated Citrus tests to verify the application.
The Citrus tests can be run as arbitrary JUnit Jupiter tests both via Maven CLI and your favorite Java IDE (e.g. IntelliJ, Eclipse, VSCode).

Navigate to a subfolder and run this common CLI Maven command for building and running all available tests:

```shell script
./mvnw verify
```

You may also use Quarkus dev mode to start the application with an awesome continuous testing experience.

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.
