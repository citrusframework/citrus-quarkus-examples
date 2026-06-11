package org.acme;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(kafka("words-in").autoOffsetReset("earliest"))
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to(kafka("words-out"));
    }
}
