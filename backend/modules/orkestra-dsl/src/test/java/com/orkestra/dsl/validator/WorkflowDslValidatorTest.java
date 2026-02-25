package com.orkestra.dsl.validator;

import com.orkestra.dsl.model.DslModel;
import com.orkestra.exception.WorkflowValidationException;
import com.orkestra.graph.model.GraphEdgeModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowDslValidatorTest {

    private final WorkflowYamlValidator yamlValidator = new WorkflowYamlValidator();
    private final WorkflowDslValidator dslValidator = new WorkflowDslValidator();

    @Test
    @DisplayName("Valid workflow -> builds acyclic graph with correct edges and topological order")
    void validWorkflowBuildsGraph() {
        String yaml = loadResource("workflows/01-valid.yaml");
        String expectedName = extractExpectedName(yaml);

        // First parse YAML into DslModel
        DslModel dslModel = yamlValidator.validate(expectedName, yaml);

        // Then validate DSL and build GraphModel
        GraphModel graph = dslValidator.validate(dslModel);

        assertThat(graph).isNotNull();

        // Steps count should match declared steps count
        int declaredSteps = dslModel.getSteps().size();
        assertThat(graph.getSteps()).hasSize(declaredSteps);

        // Edges count should match number of declared dependencies among declared nodes
        Set<String> declaredIds = dslModel.getSteps().stream()
                .map(s -> String.valueOf(s.getId()))
                .collect(Collectors.toSet());
        int expectedEdges = dslModel.getSteps().stream()
                .map(s -> s.getDependsOn() == null ? List.<String>of() : s.getDependsOn())
                .flatMap(Collection::stream)
                .map(String::valueOf)
                .filter(declaredIds::contains)
                .mapToInt(x -> 1)
                .sum();

        assertThat(graph.getEdges()).hasSize(expectedEdges);

        // Topological order: for every edge (from -> to), index(from) < index(to)
        Map<String, Integer> indexById = new HashMap<>();
        List<GraphStepModel> topoSteps = graph.getSteps();
        for (int i = 0; i < topoSteps.size(); i++) {
            indexById.put(topoSteps.get(i).getId(), i);
        }
        for (GraphEdgeModel e : graph.getEdges()) {
            Integer fromIdx = indexById.get(e.getFrom());
            Integer toIdx = indexById.get(e.getTo());
            assertThat(fromIdx)
                    .withFailMessage("Missing step id in graph: %s", e.getFrom())
                    .isNotNull();
            assertThat(toIdx)
                    .withFailMessage("Missing step id in graph: %s", e.getTo())
                    .isNotNull();
            assertThat(fromIdx).isLessThan(toIdx);
        }

        // Step types should be propagated into GraphStepModel
        assertThat(graph.getSteps())
                .extracting(GraphStepModel::getType)
                .allMatch(Objects::nonNull);
    }

    @Test
    @DisplayName("Duplicate step id -> WORKFLOW_SCHEMA_INVALID (contains 'is already defined.')")
    void duplicateStepId() {
        String yaml = loadResource("workflows/05-invalid-duplicate-step-id.yaml");
        String name = extractExpectedName(yaml);

        DslModel dslModel = yamlValidator.validate(name, yaml);

        assertThatThrownBy(() -> dslValidator.validate(dslModel))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("is already defined.")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Unknown dependency -> WORKFLOW_SCHEMA_INVALID (contains 'is not defined but it is a dependency.')")
    void unknownDependency() {
        String yaml = loadResource("workflows/06-invalid-unknown-dependency.yaml");
        String name = extractExpectedName(yaml);

        DslModel dslModel = yamlValidator.validate(name, yaml);

        assertThatThrownBy(() -> dslValidator.validate(dslModel))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("is not defined but it is a dependency.")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Self dependency -> WORKFLOW_SCHEMA_INVALID (contains 'cannot depend on itself.')")
    void selfDependency() {
        String yaml = loadResource("workflows/07-invalid-self-dependency.yaml");
        String name = extractExpectedName(yaml);

        DslModel dslModel = yamlValidator.validate(name, yaml);

        assertThatThrownBy(() -> dslValidator.validate(dslModel))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("cannot depend on itself.")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Cyclic dependencies -> WORKFLOW_SCHEMA_INVALID (contains cycle message)")
    void cyclicDependencies() {
        String yaml = loadResource("workflows/08-invalid-cycle.yaml");
        String name = extractExpectedName(yaml);

        DslModel dslModel = yamlValidator.validate(name, yaml);

        assertThatThrownBy(() -> dslValidator.validate(dslModel))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Workflow contains cyclic dependencies among steps:")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    // --- helpers ---

    private static String loadResource(String path) {
        try (InputStream in = WorkflowDslValidatorTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + path);
            }
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }

    private static String extractExpectedName(String yaml) {
        for (String line : yaml.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("name:")) {
                return trimmed.substring("name:".length()).trim();
            }
        }
        return "workflow";
    }
}
