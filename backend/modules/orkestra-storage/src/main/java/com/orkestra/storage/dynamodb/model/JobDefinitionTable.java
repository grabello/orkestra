package com.orkestra.storage.dynamodb.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
@DynamoDbBean
public class JobDefinitionTable {

    private String name;

    private Integer version;

    private OffsetDateTime createdAt;

    private String definition;

    private List<String> topoOrder;

    private List<Map<String, String>> edges;

    private String tenant;

    private String dslJson;

    private String graphJson;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("pk")
    public String getPk() {
        return "TENANT#" + this.tenant + "#WF#" + this.name;
    }

    public void setPk(String pk) {
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("sk")
    public String getSk() {
        return "VERSION#" + this.version;
    }

    public void setSk(String sk) {
        // no-op: derived from version
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbIgnore
    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getTopoOrder() {
        return topoOrder;
    }

    public void setTopoOrder(List<String> topoOrder) {
        this.topoOrder = topoOrder;
    }

    public List<Map<String, String>> getEdges() {
        return edges;
    }

    public void setEdges(List<Map<String, String>> edges) {
        this.edges = edges;
    }

    public String getDslJson() {
        return dslJson;
    }

    public void setDslJson(String dslJson) {
        this.dslJson = dslJson;
    }

    public String getGraphJson() {
        return graphJson;
    }

    public void setGraphJson(String graphJson) {
        this.graphJson = graphJson;
    }
}
