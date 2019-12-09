package org.kie.kogito.poc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * REST Resource that uses asynchronous execution of RuleUnits via CompletionStage composition.
 */
@ApplicationScoped
@Path("/drinksReactive")
public class DrinksResourceReactive {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrinksResourceReactive.class);

    @Inject
    private RuleUnit<CanDrinkUnit> canDrinkRuleUnit;

    @Inject
    private RuleUnit<FilterDrinksByIngredientsUnit> filterDrinksByIngredientRuleUnit;

    @Inject
    private RuleUnit<FilterDrinksByCanDrinkUnit> filterDrinksByCanDrinkRuleUnit;
    
    @Inject
    private DrinkRepository drinkRepository;


    @PostConstruct
    public void onStartupToo() {
        LOGGER.info("Starting Quarkus/Kogito with PostConstruct.");
    }

    // ---------------------------- Quarkus Event Observers ------------------------------------------
    // Observer Quarkus startup events.
    public void onStartup(@Observes StartupEvent event) {
        LOGGER.info("Starting Quarkus/Kogito async application.");
    }

    // Observe Quarkus Shutdown events.
    public void onShutdown(@Observes ShutdownEvent event) {
        LOGGER.info("Goodbye Quarkus.");
    }
    // -----------------------------------------------------------------------------------------------

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<String> helloAsyncBus(@QueryParam("name") String name, @QueryParam("age") int age,
            @QueryParam("ingredients") String ingredients) {

        // Prepare Facts.
        PreferredIngredients prefIngredients = new PreferredIngredients(ingredients.split(","));
        Person person = new Person(name, null, age);
        List<Drink> drinks = drinkRepository.getDrinks();

        // ----------------------------- Can Person Drink -----------------------------------------

        CompletionStage<Person> canPersonDrink = CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Evaluating CanDrinkRuleUnit.");
            CanDrinkUnit unitMemory = new CanDrinkUnit();
            unitMemory.getPersons().add(person);
            
            canDrinkRuleUnit.evaluate(unitMemory);            
            LOGGER.info("CanDrinkUnit evaluated. Returning response!!!");
            return person;
        });

        // -------------------------------- Filter Drinks by Ingredients --------------------------------------
        
        CompletionStage<List<Drink>> filteredDrinksStage = CompletableFuture.supplyAsync(() -> {

            FilterDrinksByIngredientsUnit unitMemory = new FilterDrinksByIngredientsUnit();
            DataStore<Drink> drinksDS = unitMemory.getDrinks();
            DataStore<PreferredIngredients> ingredientsDS = unitMemory.getIngredients();

            drinks.stream().forEach((drink) -> {
                LOGGER.info("Adding drink '" + drink.getName() + "' to SessionMemory!");
                drinksDS.add(drink);
            });

            ingredientsDS.add(prefIngredients);

            RuleUnitInstance<FilterDrinksByIngredientsUnit> filterDrinksByIngredientsRuleUnitInstance = filterDrinksByIngredientRuleUnit.evaluate(unitMemory);
            
            List<Map<String, Object>> filteredDrinksList = filterDrinksByIngredientsRuleUnitInstance.executeQuery("FindDrinks");
            
            List<Drink> filteredDrinksResponse = new ArrayList<>();
            filteredDrinksList.stream().forEach(map -> {
                // We could grab the EntrySet.stream() and apply a 'filter' to filter based on a
                // 'key', but I think using a simple 'get' is simply faster ....
                Object drink = map.get("drink");
                if (drink != null) {
                    filteredDrinksResponse.add((Drink) drink);
                }
            });
            return filteredDrinksResponse;
        });

        // ----------------------------------------------------------------------

        // ----------------------------- Filter Drinks By Can Drink -----------------------------------------
        /*
         * Combine the previous CompletionStages, create the event for the "Filter Drinks By Can Drink", send the event and parse the response.
         * Return the response in a new CompletionStage.
         */
        CompletionStage<String> finalStage = canPersonDrink.thenCombine(filteredDrinksStage,
                //combine the responses of the first 2 rule units.
                (myPerson, myDrinks) -> {
                    FilterDrinksByCanDrinkUnit unitMemory = new FilterDrinksByCanDrinkUnit();
                    DataStore<Drink> drinksDS = unitMemory.getDrinks();
                    DataStore<Person> personsDS = unitMemory.getPersons();
                    personsDS.add(myPerson);
                    
                    myDrinks.stream().forEach((myDrink) -> {
                        drinksDS.add(myDrink);
                    });

                    RuleUnitInstance<FilterDrinksByCanDrinkUnit> filterDrinksByCanDrinkRuleUnitInstance = filterDrinksByCanDrinkRuleUnit.evaluate(unitMemory);
                    List<Map<String, Object>> finalFilteredDrinks = filterDrinksByCanDrinkRuleUnitInstance.executeQuery("FindFilteredDrinks");
                    
                    List<Drink> filteredDrinksResponse = new ArrayList<>();

                    finalFilteredDrinks.stream().forEach((map) -> {
                        Object drink = map.get("drink");
                        if (drink != null) {
                            LOGGER.info("Filtered drink: " + drink);
                            filteredDrinksResponse.add((Drink) drink);
                        }
                    });
                    
                    return filteredDrinksResponse;
                    //return "Hello";
                })
                .thenApply((filteredDrinksResponse) -> {
                    StringBuilder responseStringBuilder = new StringBuilder();
                    responseStringBuilder
                        .append(person.getName())
                        .append(person.isCanDrink()? " CAN DRINK!": "CAN NOT DRINK!")
                        .append("\n---------------------------\n")
                        .append("Selected drinks: \n")
                        .append(filteredDrinksResponse.stream().map(Drink::getName).collect(Collectors.joining(",")));

                    return responseStringBuilder.toString();
                });
        return finalStage;
    }

    
}