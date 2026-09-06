package com.siguang.workflow.request;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
public class WorkflowRequestController {
    private final WorkflowRequestService service;

    public WorkflowRequestController(WorkflowRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WorkflowRequestResponse> create(
            @Valid @RequestBody CreateWorkflowRequest command) {
        WorkflowRequestResponse response = service.create(command);
        return ResponseEntity
                .created(URI.create("/api/requests/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<WorkflowRequestResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public WorkflowRequestResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping("/{id}/transition")
    public WorkflowRequestResponse transition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionWorkflowRequest command) {
        return service.transition(id, command);
    }
}
