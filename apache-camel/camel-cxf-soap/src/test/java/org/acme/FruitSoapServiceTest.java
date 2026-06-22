package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.quarkus.CitrusSupport;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.config.annotation.WebServiceClientConfig;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@QuarkusTest
@CitrusSupport
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FruitSoapServiceTest implements TestActionSupport {

    private static final String FRUIT_NS = "http://server.it.cxf.quarkiverse.io/";

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8081/soap/fruits")
    WebServiceClient soapClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @BindToRegistry
    public SoapMessageFactory messageFactory() {
        SaajSoapMessageFactory factory = new SaajSoapMessageFactory();
        factory.afterPropertiesSet();
        return factory;
    }

    @Test
    @Order(1)
    void shouldListFruits() {
        runner.given(createVariables()
                .variable("targetNS", FRUIT_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:listFruits xmlns:ns=\"${targetNS}\"/>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:listFruitsResponse xmlns:ns=\"${targetNS}\">" +
                                "<return><description>Winter fruit</description><name>Apple</name></return>" +
                                "<return><description>Citrus fruit</description><name>Orange</name></return>" +
                            "</ns:listFruitsResponse>")
        );
    }

    @Test
    @Order(2)
    void shouldAddFruit() {
        runner.given(createVariables()
                .variable("targetNS", FRUIT_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:addFruit xmlns:ns=\"${targetNS}\">" +
                                "<fruit><description>Tropical fruit</description><name>Mango</name></fruit>" +
                            "</ns:addFruit>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:addFruitResponse xmlns:ns=\"${targetNS}\">" +
                                "<return><description>Winter fruit</description><name>Apple</name></return>" +
                                "<return><description>Citrus fruit</description><name>Orange</name></return>" +
                                "<return><description>Tropical fruit</description><name>Mango</name></return>" +
                            "</ns:addFruitResponse>")
        );
    }

    @Test
    @Order(3)
    void shouldDeleteFruit() {
        runner.given(createVariables()
                .variable("targetNS", FRUIT_NS));

        runner.when(
                soap().client(soapClient)
                        .send()
                        .message()
                        .soapAction("")
                        .body("<ns:deleteFruit xmlns:ns=\"${targetNS}\">" +
                                "<fruit><description>Winter fruit</description><name>Apple</name></fruit>" +
                            "</ns:deleteFruit>")
        );

        runner.then(
                soap().client(soapClient)
                        .receive()
                        .message()
                        .body("<ns:deleteFruitResponse xmlns:ns=\"${targetNS}\">" +
                                "<return><description>Citrus fruit</description><name>Orange</name></return>" +
                                "<return><description>Tropical fruit</description><name>Mango</name></return>" +
                            "</ns:deleteFruitResponse>")
        );
    }
}
