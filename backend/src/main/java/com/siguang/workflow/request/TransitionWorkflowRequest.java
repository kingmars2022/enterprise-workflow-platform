package com.siguang.workflow.request;

import jakarta.validation.constraints.NotBlank;

public record TransitionWorkflowRequest(
        @NotBlank String action,
        @NotBlank String actor,
        String comment) {
}
