package com.incedo.workflow;


import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.camunda.community.migration.CamundaPlatform7AdapterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@EnableZeebeClient
@Import(CamundaPlatform7AdapterConfig.class)
@ZeebeDeployment(resources = "classpath:*.bpmn")
@SpringBootApplication
public class Application {
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }


}