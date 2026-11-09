package br.upe.alexapatterns.staff.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Conta de acesso com senha armazenada como hash.
 */
@Entity
@Table(name = "staff_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @JsonIgnore
  @Column(name = "password_hash", nullable = false, length = 60)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  /**
   * Cria uma conta com a senha previamente codificada.
   */
  public StaffUser(String username, String passwordHash, Role role) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
  }
}
