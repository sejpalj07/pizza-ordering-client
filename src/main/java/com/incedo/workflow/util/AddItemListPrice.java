package com.incedo.workflow.util;

import com.incedo.workflow.exception.BPMNErrorList;
import com.incedo.workflow.exception.PriceNotFound;
import com.incedo.workflow.model.Item;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AddItemListPrice {
    @ZeebeWorker(type = "AddItemListPrice", autoComplete = true)
    public Map<String, Object> executeProcess(final ActivatedJob job){
        try {
            int itemPrice = (int) job.getVariablesAsMap().get("itemPrice");
            Item eachItem = (Item) job.getVariablesAsMap().get("eachItemOrder");
            List<Item> itemList = (ArrayList<Item>) job.getVariablesAsMap().get("itemList");
            for (Item item : itemList) {
                if (item.getItemId().equals(eachItem.getItemId())) {
                    item.setPrice(itemPrice);
                }
            }
            log.info(String.valueOf(itemList));
            return Map.of("itemList", itemList);
        } catch (Exception ex) {
            log.error("Price not Found.");
            throw new PriceNotFound(BPMNErrorList.ERROR_PRICE_NOT_FOUND, "Price not found in dmn table.");
        }
    }
}
