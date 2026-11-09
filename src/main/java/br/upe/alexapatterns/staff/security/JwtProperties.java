package br.upe.alexapatterns.staff.security;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propriedades de assinatura e validade dos tokens.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  @NotBlank
  @Size(min = 32)
  private String secret;

  @NotBlank
  private String issuer;

  @NotNull
  private Duration expiration;

  /**
   * Verifica se a duração dos tokens é positiva.
   */
  @AssertTrue(message = "app.jwt.expiration deve ser uma duração positiva")
  public boolean isExpirationValid() {
    return expiration != null && !expiration.isNegative() && !expiration.isZero();
  }
}