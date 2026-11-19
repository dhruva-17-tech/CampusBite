package com.campusbite.auth;

import com.campusbite.users.Role;

public record AuthResponse(
        String token,
        String name,
        String email,
        Role role) {
}