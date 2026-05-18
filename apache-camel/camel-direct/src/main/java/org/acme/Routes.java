package org.acme;

import org.apache.camel.builder.RouteBuilder;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:words-in")
            .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
            .to("mock:words-out");
    }
}
