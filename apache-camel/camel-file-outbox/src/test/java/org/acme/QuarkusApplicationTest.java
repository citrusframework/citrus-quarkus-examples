package org.acme;

import java.util.Arrays;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.camel.dsl.CamelSupport;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldAggregateFileContent() {
        runner.when(
            Arrays.asList(
                send()
                    .endpoint("camel:direct:tasks")
                    .message()
                    .body("Doctor's appointment 9:00am"),
                send()
                    .endpoint("camel:direct:tasks")
                    .message()
                    .body("Fetch kids from school"),
                send()
                    .endpoint("camel:direct:tasks")
                    .message()
                    .body("Plan next vacation in June")
            ) // send three messages that should be aggregated
        );

        runner.then(
            camel()
                .receive()
                .endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
                .process(processor().camel(camelContext).convertBodyTo(String.class))
                .message()
                .header("CamelFileName", "tasks.txt") // validates the file name
                .body("""
                Doctor's appointment 9:00am
                Fetch kids from school
                Plan next vacation in June
                """) // validate the aggregated message body
        );
    }
}
