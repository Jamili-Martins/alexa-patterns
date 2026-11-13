package br.upe.alexapatterns.staff.service;

import br.upe.alexapatterns.exception.BadRequestException;
import br.upe.alexapatterns.exception.ConflictException;
import br.upe.alexapatterns.staff.dto.CreateUserRequest;
import br.upe.alexapatterns.staff.dto.UserResponse;
import br.upe.alexapatterns.staff.entity.Role;
import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.staff.repository.StaffUserRepository;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra as contas de usuário da aplicação.
 */
@Service
@RequiredArgsConstructor
public class StaffUserService {

  private final StaffUserRepository repository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Cria uma conta USER mediante autorização de um administrador.
   */
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse create(CreateUserRequest request) {
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new BadRequestException("A senha deve ter no máximo 72 bytes em UTF-8");
    }
    if (repository.findByUsername(request.username()).isPresent()) {
      throw new ConflictException("Nome de usuário já cadastrado");
    }

    String passwordHash = passwordEncoder.encode(request.password());
    StaffUser account = new StaffUser(request.username(), passwordHash, Role.USER);
    try {
      account = repository.saveAndFlush(account);
    } catch (DataIntegrityViolationException exception) {
      throw new ConflictException("Nome de usuário já cadastrado");
    }
    return new UserResponse(account.getId(), account.getUsername(), account.getRole());
  }
}
