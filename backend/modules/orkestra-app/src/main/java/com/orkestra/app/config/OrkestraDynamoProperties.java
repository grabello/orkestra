package com.orkestra.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orkestra.dynamodb")
public record OrkestraDynamoProperties(Tables tables) {

    public record Tables(String jobDefinitions, String workflowIndex) {
    }
}
