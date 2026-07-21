package org.acme;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataType;

public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kamelet:timer-source?period=5000&message={{timer.message:Hello Knative!}}")
                .transformDataType(new DataType("http:application-cloudevents"))
                .to("knative:event/org.apache.camel.event.messages?kind=Broker&name=default");
    }
}
