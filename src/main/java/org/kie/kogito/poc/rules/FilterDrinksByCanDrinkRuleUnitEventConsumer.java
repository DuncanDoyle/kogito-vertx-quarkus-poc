package org.kie.kogito.poc.rules;

import static org.kie.kogito.poc.marshall.MarshallingUtil.unmarshallDrinksFromJson;
import static org.kie.kogito.poc.marshall.MarshallingUtil.unmarshallPersonFromJson;
import static org.kie.kogito.poc.util.EventBusAddressProvider.FILTER_DRINKS_CAN_DRINK_CHANNEL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.Person;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class FilterDrinksByCanDrinkRuleUnitEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDrinksByCanDrinkRuleUnitEventConsumer.class);

    @Inject
    private RuleUnit<FilterDrinksByCanDrinkUnit> ruleUnit;

    @ConsumeEvent(value = FILTER_DRINKS_CAN_DRINK_CHANNEL)
    public CompletionStage<JsonArray> filterDrinksCanDrink(JsonObject drinksAndIngredients) {
        Person person = unmarshallPersonFromJson(drinksAndIngredients.getJsonObject("person"));
        List<Drink> drinks = unmarshallDrinksFromJson(drinksAndIngredients.getJsonArray("drinks"));

        FilterDrinksByCanDrinkUnit unit = new FilterDrinksByCanDrinkUnit();
        DataStore<Drink> drinksDS = unit.getDrinks();
        DataStore<Person> personsDS = unit.getPersons();

        personsDS.add(person);
        drinks.stream().forEach((drink) -> {
            drinksDS.add(drink);
        });

        return CompletableFuture.supplyAsync(() -> {
            RuleUnitInstance<FilterDrinksByCanDrinkUnit> filterDrinksByCanDrinkRuleUnitInstance = ruleUnit.evaluate(unit);
            
            List<Map<String, Object>> filteredDrinks = filterDrinksByCanDrinkRuleUnitInstance.executeQuery("FindFilteredDrinks");

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