package br.upe.alexapatterns.staff.dto;

import br.upe.alexapatterns.staff.entity.Role;

/**
 * Dados públicos de uma conta de acesso.
 */
public record UserResponse(Long id, String username, Role role) {
}
