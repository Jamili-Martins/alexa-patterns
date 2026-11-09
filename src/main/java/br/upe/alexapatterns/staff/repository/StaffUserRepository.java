package br.upe.alexapatterns.staff.repository;

import br.upe.alexapatterns.staff.entity.StaffUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistência das contas de acesso.
 */
public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {

  Optional<StaffUser> findByUsername(String username);
}
