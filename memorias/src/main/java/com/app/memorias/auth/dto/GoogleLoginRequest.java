package com.app.memorias.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank(message = "El idToken de Google es obligatorio") String idToken) {
}