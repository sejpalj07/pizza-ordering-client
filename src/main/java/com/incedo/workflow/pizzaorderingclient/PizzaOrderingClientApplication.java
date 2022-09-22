package com.incedo.workflow.pizzaorderingclient;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
public class PizzaOrderingClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzaOrderingClientApplication.class, args);
    }

}
