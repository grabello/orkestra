package com.orkestra.dsl.validator;

import com.orkestra.dsl.model.DslModel;
import com.orkestra.dsl.model.DslType;
import com.orkestra.exception.WorkflowValidationException;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.parser.ParserException;

import java.util.ArrayList;

public class WorkflowYamlValidator {

    public DslModel validate(final String name, final String yaml) {
        try {
            Yaml yamlParser = new Yaml(new Constructor(DslModel.class, new LoaderOptions()));
            DslModel dslModel = yamlParser.load(yaml);

            if (dslModel.getSteps() == null || dslModel.getSteps().isEmpty()) {
                throw new WorkflowValidationException("WORKFLOW_SCHEMA_INVALID", "Workflow must contain at least one step");
            }

            dslModel.getSteps().forEach(step -> {
                if (step.getType() == null) {
                    throw new WorkflowValidationException("WORKFLOW_SCHEMA_INVALID", "Step is missing DSL type");
                }

                DslType.fromValue(step.getType());

                if (step.getDependsOn() == null) {
                    step.setDependsOn(new ArrayList<>());
                }
            });


            if (!dslModel.getName().equals(name)) {
                throw new WorkflowValidationException("WORKFLOW_NAME_MISMATCH", "Workflow name does not match: " + name);
            }

            return dslModel;
        } catch (ParserException e) {
            throw new WorkflowValidationException("WORKFLOW_SCHEMA_INVALID", "Unable to parse workflow definition: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new WorkflowValidationException("WORKFLOW_SCHEMA_INVALID", "Invalid DSL type: " + e.getMessage());
        }
    }
}
