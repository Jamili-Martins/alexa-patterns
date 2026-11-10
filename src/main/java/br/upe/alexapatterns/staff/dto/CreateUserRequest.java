package br.upe.alexapatterns.staff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para cadastrar uma conta de usuário.
 */
public record CreateUserRequest(
        @NotBlank
        @Pattern(regexp = "[a-zA-Z0-9._-]{3,50}")
        String username,
        @NotBlank
        @Size(min = 12, max = 72)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password) {

  @Override
  public String toString() {
    return "CreateUserRequest[username=" + username + ", password=***]";
  }
}
