package br.upe.alexapatterns.staff.controller;

import br.upe.alexapatterns.staff.dto.CreateUserRequest;
import br.upe.alexapatterns.staff.dto.UserResponse;
import br.upe.alexapatterns.staff.service.StaffUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza o cadastro de usuários ao administrador.
 */
@RestController
@RequestMapping("/api/staff/users")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class StaffUserController {

  private final StaffUserService service;

  /**
   * Cadastra uma conta de usuário.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Cadastra um usuário com papel USER")
  public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
    return service.create(request);
  }
}
