package com.orkestra.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import com.orkestra.storage.dynamodb.JobDefinitionRepository;
import com.orkestra.storage.dynamodb.WorkflowIndexRepository;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@EnableConfigurationProperties(OrkestraDynamoProperties.class)
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

    @Bean
    JobDefinitionRepository jobDefinitionRepository(DynamoDbTable<JobDefinitionTable> dynamoDbTable, final ObjectMapper objectMapper) {
        return new JobDefinitionRepository(dynamoDbTable, objectMapper);
    }

    @Bean
    WorkflowIndexRepository workflowIndexRepository(DynamoDbTable<WorkflowIndexTable> workflowIndexTable) {
        return new WorkflowIndexRepository(workflowIndexTable);
    }

}
