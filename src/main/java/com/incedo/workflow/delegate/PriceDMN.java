package com.incedo.workflow.delegate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incedo.workflow.model.Item;
import com.incedo.workflow.model.Pizza;
import com.incedo.workflow.model.Side;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PriceDMN {
    private final ObjectMapper mapper = new ObjectMapper();

    @ZeebeWorker(type = "CombineList", autoComplete = true)
    public Map<String, Object> CombineList(final JobClient client, ActivatedJob job) throws Exception {
        log.info("This is from CombineList.");
        List<Item> itemList = new ArrayList<>();
        Map<String, Object> itemMap = new HashMap<>();
        List<Pizza> pizzaList = mapper.convertValue(job.getVariablesAsMap().get("pizzaList"),
                new TypeReference<List<Pizza>>() {
                });
        List<Side> sideList = mapper.convertValue(job.getVariablesAsMap().get("sideList"),
                new TypeReference<List<Side>>() {
                });
        List<String> drinksList = mapper.convertValue(job.getVariablesAsMap().get("drinksList"),
                new TypeReference<List<String>>() {
                });
        for (Pizza pizza : pizzaList) {
            itemList.add(new Item(pizza.getPizzaId(), pizza.getPizzaName(), 0, pizza.getToppings()));
        }
        for (Side side : sideList) {
            itemList.add(new Item(side.getSideId(), side.getSideName(), 0, null));
        }
        for (String drinks : drinksList) {
            String id = drinks.replaceAll("\\s", "");
            itemList.add(new Item(id, drinks, 0, null));
        }
        log.info("itemList : " + itemList);
        itemMap.put("itemList", itemList);
        return itemMap;
    }

    @ZeebeWorker(type = "TEST", autoComplete = true)
    public void test(final JobClient client, ActivatedJob job) throws Exception {
        log.info("This is from Test.");
    }

    @ZeebeWorker(type = "FindPrice", autoComplete = true)
    public Map<String, Object> findPrice(final JobClient client, ActivatedJob job) throws Exception {
        log.info("This is from FindPrice.");
        int itemPrice = (int) job.getVariablesAsMap().get("itemPrice");
        Item eachItem = mapper.convertValue(job.getVariablesAsMap().get("eachItem"),
                new TypeReference<Item>() {
                });
        List<Item> itemList = mapper.convertValue(job.getVariablesAsMap().get("itemList"),
                new TypeReference<List<Item>>() {
                });
        for (Item item : itemList) {
            if (item.getItemId().equals(eachItem.getItemId())) {
                item.setPrice(itemPrice);
            }
        }
        log.info(String.valueOf(itemList));
        return Map.of("itemList", itemList);
    }

    @ZeebeWorker(type = "FindTotal", autoComplete = true)
    public Map<String, Object> findtotal(final JobClient client, ActivatedJob job) throws Exception {
        log.info("This is from find-total.");
        List<Item> itemList = mapper.convertValue(job.getVariablesAsMap().get("itemList"),
                new TypeReference<List<Item>>() {
                });
        int total = 0;
        for (Item item : itemList) {
            total = total + item.getPrice();
        }
        log.info("Your total is : $" + total + ".00.");
        return Map.of("itemList", itemList);
    }
}
