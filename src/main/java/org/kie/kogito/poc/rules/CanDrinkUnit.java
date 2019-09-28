package org.kie.kogito.poc.rules;

import org.kie.kogito.poc.model.Person;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitMemory;

/**
 * CanDrinkRuleUnit
 */
public class CanDrinkUnit implements RuleUnitMemory {

    private int adultAge;

    private DataStore<Person> persons;

    public CanDrinkUnit( ) {
        this( DataSource.createStore() );
    }

    public CanDrinkUnit( DataStore<Person> persons ) {
        this.persons = persons;
    }

    public DataStore<Person> getPersons() {
        return persons;
    }

    public void setPersons( DataStore<Person> persons ) {
        this.persons = persons;
    }

    public int getAdultAge() {
        return adultAge;
    }

    public void setAdultAge( int adultAge ) {
        this.adultAge = adultAge;
    }
}