package br.upe.alexapatterns.staff.controller;

import br.upe.alexapatterns.staff.dto.LoginRequest;
import br.upe.alexapatterns.staff.dto.LoginResponse;
import br.upe.alexapatterns.staff.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público de autenticação.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

  private final AuthService service;

  /**
   * Autentica uma conta e devolve seu token de acesso.
   */
  @PostMapping("/login")
  @SecurityRequirements
  @Operation(summary = "Autentica uma conta e emite um token JWT")
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return service.login(request);
  }
}
