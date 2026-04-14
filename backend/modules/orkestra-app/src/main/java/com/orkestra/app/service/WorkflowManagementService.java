package com.orkestra.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orkestra.api.model.Dsl;
import com.orkestra.api.model.Graph;
import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.RegisterWorkflowResponse;
import com.orkestra.api.model.WorkflowVersion;
import com.orkestra.api.model.WorkflowVersionMetadata;
import com.orkestra.app.converter.ModelConverter;
import com.orkestra.app.cursor.CursorCodec;
import com.orkestra.app.cursor.WorkflowCursor;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.dsl.validator.WorkflowDslValidator;
import com.orkestra.dsl.validator.WorkflowYamlValidator;
import com.orkestra.exception.WorkflowNotFoundException;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.storage.dynamodb.JobDefinitionRepository;
import com.orkestra.storage.dynamodb.WorkflowIndexRepository;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class WorkflowManagementService {

    private final WorkflowYamlValidator workflowYamlValidator;
    private final WorkflowDslValidator workflowDslValidator;
    private final WorkflowIndexRepository workflowIndexRepository;
    private final JobDefinitionRepository jobDefinitionRepository;
    private final ModelConverter modelConverter;
    private final CursorCodec cursorCodec;
    private final ObjectMapper objectMapper;

    public RegisterWorkflowResponse register(String tenant, String name, String yaml) {
        final OffsetDateTime now = OffsetDateTime.now();
        DslModel dslModel = workflowYamlValidator.validate(name, yaml);
        GraphModel graphModel = workflowDslValidator.validate(dslModel);

        final WorkflowIndexTable workflowIndex = workflowIndexRepository.getWorkflowIndex(tenant, name);

        final Integer newVersion = workflowIndex == null ? 1 : workflowIndex.getLatestVersion() + 1;

        jobDefinitionRepository.save(tenant, name, newVersion, now, graphModel, dslModel);

        workflowIndexRepository.save(tenant, name, now, newVersion);

        return new RegisterWorkflowResponse().name(name).createdAt(now).version(newVersion);
    }


    public WorkflowVersion getWorkflowVersion(String tenantId, String name, Integer version) {
        JobDefinitionTable jobDefinition = jobDefinitionRepository.getJobDefinition(tenantId, name, version);

        if (jobDefinition == null) {
            throw new WorkflowNotFoundException("WORKFLOW_NOT_FOUND", "Workflow " + name + " version " + version + " not found");
        }

        GraphModel graphModel = getModelFromJson(jobDefinition.getGraphJson(), GraphModel.class);
        DslModel dslModel = getModelFromJson(jobDefinition.getDslJson(), DslModel.class);

        final Dsl dsl = modelConverter.fromModel(dslModel);
        final Graph graph = modelConverter.fromModel(graphModel);

        return new WorkflowVersion().name(name).dsl(dsl).graph(graph).version(version).createdAt(jobDefinition.getCreatedAt());
    }

    private <T> T getModelFromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse json", e);
        }
    }

    public ListWorkflowsResponse listWorkflows(String tenantId, String cursor, Integer limit) {
        final WorkflowCursor workflowCursor = cursor == null || cursor.isBlank() ? new WorkflowCursor(tenantId, null, null) : cursorCodec.decode(cursor);
        WorkflowIndexRepository.PageResult<WorkflowIndexTable> workflowIndexTablePageResult = workflowIndexRepository.listWorkflowIndexes(tenantId, limit, workflowCursor.pk(), workflowCursor.sk());
        return modelConverter.fromPageResult(workflowIndexTablePageResult);
    }

    public ListWorkflowVersionsResponse listWorkflowVersions(String tenantId, String name) {
        List<JobDefinitionTable> jobDefinitionVersions = jobDefinitionRepository.getJobDefinitionVersions(tenantId, name);
        ListWorkflowVersionsResponse response = new ListWorkflowVersionsResponse();
        response.setName(name);
        List<WorkflowVersionMetadata> workflowVersionMetadata = jobDefinitionVersions.stream().map(modelConverter::fromTableResult).toList();
        response.setVersions(workflowVersionMetadata);
        return response;
    }
}
