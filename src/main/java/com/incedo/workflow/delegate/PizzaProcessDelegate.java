package com.incedo.workflow.delegate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incedo.workflow.exception.BPMNErrorList;
import com.incedo.workflow.model.Pizza;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class PizzaProcessDelegate {
    private ZeebeClientLifecycle client;
    private final ObjectMapper mapper = new ObjectMapper();
    PizzaProcessDelegate(@Autowired ZeebeClientLifecycle client) {
        this.client = client;
    }
    @ZeebeWorker(type = "ValidatePizza", autoComplete = true)
    public Map<String, Object> ValidatePizza(final ActivatedJob job) {
        log.info("This is from Pizza Validation.");
        List<Pizza> pizzaList = mapper.convertValue(job.getVariablesAsMap().get("pizzaList"),
                new TypeReference<List<Pizza>>() {
                });
        List<Pizza> newPizzaList = new ArrayList<>();
        for (Pizza pizza : pizzaList) {
            String name = pizza.getPizzaName();
            boolean isValidPizzaOrder = Arrays.stream(PizzaProcessDelegate.PizzaName.values())
                    .anyMatch(t -> t.pizza.equals(name));
            if (isValidPizzaOrder) {
                newPizzaList.add(pizza);
            } else {
                log.error(BPMNErrorList.ERROR_ITEM_INVALID + ": InValid Pizza Item: " + pizza + "\n with Business Key: ");
            }
        }
        if (newPizzaList.isEmpty()) {
            log.error(BPMNErrorList.ERROR_EMPTY_LIST + ": PizzaList is Empty with Business key: ");
            return (Map.of("pizzaList", null));
        } else {
            return (Map.of("pizzaList", newPizzaList));
        }
    }

    @ZeebeWorker(type = "PizzaPrepareOrder", autoComplete = true)
    public void PizzaPrepareOrder(final ActivatedJob job) {
        log.info("This is from PizzaPrepareOrder.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        Pizza pizza = mapper.convertValue(job.getVariablesAsMap().get("inputPizza"),
                new TypeReference<Pizza>() {
        });
        Map<String, Object> pizzaName = new HashMap<>();
        pizzaName.put("pizza", pizza);
        pizzaName.put("bKey", bKey);
        client.newPublishMessageCommand()
                .messageName("Message_PizzaCreation")
                .correlationKey(bKey)
                .variables(pizzaName)
                .send()
                .join();
    }

    @ZeebeWorker(type = "ConfirmPizza", autoComplete = true)
    public void ConfirmPizza(final ActivatedJob job) {
        log.info("This is from ConfirmPizza.");
        Pizza pizza = mapper.convertValue(job.getVariablesAsMap().get("pizza"),
                new TypeReference<Pizza>() {
                });
        log.info("Completed Pizza: " + pizza);
    }

    @ZeebeWorker(type = "BackHouse_Status", autoComplete = true)
    public void PizzaStatus(final ActivatedJob job) {
        log.info("This is from PizzaStatus.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        Pizza pizza = mapper.convertValue(job.getVariablesAsMap().get("pizza"),
                new TypeReference<Pizza>() {
                });
        Map<String, Object> completedPizza = new HashMap<>();
        completedPizza.put("completedPizza", pizza);
        completedPizza.put("bKey", bKey);
        client.newPublishMessageCommand()
                .messageName("Message_PizzaStatus")
                .correlationKey(bKey)
                .variables(completedPizza)
                .send()
                .join();
    }

    @ZeebeWorker(type = "CheckPizzaStatusFromKitchen", autoComplete = true)
    public void CheckPizzaStatusFromKitchen(final ActivatedJob job) {
        log.info("This is from CheckPizzaStatusFromKitchen.");
        Pizza pizza = mapper.convertValue(job.getVariablesAsMap().get("completedPizza"),
                new TypeReference<Pizza>() {
                });
        log.info("Completed Pizza: " + pizza);
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
