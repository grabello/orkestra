package com.orkestra.graph.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphModel {

    private List<GraphStepModel> steps;
    private List<GraphEdgeModel> edges;
}
