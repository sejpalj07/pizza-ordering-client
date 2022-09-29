package com.incedo.workflow.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incedo.workflow.model.Side;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MakeSideOrder {

    private final ObjectMapper mapper = new ObjectMapper();

    @ZeebeWorker(type = "MakeSideOrder", autoComplete = true)
    public void makeSideOrder(final JobClient client, ActivatedJob job) throws Exception {
        Side side = mapper.convertValue(job.getVariablesAsMap().get("eachSide"), new TypeReference<Side>() {
        });
        log.info("FROM SIDE DELEGATES Side Name :" + mapper.writeValueAsString(side));
    }
}
