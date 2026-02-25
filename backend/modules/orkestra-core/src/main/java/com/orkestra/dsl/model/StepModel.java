package com.orkestra.dsl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
public class StepModel {
    @NonNull private String id;
    @NonNull private String type;
    private List<String> dependsOn;
}
