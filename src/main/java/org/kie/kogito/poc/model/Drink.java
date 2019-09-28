package org.kie.kogito.poc.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Drink
 */
@RegisterForReflection
public class Drink {

    private String name;

    private boolean containsAlcohol;

    private List<String> ingredients;

    public Drink() {    
    }
 
    public Drink(final String name, final boolean containsAlcohol, final String... ingredients) {
        this.name = name;
        this.containsAlcohol = containsAlcohol;
        this.ingredients = Collections.unmodifiableList(Arrays.asList(ingredients));
    }

    public String getName() {
        return name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public boolean isContainsAlcohol() {
        return containsAlcohol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContainsAlcohol(boolean containsAlcohol) {
        this.containsAlcohol = containsAlcohol;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    

}