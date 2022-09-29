package com.incedo.workflow.controller;

import com.incedo.workflow.model.Order;
import com.incedo.workflow.model.Pizza;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RestController
public class HomeController {
    private ZeebeClientLifecycle client;
    private Random random = new Random();

    public HomeController(@Autowired ZeebeClientLifecycle client) {
        this.client = client;
    }

    @PostMapping("/process")
    public ResponseEntity<String> invokeProcess(@Valid @RequestBody Order order) {
        log.info("Initial order : " + order);
        String bKey = String.valueOf(Math.abs(random.nextInt()));
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("pizzaList", order.getPizzaList());
        orderMap.put("sideList", order.getSideList());
        orderMap.put("drinksList", order.getDrinksList());
        orderMap.put("paymentType", order.getPaymentType());
        orderMap.put("deliveryType", order.getDeliveryType());
        orderMap.put("pickupTime", order.getPickupTime());
        orderMap.put("address", order.getAddress());
        orderMap.put("validationMessage", order.getValidationMessage());
        orderMap.put("customerInfo", order.getCustomerInfo());
        orderMap.put("bKey", bKey);

        client.newPublishMessageCommand()
                .messageName("Message_Order")
                .correlationKey(bKey)
                .variables(orderMap)
                .send()
                .join();

        return new ResponseEntity<>("Pizza Processing BPM is Running.", HttpStatus.OK);
    }
}
