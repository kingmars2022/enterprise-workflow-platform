package com.siguang.workflow.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWorkflowRequest(
        @NotBlank String title,
        @NotBlank String type,
        @NotBlank String sourceSystem,
        @NotBlank String targetSystem,
        @NotBlank String priority,
        @NotBlank String description) {
}
