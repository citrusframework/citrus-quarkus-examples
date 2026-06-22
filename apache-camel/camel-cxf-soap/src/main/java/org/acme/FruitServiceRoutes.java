package org.acme;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FruitServiceRoutes extends RouteBuilder {

    private static final String NS = "http://server.it.cxf.quarkiverse.io/";

    private final List<Fruit> fruits = new LinkedList<>(List.of(
            new Fruit("Apple", "Winter fruit"),
            new Fruit("Orange", "Citrus fruit")
    ));

    @Override
    public void configure() throws Exception {
        from("cxf:/fruits?serviceClass=org.acme.FruitService&dataFormat=PAYLOAD")
                .choice()
                    .when(header(CxfConstants.OPERATION_NAME).isEqualTo("addFruit"))
                        .process(exchange -> {
                            Element body = exchange.getIn().getBody(Element.class);
                            Fruit fruit = extractFruit(body);
                            fruits.add(fruit);
                            exchange.getIn().setBody(buildFruitListResponse("addFruitResponse"));
                        })
                    .when(header(CxfConstants.OPERATION_NAME).isEqualTo("deleteFruit"))
                        .process(exchange -> {
                            Element body = exchange.getIn().getBody(Element.class);
                            Fruit fruit = extractFruit(body);
                            fruits.removeIf(f -> f.getName().equals(fruit.getName()));
                            exchange.getIn().setBody(buildFruitListResponse("deleteFruitResponse"));
                        })
                    .when(header(CxfConstants.OPERATION_NAME).isEqualTo("listFruits"))
                        .process(exchange ->
                            exchange.getIn().setBody(buildFruitListResponse("listFruitsResponse")));
    }

    private Fruit extractFruit(Element body) {
        NodeList fruitNodes = body.getElementsByTagNameNS(NS, "fruit");
        if (fruitNodes.getLength() == 0) {
            fruitNodes = body.getElementsByTagName("fruit");
        }
        if (fruitNodes.getLength() > 0) {
            Element fruitEl = (Element) fruitNodes.item(0);
            String name = getChildText(fruitEl, "name");
            String description = getChildText(fruitEl, "description");
            return new Fruit(name, description);
        }
        return new Fruit("Unknown", "Unknown");
    }

    private String getChildText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS(NS, localName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(localName);
        }
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    private Document buildFruitListResponse(String responseName) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        Element response = doc.createElementNS(NS, "ns:" + responseName);
        doc.appendChild(response);

        for (Fruit fruit : new ArrayList<>(fruits)) {
            Element returnEl = doc.createElement("return");
            Element descEl = doc.createElement("description");
            descEl.setTextContent(fruit.getDescription());
            returnEl.appendChild(descEl);
            Element nameEl = doc.createElement("name");
            nameEl.setTextContent(fruit.getName());
            returnEl.appendChild(nameEl);
            response.appendChild(returnEl);
        }
        return doc;
    }
}
