package com.orkestra.app.cursor;

public record WorkflowCursor(
        String tenantId,
        String pk,
        String sk
) {}
