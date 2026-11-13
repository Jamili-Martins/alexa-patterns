package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica a colaboração dos cinco padrões pelos endpoints reais da aplicação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Fluxo HTTP completo")
class HttpFlowTest extends ApiTestSupport {

  @Test
  void rotinaIntegraPadroesEPainelAcompanhaInscricao() throws Exception {
    long first = track("Primeira");
    long second = track("Segunda");
    long third = track("Terceira");
    long id = playlist(userToken, List.of(first, second, third));
    json(send(PUT, "/api/assistant/mode", userToken, Map.of("mode", "SEQUENTIAL")), 200);
    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "STOPPED", null);
    send(GET, "/api/assistant/panel", userToken, null).andExpect(status().isNoContent());
    assertEquals(0, history(userToken).size());

    assertEquals(1, enqueue("PLAY", id).path("pendingCommands").asInt());
    assertEquals(2, enqueue("PAUSE", null).path("pendingCommands").asInt());
    assertEquals(3, enqueue("RESUME", null).path("pendingCommands").asInt());
    assertEquals(4, enqueue("NEXT", null).path("pendingCommands").asInt());
    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "STOPPED", null);

    JsonNode executed = json(send(POST, "/api/assistant/queue/execute", userToken, null), 200);
    assertEquals(4, executed.path("executedCommands").asInt());
    assertEquals(0, executed.path("pendingCommands").asInt());
    assertPlayback(executed.path("playback"), "PLAYING", second);
    assertPlayback(panel(userToken), "PLAYING", second);
    assertEquals(List.of("PLAY", "PAUSE", "RESUME", "NEXT"), actions(history(userToken)));

    send(DELETE, "/api/assistant/panel/subscription", userToken, null)
            .andExpect(status().isNoContent());
    assertPlayback(command("NEXT", null), "PLAYING", third);
    assertPlayback(panel(userToken), "PLAYING", second);
    assertEquals(5, history(userToken).size());

    for (int i = 0; i < 2; i++) {
      send(PUT, "/api/assistant/panel/subscription", userToken, null)
              .andExpect(status().isNoContent());
    }
    assertPlayback(panel(userToken), "PLAYING", second);
    assertPlayback(command("NEXT", null), "STOPPED", null);
    assertPlayback(panel(userToken), "STOPPED", null);
    assertEquals(
            List.of("PLAY", "PAUSE", "RESUME", "NEXT", "NEXT", "NEXT"),
            actions(history(userToken)));

    assertPlayback(command("STOP", null), "STOPPED", null);
    assertEquals(6, history(userToken).size());
  }

  @Test
  void embaralhamentoPreservaOcorrenciasEAtingeFimDaPlaylist() throws Exception {
    long first = track("Primeira");
    long second = track("Segunda");
    long third = track("Terceira");
    List<Long> expected = new ArrayList<>(List.of(first, second, first, third));
    long id = playlist(userToken, expected);
    json(send(PUT, "/api/assistant/mode", userToken, Map.of("mode", "SHUFFLED")), 200);
    List<Long> played = new ArrayList<>();

    JsonNode current = command("PLAY", id);
    played.add(current.path("currentTrack").path("id").asLong());
    for (int i = 1; i < expected.size(); i++) {
      current = command("NEXT", null);
      assertEquals("PLAYING", current.path("status").asText());
      played.add(current.path("currentTrack").path("id").asLong());
    }

    Collections.sort(expected);
    Collections.sort(played);
    assertEquals(expected, played);
    assertPlayback(command("NEXT", null), "STOPPED", null);
    assertEquals(5, history(userToken).size());
  }

  @Test
  void erroNaFilaMantemPendenciasERejeicaoNaoPublicaEvento() throws Exception {
    long first = track("Primeira");
    long second = track("Segunda");
    long id = playlist(userToken, List.of(first, second));
    enqueue("PLAY", id);
    enqueue("PAUSE", null);
    enqueue("PAUSE", null);
    enqueue("RESUME", null);
    enqueue("NEXT", null);

    send(POST, "/api/assistant/queue/execute", userToken, null)
            .andExpect(status().isConflict());
    assertEquals(2, queue(userToken).path("pendingCommands").asInt());
    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "PAUSED", first);
    assertEquals(List.of("PLAY", "PAUSE"), actions(history(userToken)));

    JsonNode executed = json(send(POST, "/api/assistant/queue/execute", userToken, null), 200);
    assertEquals(2, executed.path("executedCommands").asInt());
    assertEquals(0, executed.path("pendingCommands").asInt());
    assertPlayback(executed.path("playback"), "PLAYING", second);
    assertEquals(List.of("PLAY", "PAUSE", "RESUME", "NEXT"), actions(history(userToken)));

    long empty = playlist(userToken, List.of());
    send(POST, "/api/assistant/commands", userToken, commandBody("PLAY", empty))
            .andExpect(status().isBadRequest());
    send(POST, "/api/assistant/commands", userToken, Map.of("action", "PLAY"))
            .andExpect(status().isBadRequest());
    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "PLAYING", second);
    assertEquals(4, history(userToken).size());
  }

  @Test
  void contasNaoCompartilhamTocadorFilaPainelOuHistorico() throws Exception {
    long first = track("Primeira");
    long second = track("Segunda");
    long ownPlaylist = playlist(userToken, List.of(first, second));
    String otherName = createUser("outro");
    String otherToken = login(otherName);
    long otherPlaylist = playlist(otherToken, List.of(second, first));
    command("PLAY", ownPlaylist);
    command("PAUSE", null);
    enqueue("RESUME", null);
    enqueue("NEXT", null);

    assertPlayback(json(send(GET, "/api/assistant", otherToken, null), 200), "STOPPED", null);
    assertEquals(0, queue(otherToken).path("pendingCommands").asInt());
    send(GET, "/api/assistant/panel", otherToken, null).andExpect(status().isNoContent());
    assertEquals(0, history(otherToken).size());

    json(send(POST, "/api/assistant/queue", otherToken,
            commandBody("PLAY", otherPlaylist)), 201);
    JsonNode otherResult = json(
            send(POST, "/api/assistant/queue/execute-next", otherToken, null), 200);
    assertEquals(1, otherResult.path("executedCommands").asInt());
    assertPlayback(otherResult.path("playback"), "PLAYING", second);
    assertEquals(List.of("PLAY"), actions(history(otherToken)));

    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "PAUSED", first);
    assertPlayback(panel(userToken), "PAUSED", first);
    assertEquals(2, queue(userToken).path("pendingCommands").asInt());
    assertEquals(List.of("PLAY", "PAUSE"), actions(history(userToken)));

    send(DELETE, "/api/assistant/queue", userToken, null).andExpect(status().isNoContent());
    assertEquals(0, queue(userToken).path("pendingCommands").asInt());
    assertPlayback(json(send(GET, "/api/assistant", userToken, null), 200), "PAUSED", first);
    assertEquals(2, history(userToken).size());
    assertPlayback(json(send(GET, "/api/assistant", otherToken, null), 200), "PLAYING", second);
  }

  private JsonNode history(String token) throws Exception {
    return json(send(GET, "/api/assistant/history", token, null), 200);
  }

  private JsonNode panel(String token) throws Exception {
    return json(send(GET, "/api/assistant/panel", token, null), 200);
  }

  private JsonNode queue(String token) throws Exception {
    return json(send(GET, "/api/assistant/queue", token, null), 200);
  }

  private List<String> actions(JsonNode events) {
    List<String> result = new ArrayList<>();
    events.forEach(event -> result.add(event.path("action").asText()));
    return result;
  }
}