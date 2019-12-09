package org.kie.kogito.poc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.DrinkRepository;
import org.kie.kogito.poc.model.Person;
import org.kie.kogito.poc.model.PreferredIngredients;
import org.kie.kogito.poc.rules.CanDrinkUnit;
import org.kie.kogito.poc.rules.FilterDrinksByCanDrinkUnit;
import org.kie.kogito.poc.rules.FilterDrinksByIngredientsUnit;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Resource that synchronously executes the rule units.
 */
@ApplicationScoped
@Path("/drinksSync")
public class DrinksResourceSync {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrinksResourceSync.class);

    @Inject
    private RuleUnit<CanDrinkUnit> canDrinkRuleUnit;

    @Inject
    private RuleUnit<FilterDrinksByIngredientsUnit> filterDrinksByIngredientRuleUnit;

    @Inject
    private RuleUnit<FilterDrinksByCanDrinkUnit> filterDrinksByCanDrinkRuleUnit;
    

    @Inject
    private DrinkRepository drinkRepository;

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // Todo: figure out how we can pass a collection via a queryparam.
    public String hello(@QueryParam("name") String name, @QueryParam("age") int age,
            @QueryParam("ingredients") String ingredients) throws Exception {
        
                
        // Prepare Facts.
        PreferredIngredients prefIngredients = new PreferredIngredients(ingredients.split(","));

        for (String nextIngredient : prefIngredients.getIngredients()) {
            LOGGER.info("Ingredient: " + nextIngredient);
        }
        Person person = new Person(name, null, age);

        // Execute RuleUnitOne to determine whether the Person can drink alcohol.
        CanDrinkUnit canDrinkUnit = new CanDrinkUnit();
        DataStore<Person> personsDS = canDrinkUnit.getPersons();
        personsDS.add(person);
        RuleUnitInstance<CanDrinkUnit> canDrinkRuleUnitInstance = canDrinkRuleUnit.evaluate(canDrinkUnit);
               

        // Execute RuleUnitTwo to filter the drinks based on ingredients.
        //SessionMemory memoryTwo = new SessionMemory();
        FilterDrinksByIngredientsUnit filterDrinksByIngredientsUnit = new FilterDrinksByIngredientsUnit();
        DataStore<Drink> drinksDS = filterDrinksByIngredientsUnit.getDrinks();
        DataStore<PreferredIngredients> ingredientsDS = filterDrinksByIngredientsUnit.getIngredients();

        drinkRepository.getDrinks().stream().forEach(drink -> drinksDS.add(drink));
        ingredientsDS.add(prefIngredients);
        RuleUnitInstance<FilterDrinksByIngredientsUnit> filterDrinksByIngredientsRuleUnitInstance = filterDrinksByIngredientRuleUnit.evaluate(filterDrinksByIngredientsUnit);

        List<Map<String, Object>> filteredDrinks = filterDrinksByIngredientsRuleUnitInstance.executeQuery("FindDrinks");

        List<Drink> drinks = new ArrayList<>();

        filteredDrinks.stream().forEach(map -> {
            // We could grab the EntrySet.stream() and apply a 'filter' to filter based on a
            // 'key', but I think using a simple 'get' is simply faster ....
            Object drink = map.get("drink");
            if (drink != null) {
                drinks.add((Drink) drink);
            }
        });

        

        // Execute RuleUnitThree to filter drinks based on whether someone can drink and
        // the drink contains alcohol or not.
        FilterDrinksByCanDrinkUnit filterDrinksByCanDrinkUnit = new FilterDrinksByCanDrinkUnit();
        DataStore<Drink> drinksDS2 = filterDrinksByCanDrinkUnit.getDrinks();
        DataStore<Person> personsDS2 = filterDrinksByCanDrinkUnit.getPersons();

        drinks.stream().forEach((drink) -> drinksDS2.add(drink));
        personsDS2.add(person);
        RuleUnitInstance<FilterDrinksByCanDrinkUnit> filterDrinksByCanDrinkRuleUnitInstance = filterDrinksByCanDrinkRuleUnit.evaluate(filterDrinksByCanDrinkUnit);

        List<Map<String, Object>> finalDrinks = filterDrinksByCanDrinkRuleUnitInstance.executeQuery("FindFilteredDrinks");

        List<Drink> myDrinks = new ArrayList<>();
        finalDrinks.stream().forEach((map) -> {
            Object drink = map.get("drink");
            if (drink != null) {
                myDrinks.add((Drink) drink);
            }
        });

        // Build the response.
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(person.getName()).append(" ").append(person.isCanDrink() ? "CAN" : "CAN NOT")
                .append(" drink");

        responseBuilder.append("\n").append("------------------------------------").append("\n");

        myDrinks.stream().forEach((drink) -> {
            responseBuilder.append("\n").append("- ").append(drink.getName());
        });
        
        return responseBuilder.toString();
    }
}