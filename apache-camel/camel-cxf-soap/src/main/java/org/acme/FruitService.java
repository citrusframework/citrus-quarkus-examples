package org.acme;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

@WebService(
        targetNamespace = "http://server.it.cxf.quarkiverse.io/",
        name = "FruitService"
)
public interface FruitService {

    @WebMethod
    @WebResult(name = "return")
    List<Fruit> addFruit(@WebParam(name = "fruit") Fruit fruit);

    @WebMethod
    @WebResult(name = "return")
    List<Fruit> deleteFruit(@WebParam(name = "fruit") Fruit fruit);

    @WebMethod
    @WebResult(name = "return")
    List<Fruit> listFruits();
}
