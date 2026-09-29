package com.loyalty.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "name is required") @Size(max = 100) String name,
        @NotBlank(message = "email is required") @Email(message = "email is invalid") String email,
        @Size(max = 20) String phone) {}
