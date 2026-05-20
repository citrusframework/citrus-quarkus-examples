package org.acme;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(file("inbox").delete(true))
            .unmarshal(dataFormat().zipFile().end())
            .convertBodyTo(String.class)
            .split(simple("${body}"))
                .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
                .to("kafka:words-out");
    }
}
