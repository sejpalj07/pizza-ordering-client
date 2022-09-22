package com.incedo.workflow.pizzaorderingclient.controller;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/smaple")
public class SampleController {
//
//    @Qualifier("zeebeClientLifecycle")
//    @Autowired
//    ZeebeClient zeebeClient;


//    public void placeholder() {
//        zeebeClient.newCreateInstanceCommand().processDefinitionKey("").variables().send();
//    }


}
