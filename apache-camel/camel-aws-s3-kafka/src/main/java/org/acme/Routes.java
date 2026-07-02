package org.acme;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:aws-s3-source?" +
                "bucketNameOrArn={{aws.s3.bucketNameOrArn}}&" +
                "region={{aws.s3.region}}&" +
                "overrideEndpoint=true&" +
                "forcePathStyle=true&" +
                "uriEndpointOverride={{aws.s3.uriEndpointOverride}}&" +
                "accessKey={{aws.s3.accessKey}}&" +
                "secretKey={{aws.s3.secretKey}}")
            .split(body().tokenize("\n"))
            .filter(simple("${body} != \"\""))
            .setBody()
                .simple("""
                    { "message": "${body}" }
                    """)
            .to(kafka("s3-events"));
    }
}
