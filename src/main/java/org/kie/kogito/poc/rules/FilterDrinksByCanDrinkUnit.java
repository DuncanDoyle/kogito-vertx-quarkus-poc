package org.kie.kogito.poc.rules;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.Person;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitMemory;

/**
 * FilterDrinksByCanDrinkUnit
 */
public class FilterDrinksByCanDrinkUnit implements RuleUnitMemory {


    private DataStore<Drink> drinks;
    private DataStore<Person> persons;

    public FilterDrinksByCanDrinkUnit() {
        this(DataSource.createStore(), DataSource.createStore());
    }

    public FilterDrinksByCanDrinkUnit(DataStore<Drink> drinks, DataStore<Person> persons) {
        this.drinks = drinks;
        this.persons = persons;
    }

    public DataStore<Drink> getDrinks() {
        return drinks;
    }

    public void setDrinks(DataStore<Drink> drinks) {
        this.drinks = drinks;
    }

    public DataStore<Person> getPersons() {
        return persons;
    }

    public void setPersons(DataStore<Person> persons) {
        this.persons = persons;
    }


}