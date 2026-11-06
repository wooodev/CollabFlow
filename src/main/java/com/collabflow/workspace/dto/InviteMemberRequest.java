package com.collabflow.workspace.dto;

import com.collabflow.workspace.domain.WorkspaceMember.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class InviteMemberRequest {

    @Email
    @NotBlank
    private String email;

    @NotNull
    private MemberRole role;
}
