package com.orkestra.app.config;

import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {

    @Bean
    public WorkflowYamlValidator workflowYamlValidator() {
        return new WorkflowYamlValidator();
    }

    @Bean
    public WorkflowDslValidator workflowDslValidator() {
        return new WorkflowDslValidator();
    }
}
