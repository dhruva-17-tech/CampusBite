package com.campusbite.admin;
import com.campusbite.users.Role;
import jakarta.validation.constraints.*;
public record CreateTeamUserRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank @Size(min=8,max=72) String password,
    @NotNull Role role,
    String assignedOutlet
) {}
