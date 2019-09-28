package org.kie.kogito.poc.rules;

import static org.kie.kogito.poc.util.EventBusAddressProvider.PERSON_CAN_DRINK_CHANNEL;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.kogito.poc.model.Person;
import org.kie.kogito.rules.RuleUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
public class CanDrinkRuleUnitEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanDrinkRuleUnitEventConsumer.class);

    @Inject
    private RuleUnit<CanDrinkUnit> canDrinkUnit;

    @ConsumeEvent(value = PERSON_CAN_DRINK_CHANNEL)
    public CompletionStage<Person> canPersonDrink(Person person) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Evaluating CanDrinkRuleUnit.");

            CanDrinkUnit unitMemory = new CanDrinkUnit();
            unitMemory.getPersons().add(person);
            LOGGER.info("CanDrinkUnit: calling evaluate!");
            canDrinkUnit.evaluate(unitMemory);            
            LOGGER.info("CanDrinkUnit evaluated. Returning response!!!");
            return person;
        });
    }


}