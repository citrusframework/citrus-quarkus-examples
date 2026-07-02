package org.acme;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kafka.config.annotation.KafkaEndpointConfig;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.quarkus.LocalStackContainerSupport;
import org.citrusframework.testcontainers.kafka.quarkus.apache.KafkaContainerSupport;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.kafka.KafkaContainer;
import software.amazon.awssdk.services.s3.S3Client;

@QuarkusTest
@CitrusSupport
@LocalStackContainerSupport(services = AwsService.S3,
        containerLifecycleListener = QuarkusApplicationTest.LocalStackConfigurer.class)
@KafkaContainerSupport(port = 9092, version = "4.2.0",
        containerLifecycleListener = QuarkusApplicationTest.KafkaConfigurer.class)
class QuarkusApplicationTest implements TestActionSupport {

    private static final Logger log = LoggerFactory.getLogger(QuarkusApplicationTest.class);

    private static final String BUCKET_NAME = "citrus-camel-demo";

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "s3-events")
    KafkaEndpoint s3Events;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldSplitS3FileToKafkaEvents() {
        runner.when(
            camel()
                .send()
                .endpoint("aws2-s3://" + BUCKET_NAME + "?" +
                        "overrideEndpoint=true&" +
                        "forcePathStyle=true&" +
                        "uriEndpointOverride={{aws.s3.uriEndpointOverride}}&" +
                        "accessKey={{aws.s3.accessKey}}&" +
                        "secretKey={{aws.s3.secretKey}}&" +
                        "region={{aws.s3.region}}")
                .message()
                .fork(true)
                .body("Hello Camel!\nHello Citrus!\nHello Quarkus!")
                .header("CamelAwsS3Key", "hello.txt")
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Camel!" }
                    """)
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Citrus!" }
                    """)
        );

        runner.then(
            receive()
                .endpoint(s3Events)
                .message()
                .body("""
                    { "message": "Hello Quarkus!" }
                    """)
        );
    }

    public static class LocalStackConfigurer implements ContainerLifecycleListener<LocalStackContainer> {
        @Override
        public Map<String, String> started(LocalStackContainer container) {
            String serviceEndpoint = container.getServiceEndpoint().toString();
            log.info("LocalStack started at: {}", serviceEndpoint);

            S3Client s3Client = container.getClient(AwsService.S3);
            s3Client.createBucket(builder -> builder.bucket(BUCKET_NAME));
            log.info("Successfully created S3 bucket: {}", BUCKET_NAME);

            return Map.of(
                    "aws.s3.bucketNameOrArn", BUCKET_NAME,
                    "aws.s3.uriEndpointOverride", serviceEndpoint,
                    "aws.s3.accessKey", container.getAccessKey(),
                    "aws.s3.secretKey", container.getSecretKey(),
                    "aws.s3.region", container.getRegion()
            );
        }
    }

    public static class KafkaConfigurer implements ContainerLifecycleListener<KafkaContainer> {
        @Override
        public Map<String, String> started(KafkaContainer container) {
            try (Admin adminClient = Admin.create(Collections.singletonMap(
                    AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, container.getBootstrapServers()))) {
                CreateTopicsResult result = adminClient.createTopics(Set.of(
                    new NewTopic("s3-events", 1, (short) 1)
                ));

                result.all().get();

                adminClient.listTopics().names().get()
                        .forEach(topic -> log.info("Successfully created topic: {}", topic));
            } catch (ExecutionException | InterruptedException e) {
                throw new CitrusRuntimeException("Failed to create topics", e);
            }

            return Collections.emptyMap();
        }
    }
}
