package org.kie.kogito.poc.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PreferredIngredients
 */
public class PreferredIngredients {

    private final List<String> ingredients;
    
    public PreferredIngredients(String... ingredients) {
        this.ingredients = Collections.unmodifiableList(Arrays.asList(ingredients));
    }

    public List<String> getIngredients() {
		return ingredients;
    }
}