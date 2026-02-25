package com.orkestra.graph.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GraphModel {

    private List<GraphStepModel> steps;
    private List<GraphEdgeModel> edges;
}
