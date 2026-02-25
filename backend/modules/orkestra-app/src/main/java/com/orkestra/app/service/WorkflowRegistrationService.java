package com.orkestra.app.service;

import com.orkestra.dsl.model.DslModel;
import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import com.orkestra.graph.model.GraphModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WorkflowRegistrationService {
    private final WorkflowYamlValidator workflowYamlValidator;
    private final WorkflowDslValidator workflowDslValidator;

    public void register(String name, String yaml) {
        DslModel dslModel = workflowYamlValidator.validate(name, yaml);
        GraphModel graphModel = workflowDslValidator.validate(dslModel);
    }


}
