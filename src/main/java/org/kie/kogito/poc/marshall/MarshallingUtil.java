package org.kie.kogito.poc.marshall;

import java.util.ArrayList;
import java.util.List;

import org.kie.kogito.poc.model.Drink;
import org.kie.kogito.poc.model.Person;
import org.kie.kogito.poc.model.PreferredIngredients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


/**
 * MarshallingUtil
 */
public class MarshallingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarshallingUtil.class);

    public static JsonArray marshallDrinksToJson(List<Drink> drinks) {
        JsonArray drinksJsonArray = new JsonArray();
        drinks.stream().forEach((drink) -> {
            drinksJsonArray.add(JsonObject.mapFrom(drink));
        });
        return drinksJsonArray;
    }

    public static List<Drink> unmarshallDrinksFromJson(JsonArray drinksJsonArray) {
        LOGGER.info("Unmarshalling drinks: " + drinksJsonArray);
        List<Drink> drinks = new ArrayList<>();

        drinksJsonArray.forEach((drink) -> {
            LOGGER.info("Next drink.");
            Drink decodedDrink = ((JsonObject) drink).mapTo(Drink.class);
            drinks.add(decodedDrink);
            LOGGER.info("Decoded drink: " + decodedDrink.getName());
        });
        return drinks;
    }

    public static PreferredIngredients unmarshallPreferredIngredientsFromJson(JsonArray prefIngredientsJsonArray) {
        List<String> prefIngredientsList = new ArrayList<>();
        LOGGER.info("Unmarshalling Preferred Ingredients: " + prefIngredientsJsonArray);
        
        prefIngredientsJsonArray.stream().forEach((ingredient) -> {
            // Ingredient is retrieved as a String.
            prefIngredientsList.add((String) ingredient);
        });
        String[] prefIngredientsArray = new String[prefIngredientsList.size()];
        return new PreferredIngredients(prefIngredientsList.toArray(prefIngredientsArray));
    }

    public static JsonArray marshallPreferredIngredientsToJson(PreferredIngredients prefIngredients) {
        JsonArray prefIngredientsJsonArray = new JsonArray();
        prefIngredients.getIngredients().stream().forEach((ingr) -> {
            prefIngredientsJsonArray.add(ingr);
        });
        return prefIngredientsJsonArray;
    }

    public static Person unmarshallPersonFromJson(JsonObject personJsonObject) {
        return personJsonObject.mapTo(Person.class);
    }

    public static JsonObject marshallPersonToJson(Person person) {
        return JsonObject.mapFrom(person);
    }
}