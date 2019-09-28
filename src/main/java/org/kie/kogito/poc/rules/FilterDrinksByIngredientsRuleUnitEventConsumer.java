package org.kie.kogito.poc.rules;

import static org.kie.kogito.poc.marshall.MarshallingUtil.unmarshallDrinksFromJson;
import static org.kie.kogito.poc.marshall.MarshallingUtil.unmarshallPreferredIngredientsFromJson;
import static org.kie.kogito.poc.util.EventBusAddressProvider.FILTER_DRINKS_INGREDIENTS_CHANNEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.PreferredIngredients;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class FilterDrinksByIngredientsRuleUnitEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDrinksByIngredientsRuleUnitEventConsumer.class);

    /*
    @Inject
    @Named("filterDrinksByIngredientsRuleUnit")
    private RuleUnit<SessionMemory> filterDrinksByIngredientsRuleUnit;
    */
    @Inject
    private RuleUnit<FilterDrinksByIngredientsUnit> ruleUnit;

    @ConsumeEvent(value = FILTER_DRINKS_INGREDIENTS_CHANNEL)
    public CompletionStage<JsonArray> filterDrinksIngredients(JsonObject drinksAndIngredients) {
        LOGGER.info("Consuming DrinksAndIngredients message from bus!");

        JsonArray drinksJsonArray = drinksAndIngredients.getJsonArray("drinks");
        JsonArray ingredientsJsonArray = drinksAndIngredients.getJsonArray("ingredients");

        LOGGER.info("Unmarshalling Drinks!");
        List<Drink> drinks = unmarshallDrinksFromJson(drinksJsonArray);
        LOGGER.info("Unmarshalling Preferred Ingredients!");
        PreferredIngredients preferredIngredients = null;
        try {
            preferredIngredients = unmarshallPreferredIngredientsFromJson(ingredientsJsonArray);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        LOGGER.info("Drinks: " + drinksJsonArray.toString());
        LOGGER.info("Drinks list size: " + drinks.size());
        LOGGER.info("Ingredients: " + ingredientsJsonArray.toString());

        FilterDrinksByIngredientsUnit unit = new FilterDrinksByIngredientsUnit();
        DataStore<Drink> drinksDS = unit.getDrinks();
        DataStore<PreferredIngredients> ingredientsDS = unit.getIngredients();

        drinks.stream().forEach((drink) -> {
            LOGGER.info("Adding drink '" + drink.getName() + "' to SessionMemory!");
            drinksDS.add(drink);
        });

        ingredientsDS.add(preferredIngredients);

        // TODO: Why don't we run the whole thing in a CompletableFuture? I.e., also the
        // marshalling of data and insertion of data?
        LOGGER.info("Before returning the Completable Future!");
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Executing RuleUnit2!");

            RuleUnitInstance<FilterDrinksByIngredientsUnit> filterDrinksByIngredientsRuleUnitInstance = ruleUnit.evaluate(unit);
            LOGGER.info("Executing Query!");

            List<Map<String, Object>> filteredDrinks = filterDrinksByIngredientsRuleUnitInstance.executeQuery("FindDrinks");
            LOGGER.info("Query returned!! Found drinks.");
            List<Drink> filteredDrinksResponse = new ArrayList<>();
            filteredDrinks.stream().forEach(map -> {
                // We could grab the EntrySet.stream() and apply a 'filter' to filter based on a
                // 'key', but I think using a simple 'get' is simply faster ....
                Object drink = map.get("drink");
                if (drink != null) {
                    LOGGER.info("Filtered drink: " + drink);
                    filteredDrinksResponse.add((Drink) drink);
                }
            });

            JsonArray filteredDrinksRepsonseJsonArray = new JsonArray();

            filteredDrinksResponse.stream().forEach((drink -> {
                filteredDrinksRepsonseJsonArray.add(JsonObject.mapFrom(drink));
            }));

            LOGGER.info("Filtered drinks response array: " + filteredDrinksRepsonseJsonArray);

            return filteredDrinksRepsonseJsonArray;
        });
    }
}