package com.incedo.workflow.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.ZeebeClientLifecycle;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PaymentProcess {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ZeebeClientLifecycle client;

    public PaymentProcess(@Autowired ZeebeClientLifecycle client){
        this.client=client;
    }

    @ZeebeWorker(type = "InvokePaymentProcess", autoComplete = true)
    public void invokePaymentProcess(final ActivatedJob job) {
        log.info("This is from InvokePaymentProcess.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
        String paymentType = (String) job.getVariablesAsMap().get("paymentType");
        Map<String, Object> mapObj = new HashMap<>();
        mapObj.put("bKey", bKey);
        mapObj.put("paymentType", paymentType);
        client.newPublishMessageCommand()
                .messageName("Message_MakePayment")
                .correlationKey(bKey)
                .variables(mapObj)
                .send()
                .join();
    }

    @ZeebeWorker(type = "ValidateAccountBalance", autoComplete = true)
    public Map<String, Object> validateAccountBalance(){
        log.info("Entered ValidateAccountBalance");
        Map<String, Object> objMap = new HashMap<>();
        objMap.put("sufficientBalance", true);
        return objMap;
    }

    @ZeebeWorker(type = "DebitDelegate", autoComplete = true)
    public void debitDelegate(){
        log.info("From DebitDelegate.");
    }

    @ZeebeWorker(type = "CreditDelegate", autoComplete = true)
    public void creditDelegate(){
        log.info("From CreditDelegate.");
    }

    @ZeebeWorker(type = "CashPayment", autoComplete = true)
    public void cashPayment(){
        log.info("From CashPayment.");
    }

    @ZeebeWorker(type = "OnlinePayment", autoComplete = true)
    public void onlinePayment(){
        log.info("From OnlinePayment.");
    }

    @ZeebeWorker(type = "PaymentCompletion", autoComplete = true)
    public Map<String, Object> paymentCompletion(final ActivatedJob job){
        log.info("From paymentStatus.");
        String bKey = (String) job.getVariablesAsMap().get("bKey");
//        String sufficientBalance = (String) job.getVariablesAsMap().get("sufficientBalance");
        Map<String, Object> variables = new HashMap<>();//        variables.put("sufficientBalance", sufficientBalance);
        variables.put("bKey", bKey);
        client.newPublishMessageCommand()
                .messageName("Message_PaymentCompletion")
                .correlationKey(bKey)
                .variables(variables)
                .send()
                .join();
        return variables;
    }

}
