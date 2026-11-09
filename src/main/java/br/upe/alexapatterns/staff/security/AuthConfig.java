package br.upe.alexapatterns.staff.security;

import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.staff.repository.StaffUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configura a autenticação das contas persistidas.
 */
@Configuration(proxyBeanMethods = false)
public class AuthConfig {

  /**
   * Carrega as credenciais e o papel de uma conta.
   */
  @Bean
  public UserDetailsService userDetailsService(StaffUserRepository repository) {
    return username -> {
      StaffUser account = repository.findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
      return User.withUsername(account.getUsername())
              .password(account.getPasswordHash())
              .roles(account.getRole().name())
              .build();
    };
  }

  /**
   * Autentica usuário e senha usando BCrypt.
   */
  @Bean
  public AuthenticationManager authenticationManager(
          UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }
}
