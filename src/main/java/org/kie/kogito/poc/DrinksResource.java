package org.kie.kogito.poc;

import static org.kie.kogito.poc.marshall.MarshallingUtil.marshallDrinksToJson;
import static org.kie.kogito.poc.marshall.MarshallingUtil.marshallPersonToJson;
import static org.kie.kogito.poc.marshall.MarshallingUtil.marshallPreferredIngredientsToJson;
import static org.kie.kogito.poc.marshall.MarshallingUtil.unmarshallDrinksFromJson;
import static org.kie.kogito.poc.util.EventBusAddressProvider.FILTER_DRINKS_CAN_DRINK_CHANNEL;
import static org.kie.kogito.poc.util.EventBusAddressProvider.FILTER_DRINKS_INGREDIENTS_CHANNEL;
import static org.kie.kogito.poc.util.EventBusAddressProvider.PERSON_CAN_DRINK_CHANNEL;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
import org.kie.kogito.poc.model.codec.PersonMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.axle.core.eventbus.EventBus;
import io.vertx.axle.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 *
 */
@ApplicationScoped
@Path("/drinks")
public class DrinksResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrinksResource.class);

    /*
     * We use the Vert.x EventBus to send messages asynchronously between beans.
     * The advantage over ReactiveStreams is that we can do replies, which is kind of convenient when we want to do HTTP request reply.
     */ 
    @Inject
    private EventBus bus;

    @Inject
    private DrinkRepository drinkRepository;

    // ---------------------------- Quarkus Event Observers ------------------------------------------
    // Observer Quarkus startup events.
    public void onStartup(@Observes StartupEvent event) {
        LOGGER.info("Starting Quarkus/Kogito async application.");
        // Register Codecs
        LOGGER.info("Registering codecs");
        // bus.registerCodec(new PersonMessageCodec());
        bus.getDelegate().registerDefaultCodec(Person.class, new PersonMessageCodec());
    }

    // Observe Quarkus Shutdown events.
    public void onShutdown(@Observes ShutdownEvent event) {
        LOGGER.info("Goodbye Quarkus.");
        // Need to unregister for Hot-Reload to work and not crap out (it will if the
        // codec is registered twice).
        bus.getDelegate().unregisterDefaultCodec(Person.class);
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

        //Send event to trigger RuleUnit that determines whether the person can drink.
        CompletionStage<Person> canPersonDrink = bus.<Person>send(PERSON_CAN_DRINK_CHANNEL, person).thenApply(Message::body);

        // ----------------------------------------------------------------------
        
        // ----------------------------- Filter Drinks By Ingredients -----------------------------------------
        // Prepare event which contains both the drinks and the preferred ingredients.
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("drinks", marshallDrinksToJson(drinks));
        jsonObject.put("ingredients", marshallPreferredIngredientsToJson(prefIngredients));
        LOGGER.info("Sending DrinksAndIngredients message to bus!");
        LOGGER.info(jsonObject.toString());

        //Send event to trigger RuleUnit that determines the drinks based on provided ingredients.
        CompletionStage<List<Drink>> filteredDrinks = bus.<JsonArray>send(FILTER_DRINKS_INGREDIENTS_CHANNEL, jsonObject)
                .thenApply((message) -> {
                    JsonArray filteredDrinksJsopnArray = message.body();
                    List<Drink> filteredDrinksUnmarshalled = unmarshallDrinksFromJson(filteredDrinksJsopnArray);
                    return filteredDrinksUnmarshalled;
                });

        // ----------------------------------------------------------------------

        // ----------------------------- Filter Drinks By Can Drink -----------------------------------------
        /*
         * Combine the previous CompletionStages, create the event for the "Filter Drinks By Can Drink", send the event and parse the response.
         * Return the response in a new CompletionStage.
         */
        CompletionStage<String> finalStage = canPersonDrink.thenCombine(filteredDrinks,
                //combine the responses of the first 2 rule units.
                (myPerson, myDrinks) -> {
                    JsonObject myJsonObject = new JsonObject();
                    myJsonObject.put("person", marshallPersonToJson(myPerson));
                    myJsonObject.put("drinks", marshallDrinksToJson(myDrinks));
                    return myJsonObject;
                })
                // send the event to the Vert.x bus to trigger the final Rule Unit execution.
                .thenCompose(inputJson -> bus.<JsonArray>send(FILTER_DRINKS_CAN_DRINK_CHANNEL, inputJson))
                .thenApply(Message::body)
                //parse the reponse and create a new response string.
                .thenApply(finalDrinksJsonArray -> {
                        List<Drink> outputDrinks = unmarshallDrinksFromJson(finalDrinksJsonArray);
                        StringBuilder responseStringBuilder = new StringBuilder();
                        responseStringBuilder
                                        .append(person.getName())
                                        .append(person.isCanDrink()? " CAN DRINK!": "CAN NOT DRINK!")
                                        .append("\n---------------------------\n")
                                        .append("Selected drinks: \n")
                                        .append(outputDrinks.stream().map(Drink::getName).collect(Collectors.joining(",")));
                        return responseStringBuilder.toString();
                });
        // ----------------------------------------------------------------------

        return finalStage;
    }

    
}