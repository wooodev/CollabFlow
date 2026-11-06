package com.collabflow.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DocumentCreateRequest {

    @NotBlank
    @Size(max = 500)
    private String title;
}
