package com.orkestra.dsl.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Data
@NoArgsConstructor
public class DslModel {

    @NonNull private String name;
    @NonNull private List<StepModel> steps;
}
