package com.orkestra.dsl.validator;

import com.orkestra.dsl.model.DslModel;
import com.orkestra.exception.WorkflowValidationException;
import com.orkestra.graph.model.GraphEdgeModel;
import com.orkestra.graph.model.GraphModel;
import com.orkestra.graph.model.GraphStepModel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class WorkflowDslValidator {

    public GraphModel validate(final DslModel dslModel) {
        final StringBuilder errors = new StringBuilder();
        final Map<String, Set<String>> stepNamesAndDependencies = new HashMap<>();
        final Set<String> allDependencies = new HashSet<>();
        final Map<String, String> stepTypes = new HashMap<>();

        dslModel.getSteps().forEach(step -> {
            if (stepNamesAndDependencies.containsKey(step.getId())) {
                errors.append(step.getId()).append(" is already defined.").append(System.lineSeparator());
            } else {
                Set<String> dependencies = step.getDependsOn() == null ? new HashSet<>() : step.getDependsOn().stream().map(String::valueOf).collect(HashSet::new, Set::add, Set::addAll);
                allDependencies.addAll(dependencies);
                if (dependencies.contains(step.getId())) {
                    errors.append(step.getId()).append(" cannot depend on itself.").append(System.lineSeparator());
                }
                stepNamesAndDependencies.put(step.getId(), dependencies);
                stepTypes.put(step.getId(), step.getType());
            }
        });

        allDependencies.stream().filter(key -> !stepNamesAndDependencies.containsKey(key)).forEach(stepName -> errors.append(stepName).append(" is not defined but it is a dependency.").append(System.lineSeparator()));


        // After basic schema checks, verify there are no cycles using Kahn's algorithm.
        // Also materialize a GraphModel (steps in topological order, edges dep -> node).
        Optional<GraphModel> graphModel = topologicalValidation(stepNamesAndDependencies, stepTypes, errors);

        if (!errors.isEmpty()) {
            throw new WorkflowValidationException("WORKFLOW_SCHEMA_INVALID", errors.toString());
        }

        return graphModel.orElse(null);
    }

    /**
     * Verifies the dependency graph is acyclic using Kahn's algorithm.
     * Graph format: key = stepId, value = set of prerequisite stepIds that "key" depends on.
     * If a cycle exists, throws WorkflowValidationException with details.
     * Returns a GraphModel where:
     * - steps are ordered topologically
     * - edges are (from=dependency, to=dependent)
     * Note: step type is not known here, so it is left null.
     */
    private static Optional<GraphModel> topologicalValidation(final Map<String, Set<String>> graph, Map<String, String> stepTypes, StringBuilder errors) {
        // Build indegree for each node and adjacency from dependency -> dependents.
        final Map<String, Integer> indegree = new HashMap<>();
        final Map<String, Set<String>> adjacency = new HashMap<>();

        // Initialize nodes with indegree 0
        for (String node : graph.keySet()) {
            indegree.put(node, 0);
            adjacency.put(node, new HashSet<>());
        }

        // For each edge dep -> node (because node depends on dep), increase indegree(node)
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String node = entry.getKey();
            for (String dep : entry.getValue()) {
                // Only consider dependencies that are declared nodes (undefined deps already validated earlier)
                if (indegree.containsKey(dep)) {
                    indegree.put(node, indegree.get(node) + 1);
                    adjacency.get(dep).add(node);
                }
            }
        }

        // Collect all nodes with indegree 0
        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        List<String> topoOrder = new ArrayList<>(graph.size());

        while (!queue.isEmpty()) {
            String u = queue.removeFirst();
            topoOrder.add(u);
            for (String v : adjacency.getOrDefault(u, Set.of())) {
                int deg = indegree.get(v) - 1;
                indegree.put(v, deg);
                if (deg == 0) {
                    queue.addLast(v);
                }
            }
        }

        // If we didn't process all nodes, there is a cycle.
        if (topoOrder.size() != graph.size()) {
            // Collect nodes still with indegree > 0 as part of at least one cycle.
            List<String> cyclic = new ArrayList<>();
            for (Map.Entry<String, Integer> e : indegree.entrySet()) {
                if (e.getValue() > 0) {
                    cyclic.add(e.getKey());
                }
            }
            errors.append("Workflow contains cyclic dependencies among steps: ").append(String.join(", ", cyclic)).append(System.lineSeparator());
            return Optional.empty();
        }

        // Build GraphModel:
        // - steps in topological order (type unknown here -> left null)
        return Optional.of(getGraphModel(graph, stepTypes, topoOrder));
    }

    private static GraphModel getGraphModel(Map<String, Set<String>> graph, Map<String, String> stepTypes, List<String> topoOrder) {
        List<GraphStepModel> steps = new ArrayList<>(topoOrder.size());
        for (String id : topoOrder) {
            GraphStepModel step = new GraphStepModel();
            step.setId(id);
            step.setType(stepTypes.get(id));
            // type is unknown at this layer; leave null
            steps.add(step);
        }

        // - edges dep -> node for declared nodes
        List<GraphEdgeModel> edges = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String node = entry.getKey();
            for (String dep : entry.getValue()) {
                if (graph.containsKey(dep)) {
                    GraphEdgeModel e = new GraphEdgeModel();
                    e.setFrom(dep);
                    e.setTo(node);
                    edges.add(e);
                }
            }
        }

        GraphModel model = new GraphModel();
        model.setSteps(steps);
        model.setEdges(edges);
        return model;
    }
}
