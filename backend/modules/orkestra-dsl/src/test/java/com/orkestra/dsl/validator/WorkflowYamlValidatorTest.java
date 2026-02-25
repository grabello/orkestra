package com.orkestra.dsl.validator;

import com.orkestra.dsl.model.DslModel;
import com.orkestra.exception.WorkflowValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowYamlValidatorTest {

    private final WorkflowYamlValidator validator = new WorkflowYamlValidator();

    @Test
    @DisplayName("Valid workflow YAML parses into DslModel")
    void validWorkflowParses() {
        String yaml = loadResource("workflows/01-valid.yaml");
        // The YAML file contains the workflow name; pass the same value here.
        String expectedName = extractExpectedName(yaml);

        DslModel model = validator.validate(expectedName, yaml);

        assertThat(model).isNotNull();
        assertThat(model.getName()).isEqualTo(expectedName);
        assertThat(model.getSteps()).isNotEmpty();
    }

    @Test
    @DisplayName("Invalid YAML syntax -> WORKFLOW_SCHEMA_INVALID")
    void invalidYamlSyntax() {
        String yaml = loadResource("workflows/02-invalid-yaml-syntax.yaml");
        String expectedName = "invalid-yaml-syntax";

        assertThatThrownBy(() -> validator.validate(expectedName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Unable to parse workflow definition")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Missing steps -> WORKFLOW_SCHEMA_INVALID")
    void missingSteps() {
        String yaml = loadResource("workflows/03-invalid-missing-steps.yaml");
        String expectedName = extractExpectedName(yaml);

        assertThatThrownBy(() -> validator.validate(expectedName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("at least one step")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Empty steps -> WORKFLOW_SCHEMA_INVALID")
    void emptySteps() {
        String yaml = loadResource("workflows/04-invalid-empty-steps.yaml");
        String expectedName = extractExpectedName(yaml);

        assertThatThrownBy(() -> validator.validate(expectedName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("at least one step")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Missing step type -> WORKFLOW_SCHEMA_INVALID")
    void missingStepType() {
        String yaml = loadResource("workflows/09-invalid-missing-step-type.yaml");
        String expectedName = extractExpectedName(yaml);

        assertThatThrownBy(() -> validator.validate(expectedName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Step is missing DSL type")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Invalid step type enum -> WORKFLOW_SCHEMA_INVALID")
    void invalidStepType() {
        String yaml = loadResource("workflows/10-invalid-invalid-step-type.yaml");
        String expectedName = extractExpectedName(yaml);

        assertThatThrownBy(() -> validator.validate(expectedName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Invalid DSL type")
                .extracting("code").isEqualTo("WORKFLOW_SCHEMA_INVALID");
    }

    @Test
    @DisplayName("Provided name different from YAML name -> WORKFLOW_NAME_MISMATCH")
    void nameMismatch() {
        String yaml = loadResource("workflows/01-valid.yaml");
        String wrongName = "some-other-name";

        assertThatThrownBy(() -> validator.validate(wrongName, yaml))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Workflow name does not match")
                .extracting("code").isEqualTo("WORKFLOW_NAME_MISMATCH");
    }

    // --- helpers ---

    private static String loadResource(String path) {
        try (InputStream in = WorkflowYamlValidatorTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + path);
            }
            byte[] bytes = in.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource: " + path, e);
        }
    }

    // Extract the expected workflow name from the YAML content (very small helper for tests)
    protected static String extractExpectedName(String yaml) {
        // naive extraction: first line like "name: xyz"
        for (String line : yaml.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("name:")) {
                return trimmed.substring("name:".length()).trim();
            }
        }
        // Fallback so tests can still run even if 'name:' isn't first
        return "workflow";
    }
}
