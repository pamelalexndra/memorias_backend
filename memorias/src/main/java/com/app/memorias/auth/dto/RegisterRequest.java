package com.app.memorias.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Correo inválido") @NotBlank(message = "El correo es obligatorio") String email,
        @NotBlank(message = "El usuario es obligatorio") @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres") String username,
        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres") String password) {
}


