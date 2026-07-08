package com.app.memorias.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String userId, String email, String displayName) {
}