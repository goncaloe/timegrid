package com.timegrid.controller;

import com.timegrid.dto.CreateWorkflowTransitionRequest;
import com.timegrid.dto.WorkflowTransitionResponse;
import com.timegrid.entity.WorkflowTransition;
import com.timegrid.service.WorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow/transitions")
@RequiredArgsConstructor
public class WorkflowTransitionController {

    private final WorkflowService workflowService;

    @GetMapping
    @PreAuthorize("hasAuthority('workflow:manage')")
    public List<WorkflowTransitionResponse> findAll() {
        return workflowService.findAll().stream()
                .map(WorkflowTransitionResponse::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('workflow:manage')")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowTransitionResponse create(@Valid @RequestBody CreateWorkflowTransitionRequest request) {
        WorkflowTransition transition = workflowService.create(
                request.entityType(), request.fromStatus(), request.toStatus(), request.role());
        return WorkflowTransitionResponse.from(transition);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('workflow:manage')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        workflowService.delete(id);
    }

}