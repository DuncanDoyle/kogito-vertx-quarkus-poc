package org.kie.kogito.poc.rules;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.PreferredIngredients;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitMemory;

/**
 * FilterDrinksByIngredientsUnit
 */
public class FilterDrinksByIngredientsUnit implements RuleUnitMemory {

    private DataStore<Drink> drinks;

    private DataStore<PreferredIngredients> ingredients;
    
    public FilterDrinksByIngredientsUnit() {
        this(DataSource.createStore(), DataSource.createStore());
    }

    public FilterDrinksByIngredientsUnit(DataStore<Drink> drinks, DataStore<PreferredIngredients> ingredients) {
        this.drinks = drinks;
        this.ingredients = ingredients;
    }

    public DataStore<Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(DataStore<Drink> drinks) {
        this.drinks = drinks;
    }

    public DataStore<PreferredIngredients> getIngredients() {
        return ingredients;
    }

    public void setIngredients(DataStore<PreferredIngredients> ingredients) {
        this.ingredients = ingredients;
    }

}