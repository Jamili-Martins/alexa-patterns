package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.upe.alexapatterns.staff.security.JwtProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica autenticação, validade dos tokens, permissões e propriedade dos dados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Segurança da API")
class ApiSecurityTest extends ApiTestSupport {

  @Autowired
  private JwtDecoder decoder;

  @Autowired
  private JwtEncoder encoder;

  @Autowired
  private JwtProperties properties;

  @Test
  void endpointsProtegidosExigemToken() throws Exception {
    for (String path : List.of(
            "/api/tracks", "/api/playlists", "/api/assistant",
            "/api/assistant/queue", "/api/assistant/panel", "/api/assistant/history")) {
      send(GET, path, null, null).andExpect(status().isUnauthorized());
    }
    for (String path : List.of(
            "/api/staff/users", "/api/tracks", "/api/playlists",
            "/api/assistant/commands", "/api/assistant/queue",
            "/api/assistant/queue/execute", "/api/assistant/queue/execute-next")) {
      send(POST, path, null, Map.of()).andExpect(status().isUnauthorized());
    }
    send(PUT, "/api/assistant/mode", null, Map.of("mode", "SEQUENTIAL"))
            .andExpect(status().isUnauthorized());
    send(PUT, "/api/assistant/panel/subscription", null, null)
            .andExpect(status().isUnauthorized());
    send(DELETE, "/api/assistant/panel/subscription", null, null)
            .andExpect(status().isUnauthorized());
    send(DELETE, "/api/assistant/queue", null, null)
            .andExpect(status().isUnauthorized());
  }

  @Test
  void credenciaisInvalidasRecebemMesmoErro() throws Exception {
    for (Map<String, String> credentials : List.of(
            Map.of("username", username, "password", "SenhaIncorreta!2026"),
            Map.of("username", uniqueUsername("ausente"), "password", PASSWORD))) {
      JsonNode error = json(send(POST, "/api/auth/login", null, credentials), 401);
      assertEquals("Usuário ou senha inválidos", error.path("error").asText());
    }
  }

  @Test
  void loginEmiteTokenValidoComPapelDaConta() throws Exception {
    JsonNode result = json(send(POST, "/api/auth/login", null,
            Map.of("username", username, "password", PASSWORD)), 200);
    assertEquals("Bearer", result.path("tokenType").asText());
    assertEquals(properties.getExpiration().toSeconds(), result.path("expiresIn").asLong());

    Jwt token = decoder.decode(result.path("token").asText());
    assertEquals(username, token.getSubject());
    assertEquals(List.of("USER"), token.getClaimAsStringList("roles"));
    assertEquals(properties.getIssuer(), token.getClaimAsString("iss"));
    send(GET, "/api/tracks", token.getTokenValue(), null).andExpect(status().isOk());
  }

  @Test
  void usuarioComumNaoCadastraContasNemMusicas() throws Exception {
    send(POST, "/api/staff/users", userToken,
            Map.of("username", uniqueUsername("novo"), "password", PASSWORD))
            .andExpect(status().isForbidden());
    send(POST, "/api/tracks", userToken,
            Map.of("title", "Música", "artist", "Artista", "durationSeconds", 180))
            .andExpect(status().isForbidden());
  }

  @Test
  void administradorCadastraUsuarioSemExporSenhaEAdicionaMusica() throws Exception {
    String name = uniqueUsername("novo");
    JsonNode created = json(send(POST, "/api/staff/users", adminToken,
            Map.of("username", name, "password", PASSWORD)), 201);
    assertEquals("USER", created.path("role").asText());
    assertFalse(created.has("password"));
    assertFalse(created.has("passwordHash"));
    String hash = users.findByUsername(name).orElseThrow().getPasswordHash();
    assertFalse(PASSWORD.equals(hash));
    assertTrue(passwordEncoder.matches(PASSWORD, hash));

    long id = track("Faixa cadastrada");
    JsonNode saved = json(send(GET, "/api/tracks/" + id, userToken, null), 200);
    assertEquals("Faixa cadastrada", saved.path("title").asText());
  }

  @Test
  void assinaturaAlteradaInvalidaToken() throws Exception {
    int index = userToken.lastIndexOf('.') + 1;
    char replacement = userToken.charAt(index) == 'A' ? 'B' : 'A';
    String invalid = userToken.substring(0, index) + replacement
            + userToken.substring(index + 1);

    send(GET, "/api/tracks", invalid, null).andExpect(status().isUnauthorized());
  }

  @Test
  void rejeitaTokenExpiradoOuComEmissorIncorreto() throws Exception {
    String expired = signedToken(properties.getIssuer(), Instant.now().minusSeconds(300));
    String wrongIssuer = signedToken(
            properties.getIssuer() + "-outro", Instant.now().plusSeconds(3600));

    for (String invalid : List.of(expired, wrongIssuer)) {
      send(GET, "/api/tracks", invalid, null).andExpect(status().isUnauthorized());
    }
  }

  @Test
  void playlistAlheiaRetorna404InclusiveParaAdministrador() throws Exception {
    long id = playlist(userToken, List.of(track("Faixa privada")));
    String otherToken = login(createUser("outro"));

    send(GET, "/api/playlists/" + id, userToken, null).andExpect(status().isOk());
    for (String token : List.of(otherToken, adminToken)) {
      send(GET, "/api/playlists/" + id, token, null).andExpect(status().isNotFound());
      send(POST, "/api/assistant/commands", token, commandBody("PLAY", id))
              .andExpect(status().isNotFound());
      send(POST, "/api/assistant/queue", token, commandBody("PLAY", id))
              .andExpect(status().isNotFound());
    }
  }

  private String signedToken(String issuer, Instant expiration) {
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(username)
            .issuedAt(Instant.now().minusSeconds(7200))
            .expiresAt(expiration)
            .claim("roles", List.of("USER"))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}