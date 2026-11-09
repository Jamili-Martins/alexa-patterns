package br.upe.alexapatterns.staff.service;

import br.upe.alexapatterns.staff.dto.LoginRequest;
import br.upe.alexapatterns.staff.dto.LoginResponse;
import br.upe.alexapatterns.staff.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Autentica credenciais e emite tokens de acesso.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtEncoder jwtEncoder;
  private final JwtProperties properties;

  /**
   * Confere as credenciais e devolve um token para a conta autenticada.
   */
  public LoginResponse login(LoginRequest request) {
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new BadCredentialsException("Credenciais inválidas");
    }
    Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                    request.username(), request.password()));
    return createToken(authentication);
  }

  private LoginResponse createToken(Authentication authentication) {
    Instant now = Instant.now();
    List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring(5))
            .toList();

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .subject(authentication.getName())
            .issuedAt(now)
            .expiresAt(now.plus(properties.getExpiration()))
            .claim("roles", roles)
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new LoginResponse(token, "Bearer", properties.getExpiration().toSeconds());
  }
}