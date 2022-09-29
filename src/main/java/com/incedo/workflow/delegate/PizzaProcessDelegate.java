package com.incedo.workflow.delegate;

import com.incedo.workflow.model.Pizza;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public class PizzaProcessDelegate {
    private ZeebeClientLifecycle client;
    private Random random = new Random();
    PizzaProcessDelegate(@Autowired ZeebeClientLifecycle client){
        this.client = client;
    }
    @ZeebeWorker(type = "ValidatePizza", autoComplete = true)
    public Map<String, Object> ValidatePizza(final ActivatedJob job) {
        log.info("This is from Pizza Validation.");
        List<Pizza> pizzaList = (List<Pizza>) job.getVariablesAsMap().get("pizzaList");
        log.info("Pizza List from Validation : " + pizzaList);
        return Map.of("pizzaList", pizzaList);
    }

    @ZeebeWorker(type = "PizzaPrepareOrder", autoComplete = true)
    public void PizzaPrepareOrder(final ActivatedJob job) {
        log.info("This is from PizzaPrepareOrder.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        HashMap<String, String> pizzaObj = (HashMap<String, String>) job.getVariablesAsMap().get("inputPizza");
        Map<String, Object> pizzaName = new HashMap<>();
        pizzaName.put("pizza", pizzaObj);
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
        Object pizza = job.getVariablesAsMap().get("pizza");
        log.info("Confirmed Pizza: " + pizza);
    }

    @ZeebeWorker(type = "BackHouse_Status", autoComplete = true)
    public void PizzaStatus(final ActivatedJob job) {
        log.info("This is from PizzaStatus.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        Object pizzaObj = job.getVariablesAsMap().get("pizza");
        Map<String, Object> completedPizza = new HashMap<>();
        completedPizza.put("completedPizza", pizzaObj);
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
        Object pizzaObj = job.getVariablesAsMap().get("completedPizza");
        log.info("Completed Pizza: " + pizzaObj);
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
