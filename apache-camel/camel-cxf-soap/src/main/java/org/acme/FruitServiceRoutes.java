package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.test.fruitservice.FruitService;

public class FruitServiceRoutes extends RouteBuilder {

    @Produces
    @ApplicationScoped
    @Named
    CxfEndpoint fruitEndpoint() {
        CxfEndpoint fruitEndpoint = new CxfEndpoint();
        fruitEndpoint.setServiceClass(FruitService.class);
        fruitEndpoint.setAddress("/fruits");

        return fruitEndpoint;
    }

    @Override
    public void configure() throws Exception {
        from("cxf:bean:fruitEndpoint")
                .recipientList(simple("bean:fruitService?method=${header.operationName}"));
    }
}
