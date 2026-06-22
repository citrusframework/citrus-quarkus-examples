package org.acme;

import io.quarkus.test.junit.QuarkusTest;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionSupport;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.config.annotation.HttpClientConfig;
import org.citrusframework.quarkus.CitrusSupport;
import org.junit.jupiter.api.Test;

@QuarkusTest
@CitrusSupport
class FruitRestDslTest implements TestActionSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:8081")
    HttpClient fruitRestClient;

    @CitrusResource
    GherkinTestActionRunner runner;

    @Test
    void shouldAddFruit() {
        runner.when(
                http()
                        .client(fruitRestClient)
                        .send()
                        .post()
                        .path("/fruits")
                        .fork(true)
                        .message()
                        .body("""
                        {
                           "name": "Orange",
                           "description": "The orange, also called sweet orange to distinguish it from the bitter orange (Citrus x aurantium), is the fruit of a tree in the family Rutaceae."
                        }
                        """)
                        .contentType("application/json")
        );

        runner.then(
                http()
                        .client(fruitRestClient)
                        .receive()
                        .response(201)
        );
    }

    @Test
    void shouldListFruits() {
        runner.when(
                http()
                        .client(fruitRestClient)
                        .send()
                        .get()
                        .path("/fruits")
        );

        runner.then(
                http()
                        .client(fruitRestClient)
                        .receive()
                        .response(200)
                        .message()
                        .body("""
                            [
                                {
                                  "name": "Apple",
                                  "description": "An apple is the round, edible fruit of an apple tree (Malus spp.)."
                                },
                                {
                                  "name": "Orange",
                                  "description": "The orange, also called sweet orange to distinguish it from the bitter orange (Citrus x aurantium), is the fruit of a tree in the family Rutaceae."
                                },
                                {
                                  "name": "Mango",
                                  "description": "A mango is an edible stone fruit produced by the tropical tree Mangifera indica."
                                }
                            ]
                            """)
        );
    }

    @Test
    void shouldDeleteFruit() {
        runner.when(
                http().client(fruitRestClient)
                        .send()
                        .delete("/fruits")
                        .message()
                        .contentType("application/json")
                        .body("""
                        {
                            "name": "Pineapple",
                            "description": "A sweet and tasty tropical fruit."
                        }
                        """)
        );

        runner.then(
                http().client(fruitRestClient)
                        .receive()
                        .response(204)
        );
    }
}
