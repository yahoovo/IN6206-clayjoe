package com.clayjoe.backend.dto;

import com.clayjoe.backend.entity.Role;

public record UserResponse(
        Long id,
        String username,
        Role role
) {}