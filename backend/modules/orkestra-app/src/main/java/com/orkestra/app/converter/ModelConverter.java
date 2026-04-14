package com.orkestra.app.converter;

import com.orkestra.api.model.Dsl;
import com.orkestra.api.model.DslStep;
import com.orkestra.api.model.Graph;
import com.orkestra.api.model.GraphEdge;
import com.orkestra.api.model.GraphStep;
import com.orkestra.api.model.ListWorkflowVersionsResponse;
import com.orkestra.api.model.ListWorkflowsResponse;
import com.orkestra.api.model.WorkflowListItem;
import com.orkestra.api.model.WorkflowVersionMetadata;
import com.orkestra.app.cursor.CursorCodec;
import com.orkestra.app.cursor.WorkflowCursor;
import com.orkestra.dsl.model.DslModel;
import com.orkestra.dsl.model.StepModel;
import com.orkestra.graph.model.GraphEdgeModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;
import com.orkestra.storage.dynamodb.WorkflowIndexRepository;
import com.orkestra.storage.dynamodb.model.JobDefinitionTable;
import com.orkestra.storage.dynamodb.model.WorkflowIndexTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Component to convert between DTO and model.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelConverter {

    private final CursorCodec cursorCodec;

    public Dsl fromModel(DslModel model) {
        if (model == null) {
            return null;
        }

        log.debug("Converting model {} to DTO", model);

        final Dsl dsl = new Dsl();
        dsl.setName(model.getName());

        final List<DslStep> dslSteps = model.getSteps().stream().map(this::fromModel).toList();
        dsl.setSteps(dslSteps);
        return dsl;
    }

    public DslStep fromModel(StepModel model) {
        final DslStep dslStep = new DslStep();
        dslStep.setDependsOn(new ArrayList<>(model.getDependsOn()));
        dslStep.setId(model.getId());
        dslStep.setType(DslStep.TypeEnum.fromValue(model.getType()));
        return dslStep;
    }

    public Graph fromModel(GraphModel model) {
        if (model == null) {
            return null;
        }
        final Graph graph = new Graph();
        graph.setSteps(model.getSteps().stream().map(this::fromModel).toList());
        graph.setEdges(model.getEdges().stream().map(this::fromModel).toList());
        return graph;
    }

    public GraphStep fromModel(GraphStepModel model) {
        final GraphStep graphStep = new GraphStep();
        graphStep.setId(model.getId());
        graphStep.setType(model.getType());
        return graphStep;

    }

    public GraphEdge fromModel(GraphEdgeModel model) {
        final GraphEdge graphEdge = new GraphEdge();
        graphEdge.setFrom(model.getFrom());
        graphEdge.setTo(model.getTo());
        return graphEdge;
    }

    public ListWorkflowsResponse fromPageResult(WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult) {
        List<WorkflowIndexTable> items = pageResult.items();
        List<WorkflowListItem> list = items.stream().map(this::fromTableResult).toList();

        ListWorkflowsResponse response = new ListWorkflowsResponse();
        response.setItems(list);
        if (pageResult.nextPk() == null || pageResult.nextSk() == null) {
            return response;
        }

        String nextCursor = cursorCodec.encode(new WorkflowCursor("1", pageResult.nextPk(), pageResult.nextSk()));
        response.setNextCursor(nextCursor);
        return response;
    }

    public WorkflowListItem fromTableResult(WorkflowIndexTable workflowIndexTable) {

        WorkflowListItem workflowListItem = new WorkflowListItem();
        workflowListItem.setName(workflowIndexTable.getWorkflowName());
        workflowListItem.setLatestVersion(workflowIndexTable.getLatestVersion());
        workflowListItem.setUpdatedAt(workflowIndexTable.getTimestamp());
        return workflowListItem;
    }

    public WorkflowVersionMetadata fromTableResult(JobDefinitionTable jobDefinitionTable) {
        final WorkflowVersionMetadata workflowVersionMetadata = new WorkflowVersionMetadata();
        workflowVersionMetadata.setVersion(jobDefinitionTable.getVersion());
        workflowVersionMetadata.setCreatedAt(jobDefinitionTable.getCreatedAt());
        return workflowVersionMetadata;
    };

}
