package org.acme;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.dsl.CamelSupport;
import org.citrusframework.kafka.config.annotation.KafkaEndpointConfig;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-out",
            server = "${kafka.bootstrap.servers}")
    KafkaEndpoint wordsOut;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldConsumeFileContent() {
        runner.when(
            camel()
                .send()
                .endpoint(CamelSupport.camel().endpoints().file("inbox")::getRawUri)
                .message()
                .header("CamelFileName", "words")
                .body("Hello World")
                .transform(processor().camel(camelContext)
                        .marshal()
                        .zipFile())
        );

        runner.then(
            receive()
                .endpoint(wordsOut)
                .message()
                .body(">> HELLO")
        );

        runner.then(
            receive()
                .endpoint(wordsOut)
                .message()
                .body(">> WORLD")
        );
    }
}
