package br.upe.alexapatterns.staff.security;

import br.upe.alexapatterns.staff.entity.Role;
import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.staff.repository.StaffUserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria a conta administrativa inicial quando necessário.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.admin", name = "enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

  private final StaffUserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties properties;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    Optional<StaffUser> existing = repository.findByUsername(properties.getUsername());
    if (existing.isPresent()) {
      if (existing.get().getRole() != Role.ADMIN) {
        throw new IllegalStateException("O usuário configurado já existe sem o papel ADMIN");
      }
      return;
    }
    String passwordHash = passwordEncoder.encode(properties.getPassword());
    repository.save(new StaffUser(properties.getUsername(), passwordHash, Role.ADMIN));
  }
}