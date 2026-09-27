package com.siguang.workflow;

import com.siguang.workflow.request.RequestNotFoundException;
import com.siguang.workflow.request.TransitionWorkflowRequest;
import com.siguang.workflow.request.WorkflowRequestController;
import com.siguang.workflow.request.WorkflowRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowRequestController.class)
class WorkflowRequestControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowRequestService service;

    @Test
    void returnsNotFoundForMissingRequest() throws Exception {
        when(service.findById(42L)).thenThrow(new RequestNotFoundException(42L));

        mockMvc.perform(get("/api/requests/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("request not found: 42"));
    }

    @Test
    void returnsConflictForInvalidTransition() throws Exception {
        when(service.transition(eq(7L), any(TransitionWorkflowRequest.class)))
                .thenThrow(new IllegalStateException("completed requests cannot be advanced"));

        mockMvc.perform(post("/api/requests/7/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"ADVANCE\",\"actor\":\"reviewer\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("completed requests cannot be advanced"));
    }

    @Test
    void returnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/requests/7/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"\",\"actor\":\"reviewer\"}"))
                .andExpect(status().isBadRequest());
    }
}
