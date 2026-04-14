package com.orkestra.app.converter;

import com.orkestra.api.model.Dsl;
import com.orkestra.api.model.DslStep;
import com.orkestra.api.model.Graph;
import com.orkestra.api.model.GraphEdge;
import com.orkestra.api.model.GraphStep;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelConverterTest {

    @Mock
    private CursorCodec cursorCodec;

    @InjectMocks
    private ModelConverter modelConverter;

    @Test
    void fromModelDsl_shouldReturnNull_whenModelIsNull() {
        assertNull(modelConverter.fromModel((DslModel) null));
    }

    @Test
    void fromModelDsl_shouldConvertDslModel() {
        StepModel step1 = new StepModel();
        step1.setId("validate");
        step1.setType("noop");
        step1.setDependsOn(List.of());

        StepModel step2 = new StepModel();
        step2.setId("persist");
        step2.setType("http");
        step2.setDependsOn(List.of("validate"));

        DslModel model = new DslModel();
        model.setName("invoice-processing");
        model.setSteps(List.of(step1, step2));

        Dsl result = modelConverter.fromModel(model);

        assertNotNull(result);
        assertEquals("invoice-processing", result.getName());
        assertNotNull(result.getSteps());
        assertEquals(2, result.getSteps().size());

        DslStep first = result.getSteps().get(0);
        assertEquals("validate", first.getId());
        assertEquals(DslStep.TypeEnum.NOOP, first.getType());
        assertEquals(List.of(), first.getDependsOn());

        DslStep second = result.getSteps().get(1);
        assertEquals("persist", second.getId());
        assertEquals(DslStep.TypeEnum.HTTP, second.getType());
        assertEquals(List.of("validate"), second.getDependsOn());
    }

    @Test
    void fromModelStep_shouldCopyDependsOnIntoNewList() {
        StepModel model = new StepModel();
        model.setId("enrich");
        model.setType("noop");
        model.setDependsOn(List.of("validate"));

        DslStep result = modelConverter.fromModel(model);

        assertNotNull(result);
        assertEquals("enrich", result.getId());
        assertEquals(DslStep.TypeEnum.NOOP, result.getType());
        assertEquals(List.of("validate"), result.getDependsOn());

        // defensive copy check
        assertNotSame(model.getDependsOn(), result.getDependsOn());
    }

    @Test
    void fromModelGraph_shouldReturnNull_whenModelIsNull() {
        assertNull(modelConverter.fromModel((GraphModel) null));
    }

    @Test
    void fromModelGraph_shouldConvertGraphModel() {
        GraphStepModel step1 = new GraphStepModel();
        step1.setId("validate");
        step1.setType("noop");

        GraphStepModel step2 = new GraphStepModel();
        step2.setId("persist");
        step2.setType("http");

        GraphEdgeModel edge = new GraphEdgeModel();
        edge.setFrom("validate");
        edge.setTo("persist");

        GraphModel model = new GraphModel();
        model.setSteps(List.of(step1, step2));
        model.setEdges(List.of(edge));

        Graph result = modelConverter.fromModel(model);

        assertNotNull(result);
        assertNotNull(result.getSteps());
        assertNotNull(result.getEdges());
        assertEquals(2, result.getSteps().size());
        assertEquals(1, result.getEdges().size());

        GraphStep first = result.getSteps().get(0);
        assertEquals("validate", first.getId());
        assertEquals("noop", first.getType());

        GraphEdge firstEdge = result.getEdges().get(0);
        assertEquals("validate", firstEdge.getFrom());
        assertEquals("persist", firstEdge.getTo());
    }

    @Test
    void fromPageResult_shouldReturnItemsWithoutCursor_whenNextKeyIsMissing() {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setWorkflowName("invoice-processing");
        item.setLatestVersion(3);
        item.setTimestamp(OffsetDateTime.parse("2026-02-25T10:15:30Z"));

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult =
                new WorkflowIndexRepository.PageResult<>(List.of(item), null, null);

        ListWorkflowsResponse response = modelConverter.fromPageResult(pageResult);

        assertNotNull(response);
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertNull(response.getNextCursor());

        WorkflowListItem resultItem = response.getItems().get(0);
        assertEquals("invoice-processing", resultItem.getName());
        assertEquals(3, resultItem.getLatestVersion());
        assertEquals(OffsetDateTime.parse("2026-02-25T10:15:30Z"), resultItem.getUpdatedAt());

        verifyNoInteractions(cursorCodec);
    }

    @Test
    void fromPageResult_shouldEncodeCursor_whenNextKeyExists() {
        WorkflowIndexTable item = new WorkflowIndexTable();
        item.setWorkflowName("invoice-processing");
        item.setLatestVersion(3);
        item.setTimestamp(OffsetDateTime.parse("2026-02-25T10:15:30Z"));

        WorkflowIndexRepository.PageResult<WorkflowIndexTable> pageResult =
                new WorkflowIndexRepository.PageResult<>(
                        List.of(item),
                        "TENANT#1",
                        "WF#invoice-processing"
                );

        when(cursorCodec.encode(new WorkflowCursor("1", "TENANT#1", "WF#invoice-processing")))
                .thenReturn("encoded-cursor");

        ListWorkflowsResponse response = modelConverter.fromPageResult(pageResult);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals("encoded-cursor", response.getNextCursor());

        verify(cursorCodec).encode(new WorkflowCursor("1", "TENANT#1", "WF#invoice-processing"));
    }

    @Test
    void fromTableResultWorkflowIndex_shouldConvertToWorkflowListItem() {
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-02-25T10:15:30Z");

        WorkflowIndexTable table = new WorkflowIndexTable();
        table.setWorkflowName("invoice-processing");
        table.setLatestVersion(4);
        table.setTimestamp(timestamp);

        WorkflowListItem result = modelConverter.fromTableResult(table);

        assertNotNull(result);
        assertEquals("invoice-processing", result.getName());
        assertEquals(4, result.getLatestVersion());
        assertEquals(timestamp, result.getUpdatedAt());
    }

    @Test
    void fromTableResultJobDefinition_shouldConvertToWorkflowVersionMetadata() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-02-25T10:15:30Z");

        JobDefinitionTable table = new JobDefinitionTable();
        table.setVersion(7);
        table.setCreatedAt(createdAt);

        WorkflowVersionMetadata result = modelConverter.fromTableResult(table);

        assertNotNull(result);
        assertEquals(7, result.getVersion());
        assertEquals(createdAt, result.getCreatedAt());
    }
}
