package com.incedo.workflow.delegate;

import com.incedo.workflow.exception.BPMNErrorList;
import com.incedo.workflow.exception.InvalidItemException;
import com.incedo.workflow.exception.ListEmptyException;
import com.incedo.workflow.model.Pizza;
import com.incedo.workflow.util.ValidatePizzaDelegate;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PizzaProcessDelegate {

    @ZeebeWorker(type = "ValidatePizza", autoComplete = true)
    public Map<String, Object> ValidatePizza(final ActivatedJob job) {
        log.info(">>>>>> Pizza Validation:");
        List<Pizza> pizzaList = (List<Pizza>) job.getVariablesAsMap().get("pizzaList");
        List<Pizza> newPizzaList = new ArrayList<>();
        for (Pizza pizza : pizzaList) {
            String name = pizza.getPizzaName();
            boolean isValidPizzaOrder = Arrays.stream(ValidatePizzaDelegate.PizzaName.values())
                    .anyMatch(t -> t.pizza.equals(name));
            if (isValidPizzaOrder) {
                newPizzaList.add(pizza);
            } else {
                log.error(BPMNErrorList.ERROR_ITEM_INVALID + ": InValid Pizza Item: " + pizza + "\n with Business Key: ");
                throw new InvalidItemException(BPMNErrorList.ERROR_ITEM_INVALID, "InValid Pizza Item" + pizza + " with Business Key: " );
            }
        }
        if (newPizzaList.isEmpty()) {
            log.error(BPMNErrorList.ERROR_EMPTY_LIST + ": PizzaList is Empty with Business key: ");
            throw new ListEmptyException(BPMNErrorList.ERROR_EMPTY_LIST, "PizzaList is Empty, with Business Key");
        } else {
            return (Map.of("pizzaList", newPizzaList));
        }
    }

    @ZeebeWorker(type = "PizzaPrepareOrder", autoComplete = true)
    public Map<String, Object> PizzaPrepareOrder(final ActivatedJob job, final ZeebeClientLifecycle client) {
        log.info("PizzaPrepareOrder start");
        Pizza pizza = (Pizza) job.getVariablesAsMap().get("eachPizza");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        Map<String, Object> pizzaName = new HashMap<>();
        pizzaName.put("pizza", pizza);
        client.newPublishMessageCommand()
                .messageName("Message_PizzaCreation")
                .correlationKey(bKey)
                .send()
                .join();
//        List<EventSubscription> eventSubscriptions = client
//                execution.getProcessEngineServices()
//                .getRuntimeService()
//                .createEventSubscriptionQuery()
//                .eventName("PizzaCreationMessage")
//                .eventType("message").list();
//        if (eventSubscriptions.isEmpty()) {
//            log.error("Back house Process isn't ready to receive the message. ");
//            throw new MessageCorrelationException(BPMNErrorList.ERROR_MESSAGE_NOT_CORRELATE, "Back house process isn't ready to receive the message. ");
//        } else {
//        }
        return Map.of("Name", "Hari");
    }

    @ZeebeWorker(type = "ConfirmPizza", autoComplete = true)
    public Map<String, Object> ConfirmPizza(final ActivatedJob job) {
        log.info("This is from PrepPizza.");
        return Map.of("Name", "Hari");
    }

    @ZeebeWorker(type = "PizzaStatus", autoComplete = true)
    public Map<String, Object> PizzaStatus(final ActivatedJob job) {
        log.info("This is from PrepPizza.");
        return Map.of("Name", "Hari");
    }

    private enum PizzaName {
        VEGGIE("Veggie Pizza"),
        CHEESE("Cheese Pizza"),
        MEAT("Meat Pizza"),
        CHICKEN("Chicken Pizza");
        private final String pizza;
        PizzaName(final String pizza) {
            this.pizza = pizza;
        }
        @Override
        public String toString() {
            return pizza;
        }
    }
}
