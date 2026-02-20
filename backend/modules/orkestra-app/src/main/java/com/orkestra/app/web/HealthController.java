package com.orkestra.app.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;

@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);


    private final DynamoDbClient dynamo;

    public HealthController(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @GetMapping("/api/health")
//    @Scheduled(fixedRate = 1000)
    public Map<String, Object> health() {
        // Simple call to verify client wiring; LocalStack will respond.
        var tables = dynamo.listTables().tableNames();
        log.info("Tables: " + tables);
        return Map.of(
                "status", "ok",
                "dynamoTables", tables
        );
    }
}
