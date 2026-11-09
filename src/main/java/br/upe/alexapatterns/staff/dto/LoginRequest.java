package br.upe.alexapatterns.staff.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenciais recebidas no login.
 */
public record LoginRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(max = 72)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password) {

  @Override
  public String toString() {
    return "LoginRequest[username=" + username + ", password=***]";
  }
}