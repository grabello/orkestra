package com.orkestra.app.web;

import com.orkestra.api.model.HealthCheck;
import com.orkestra.app.web.generated.SystemApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Map;

@RestController
public class HealthController implements SystemApi {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);


    private final DynamoDbClient dynamo;

    public HealthController(DynamoDbClient dynamo) {
        this.dynamo = dynamo;
    }

    @Override
    public ResponseEntity<HealthCheck> getHealth() {
        // Simple call to verify client wiring; LocalStack will respond.
        var tables = dynamo.listTables().tableNames();
        log.info("Tables: " + tables);
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setStatus("ok");
        healthCheck.setTables(tables);
        return ResponseEntity.ok(healthCheck);

    }
}
