package com.incedo.workflow.pizzaorderingclient.delegate;

import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PizzaPicker {

    @ZeebeWorker(type = "pickPizza", autoComplete = true)
    public void pickPizza() {
        log.info("I am in th pizza Block");

    }
}
