package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.upe.alexapatterns.staff.entity.Role;
import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.staff.repository.StaffUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Prepara contas exclusivas e executa requisições com autenticação real.
 */
abstract class ApiTestSupport {

  protected static final String PASSWORD = "SenhaTeste!2026Forte";

  @Autowired
  protected MockMvc mvc;

  @Autowired
  protected ObjectMapper mapper;

  @Autowired
  protected StaffUserRepository users;

  @Autowired
  protected PasswordEncoder passwordEncoder;

  protected String adminToken;
  protected String userToken;
  protected String username;

  @BeforeEach
  void prepareAccounts() throws Exception {
    String admin = uniqueUsername("admin");
    users.save(new StaffUser(admin, passwordEncoder.encode(PASSWORD), Role.ADMIN));
    adminToken = login(admin);
    username = createUser("user");
    userToken = login(username);
  }

  protected String uniqueUsername(String prefix) {
    return prefix + "_" + UUID.randomUUID();
  }

  protected String createUser(String prefix) throws Exception {
    String name = uniqueUsername(prefix);
    JsonNode created = json(send(POST, "/api/staff/users", adminToken,
            Map.of("username", name, "password", PASSWORD)), 201);
    assertEquals("USER", created.path("role").asText());
    return name;
  }

  protected String login(String name) throws Exception {
    JsonNode result = json(send(POST, "/api/auth/login", null,
            Map.of("username", name, "password", PASSWORD)), 200);
    String token = result.path("token").asText();
    assertFalse(token.isBlank());
    return token;
  }

  protected ResultActions send(
          HttpMethod method, String path, String token, Object body) throws Exception {
    MockHttpServletRequestBuilder request = request(method, path);
    if (token != null) {
      request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
    if (body != null) {
      request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsBytes(body));
    }
    return mvc.perform(request);
  }

  protected JsonNode json(ResultActions result, int expectedStatus) throws Exception {
    return mapper.readTree(result.andExpect(status().is(expectedStatus))
            .andReturn().getResponse().getContentAsByteArray());
  }

  protected long createdId(ResultActions result) throws Exception {
    JsonNode created = json(result, 201);
    assertTrue(created.path("id").isIntegralNumber());
    long id = created.path("id").asLong();
    assertTrue(id > 0);
    return id;
  }

  protected long track(String title) throws Exception {
    return createdId(send(POST, "/api/tracks", adminToken,
            Map.of("title", title, "artist", "Artista", "durationSeconds", 180)));
  }

  protected long playlist(String token, List<Long> ids) throws Exception {
    return createdId(send(POST, "/api/playlists", token,
            Map.of("name", "Revisão", "trackIds", ids)));
  }

  protected Map<String, Object> commandBody(String action, Long playlistId) {
    if (playlistId == null) {
      return Map.of("action", action);
    }
    return Map.of("action", action, "playlistId", playlistId);
  }

  protected JsonNode command(String action, Long playlistId) throws Exception {
    return json(send(POST, "/api/assistant/commands", userToken,
            commandBody(action, playlistId)), 200);
  }

  protected JsonNode enqueue(String action, Long playlistId) throws Exception {
    return json(send(POST, "/api/assistant/queue", userToken,
            commandBody(action, playlistId)), 201);
  }

  protected void assertPlayback(JsonNode value, String expectedStatus, Long trackId) {
    assertEquals(expectedStatus, value.path("status").asText());
    if (trackId == null) {
      assertTrue(value.path("currentTrack").isNull());
    } else {
      assertEquals(trackId.longValue(), value.path("currentTrack").path("id").asLong());
    }
  }
}