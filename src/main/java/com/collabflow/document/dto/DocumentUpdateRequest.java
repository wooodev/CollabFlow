package com.collabflow.document.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class DocumentUpdateRequest {

    @Size(max = 500)
    private String title;
}
