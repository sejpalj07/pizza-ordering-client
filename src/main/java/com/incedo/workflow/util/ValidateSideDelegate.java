package com.incedo.workflow.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incedo.workflow.model.Side;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ValidateSideDelegate {
    private final ObjectMapper mapper = new ObjectMapper();

    @ZeebeWorker(type = "SideValidation")
    public void sideValidation(final JobClient client, ActivatedJob job) throws Exception {
        List<Side> sideList = mapper.convertValue(job.getVariablesAsMap().get("sideList"), new TypeReference<List<Side>>() {
        });
        List<Side> validSideList = new ArrayList<>();
        for (Side side : sideList) {
            boolean isValidPizzaOrder = Arrays.stream(SideName.values())
                    .anyMatch(t -> t.side.equals(side.getSideName()));
            if (isValidPizzaOrder)
                validSideList.add(side);
            else
                log.error("Removing InValid Side Item " + side.getSideName());
        }
        if (validSideList.isEmpty()) {
            log.error("No Valid sides provided");
            client.newThrowErrorCommand(job)
                    .errorCode("Error_6000")
                    .errorMessage("Error_6000")
                    .send();
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("sideList", validSideList);
            client.newCompleteCommand(job.getKey()).variables(map).send();
        }
    }

    private enum SideName {
        BREAD("Garlic Bread"),
        FRIES("Fries"),
        GARLIC("Garlic"),
        WINGS("Wings");
        private final String side;

        SideName(final String side) {
            this.side = side;
        }

        @Override
        public String toString() {
            return side;
        }
    }
}
