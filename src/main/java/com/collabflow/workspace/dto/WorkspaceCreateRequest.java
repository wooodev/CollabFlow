package com.collabflow.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class WorkspaceCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;
}
