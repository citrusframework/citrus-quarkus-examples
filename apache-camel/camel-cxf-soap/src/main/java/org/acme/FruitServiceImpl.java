package org.acme;

import java.util.LinkedList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("fruitService")
public class FruitServiceImpl implements FruitService {

    private final List<Fruit> fruits = new LinkedList<>(List.of(
            new Fruit("Apple", "Winter fruit"),
            new Fruit("Orange", "Citrus fruit")
    ));

    @Override
    public Fruits addFruit(Fruit fruit) {
        fruits.add(fruit);
        return new Fruits(fruits);
    }

    @Override
    public Fruits deleteFruit(Fruit fruit) throws NoSuchFruitException {
        int index = -1;
        for (int i = 0; i < fruits.size(); i++) {
            if (fruits.get(i).getName().equals(fruit.getName())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new NoSuchFruitException(fruit.getName());
        }

        fruits.remove(index);
        return new Fruits(fruits);
    }

    @Override
    public Fruits listFruits() {
        return new Fruits(fruits);
    }
}
