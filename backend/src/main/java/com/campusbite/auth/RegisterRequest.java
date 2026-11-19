package com.campusbite.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Name is required") @Size(max = 60, message = "Name cannot exceed 60 characters") String name,

        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must contain 8 to 72 characters") String password,

        String role,           // optional: CUSTOMER (default), STAFF or ADMIN

        String assignedOutlet, // required for STAFF

        String accessCode      // required for STAFF / ADMIN when a code is configured

) {
}