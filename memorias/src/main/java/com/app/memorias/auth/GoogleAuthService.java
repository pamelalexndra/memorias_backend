package com.app.memorias.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class GoogleAuthService {
    @Value("${app.google.client-id}")
    private String googleClientId;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUser verifyIdToken(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GOOGLE_CLIENT_ID no está configurado en el backend");
        }
        URI uri = UriComponentsBuilder.fromUriString("https://oauth2.googleapis.com/tokeninfo").queryParam("id_token", idToken).build().toUri();
        Map<?, ?> response;
        try {
            response = restTemplate.getForObject(uri, Map.class);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de Google inválido");
        }
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se pudo validar el token de Google");
        }
        String audience = String.valueOf(response.get("aud"));
        String googleUserId = String.valueOf(response.get("sub"));
        String email = String.valueOf(response.get("email"));
        String name = String.valueOf(response.get("name"));
        String emailVerified = String.valueOf(response.get("email_verified"));
        if (!googleClientId.equals(audience)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token de Google no pertenece a esta aplicación");
        }
        if (!"true".equalsIgnoreCase(emailVerified)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El correo de Google no está verificado");
        }
        return new GoogleUser(googleUserId, email, name);
    }

    public record GoogleUser(String googleId, String email, String displayName) {
    }
}