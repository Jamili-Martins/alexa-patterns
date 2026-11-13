package br.upe.alexapatterns.staff.dto;

/**
 * Token de acesso e seu prazo de validade em segundos.
 */
public record LoginResponse(String token, String tokenType, long expiresIn) {
}
