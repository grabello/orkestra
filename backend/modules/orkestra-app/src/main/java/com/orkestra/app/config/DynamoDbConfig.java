package com.orkestra.app.config;

import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${orkestra.aws.region:us-east-1}") String region,
            @Value("${orkestra.dynamodb.endpoint:}") String endpointOverride,
            AwsCredentialsProvider awsCredentialsProvider
    ) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider);

        // LocalStack / custom endpoint support
        if (endpointOverride != null && !endpointOverride.isBlank()) {
            builder.endpointOverride(URI.create(endpointOverride));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    }

    @Bean
    public DynamoDbTable<JobDefinitionTable> jobDefinitionTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, final OrkestraDynamoProperties props) {
        return dynamoDbEnhancedClient.table(props.tables().jobDefinitions(), TableSchema.fromBean(JobDefinitionTable.class));
    }

    @Bean
    public DynamoDbTable<WorkflowIndexTable> workflowIndexTable(DynamoDbEnhancedClient dynamoDbEnhancedClient, final OrkestraDynamoProperties props) {
        return dynamoDbEnhancedClient.table(props.tables().workflowIndex(), TableSchema.fromBean(WorkflowIndexTable.class));
    }

    /**
     * Local dev: static dummy creds for LocalStack.
     * AWS: DefaultCredentialsProvider (env/instance profile/SSO, etc).
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(
            @Value("${orkestra.aws.local:false}") boolean localMode,
            @Value("${orkestra.aws.accessKey:}") String accessKey,
            @Value("${orkestra.aws.secretKey:}") String secretKey
    ) {
        if (localMode) {
            // LocalStack accepts any creds; we still provide stable ones.
            var ak = (accessKey == null || accessKey.isBlank()) ? "test" : accessKey;
            var sk = (secretKey == null || secretKey.isBlank()) ? "test" : secretKey;
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(ak, sk));
        }
        return DefaultCredentialsProvider.create();
    }
}
