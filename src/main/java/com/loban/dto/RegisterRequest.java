package com.loban.dto;

import com.loban.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nom complet requis")
        String fullname,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, message = "Mot de passe: au moins 8 caractères")
        String password,

        @NotNull
        Role role
) {
}
