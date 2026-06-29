package org.acme;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(
        targetNamespace = FruitService.TARGET_NS,
        name = "FruitService",
        serviceName = "FruitService"
)
public interface FruitService {

    String TARGET_NS = "http://camel.apache.org/test/FruitService";

    @WebMethod
    @WebResult(name = "fruits")
    Fruits addFruit(@WebParam(name = "fruit") Fruit fruit);

    @WebMethod
    @WebResult(name = "fruits")
    Fruits deleteFruit(@WebParam(name = "fruit") Fruit fruit) throws NoSuchFruitException;

    @WebMethod
    @WebResult(name = "fruits")
    Fruits listFruits();
}
