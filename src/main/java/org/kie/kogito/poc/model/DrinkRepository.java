package org.kie.kogito.poc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

/**
 * DrinkRepository
 */
@ApplicationScoped
public class DrinkRepository {

    
    private List<Drink> drinks;

    public DrinkRepository() {
        List<Drink> drinks = new ArrayList<>();

        drinks.add(new Drink("Screw Driver", true , "vodka"));
        drinks.add(new Drink ("Whiskey", true, "whiskey"));
        drinks.add(new Drink("Cola", false, "cola"));
        drinks.add(new Drink("Whiskey and Coke", true, "whiskey", "cola"));
        drinks.add(new Drink("Cuba Libre", true, "rum", "cola"));

        this.drinks = Collections.unmodifiableList(drinks);
    }

    public List<Drink> getDrinks() {
        return drinks;    
    }
}