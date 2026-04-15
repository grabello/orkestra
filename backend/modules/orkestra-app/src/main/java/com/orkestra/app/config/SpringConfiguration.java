package com.orkestra.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import com.orkestra.storage.dynamodb.JobDefinitionRepository;
import com.orkestra.storage.dynamodb.PlatformAdminRepository;
import com.orkestra.storage.dynamodb.TenantMembershipRepository;
import com.orkestra.storage.dynamodb.TenantRepository;
import com.orkestra.storage.dynamodb.WorkflowIndexRepository;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.PlatformAdminTable;
import com.orkestra.storage.dynamodb.model.TenantMembershipTable;
import com.orkestra.storage.dynamodb.model.TenantTable;
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

    @Bean
    PlatformAdminRepository platformAdminRepository(DynamoDbTable<PlatformAdminTable> platformAdminTableDynamoDbTable, final ObjectMapper objectMapper) {
        return new PlatformAdminRepository(platformAdminTableDynamoDbTable, objectMapper);
    }

    @Bean
    TenantRepository tenantRepository(DynamoDbTable<TenantTable> tenantTable, final ObjectMapper objectMapper) {
        return new TenantRepository(tenantTable, objectMapper);
    }

    @Bean
    TenantMembershipRepository tenantMembershipRepository(DynamoDbTable<TenantMembershipTable> tenantMembershipTable, final ObjectMapper objectMapper) {
        return new TenantMembershipRepository(tenantMembershipTable, objectMapper);
    }

}
