package br.upe.alexapatterns.staff.security;

import jakarta.validation.constraints.AssertTrue;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriedades da conta administrativa.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

  private boolean enabled;
  private String username;
  private String password;

  /**
   * Valida as credenciais quando a criação do administrador está habilitada.
   */
  @AssertTrue(message = "Defina usuário de 3 a 50 caracteres e senha de 12 caracteres ou mais,"
          + " limitada a 72 bytes em UTF-8")
  public boolean isCredentialsValid() {
    if (!enabled) {
      return true;
    }
    return username != null && username.matches("[a-zA-Z0-9._-]{3,50}")
            && password != null && !password.isBlank() && password.length() >= 12
            && password.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}