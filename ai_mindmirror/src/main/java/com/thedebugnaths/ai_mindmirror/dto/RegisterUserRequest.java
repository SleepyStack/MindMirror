package com.thedebugnaths.ai_mindmirror.dto;

public record RegisterUserRequest(
        String username,
        String email,
        String password
) {}
