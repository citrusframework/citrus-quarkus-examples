package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.knative.KnativeTestActionSupport;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariables;

@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest implements TestActionSupport, KnativeTestActionSupport {

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldProduceCloudEvents() {
        runner.given(
            createVariables()
                .variable("timer.message", "Hello Knative!")
        );

        runner.given(
            knative()
                .brokers()
                .create("default")
                .clusterType(ClusterType.LOCAL)
        );

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
    }
}
