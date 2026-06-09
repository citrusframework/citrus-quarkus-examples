package org.acme;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(direct("tasks"))
            .aggregate(constant(true), new MultilineAggregationStrategy())
                .completionSize(3)
                .to(file("outbox").fileName("tasks.txt"));
    }

    //simply combines Exchange String body values using new lines as a delimiter
    static class MultilineAggregationStrategy implements AggregationStrategy {

        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            }

            String oldBody = oldExchange.getIn().getBody(String.class);
            String newBody = newExchange.getIn().getBody(String.class);
            oldExchange.getIn().setBody(oldBody + "\n" + newBody);
            return oldExchange;
        }
    }
}
