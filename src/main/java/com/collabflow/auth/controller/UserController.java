package com.collabflow.auth.controller;

import com.collabflow.auth.domain.User;
import com.collabflow.auth.dto.UserResponse;
import com.collabflow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Operation(summary = "내 프로필 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(UserResponse.from(user));
    }
}
