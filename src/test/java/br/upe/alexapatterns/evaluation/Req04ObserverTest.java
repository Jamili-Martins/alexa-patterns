package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.playback.event.PlaybackEvent;
import br.upe.alexapatterns.playback.observer.PlaybackEventPublisher;
import br.upe.alexapatterns.playback.observer.PlaybackHistory;
import br.upe.alexapatterns.playback.observer.PlaybackObserver;
import br.upe.alexapatterns.playback.observer.PlaybackPanel;
import br.upe.alexapatterns.playback.observer.PlaybackPublisher;
import br.upe.alexapatterns.track.dto.TrackResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Avalia publicadora, painel e histórico sem executar os demais padrões.
 */
@DisplayName("Requisito - #04 - Observer")
class Req04ObserverTest {

  private final PlaybackEvent first = new PlaybackEvent(
          PlaybackAction.PLAY, PlaybackStatus.PLAYING,
          new TrackResponse(1L, "Música", "Artista", 180),
          Instant.parse("2026-01-01T00:00:00Z"));
  private final PlaybackEvent second = new PlaybackEvent(
          PlaybackAction.STOP, PlaybackStatus.STOPPED, null,
          first.occurredAt().plusSeconds(1));

  @Test
  void publicaSemAssinantesSemLancarErro() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();

    assertDoesNotThrow(() -> publisher.publish(first));
  }

  @Test
  void notificaNaOrdemDeInscricaoPreservandoEvento() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<String> calls = new ArrayList<>();
    publisher.subscribe(event -> {
      assertEquals(first, event);
      calls.add("primeiro");
    });
    publisher.subscribe(event -> {
      assertEquals(first, event);
      calls.add("segundo");
    });

    assertTrue(calls.isEmpty());
    publisher.publish(first);

    assertEquals(List.of("primeiro", "segundo"), calls);
  }

  @Test
  void inscricaoDuplicadaNaoDuplicaNotificacoes() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<PlaybackEvent> received = new ArrayList<>();
    PlaybackObserver observer = received::add;

    publisher.subscribe(observer);
    publisher.subscribe(observer);
    publisher.publish(first);

    assertEquals(List.of(first), received);
  }

  @Test
  void remocaoImpedeProximasNotificacoesESuportaAusente() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<PlaybackEvent> received = new ArrayList<>();
    List<PlaybackEvent> retained = new ArrayList<>();
    PlaybackObserver observer = received::add;
    PlaybackObserver absent = event -> received.add(event);

    publisher.subscribe(observer);
    publisher.subscribe(retained::add);
    publisher.publish(first);
    publisher.unsubscribe(observer);
    publisher.unsubscribe(observer);
    publisher.unsubscribe(absent);
    publisher.publish(second);

    assertEquals(List.of(first), received);
    assertEquals(List.of(first, second), retained);
  }

  @Test
  void publicacoesRepetidasSaoEntreguesNovamente() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<PlaybackEvent> received = new ArrayList<>();
    publisher.subscribe(received::add);

    publisher.publish(first);
    publisher.publish(first);

    assertEquals(List.of(first, first), received);
  }

  @Test
  void inscricaoNaoReproduzEventosAnteriores() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<PlaybackEvent> received = new ArrayList<>();
    publisher.publish(first);

    publisher.subscribe(received::add);
    assertTrue(received.isEmpty());
    publisher.publish(second);

    assertEquals(List.of(second), received);
  }

  @Test
  void alteracoesDurantePublicacaoValemNoProximoEvento() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    List<String> calls = new ArrayList<>();
    PlaybackObserver late = event -> calls.add("novo");
    PlaybackObserver removed = event -> calls.add("segundo");
    publisher.subscribe(event -> {
      calls.add("primeiro");
      publisher.unsubscribe(removed);
      publisher.subscribe(late);
    });
    publisher.subscribe(removed);

    publisher.publish(first);
    assertEquals(List.of("primeiro", "segundo"), calls);

    calls.clear();
    publisher.publish(second);
    assertEquals(List.of("primeiro", "novo"), calls);
  }

  @Test
  void publicadorasNaoCompartilhamAssinantes() {
    PlaybackPublisher left = new PlaybackEventPublisher();
    PlaybackPublisher right = new PlaybackEventPublisher();
    List<PlaybackEvent> leftEvents = new ArrayList<>();
    List<PlaybackEvent> rightEvents = new ArrayList<>();
    left.subscribe(leftEvents::add);

    right.publish(first);
    assertTrue(leftEvents.isEmpty());

    right.subscribe(rightEvents::add);
    left.publish(first);
    right.publish(second);

    assertEquals(List.of(first), leftEvents);
    assertEquals(List.of(second), rightEvents);
  }

  @Test
  void painelComecaVazioEMantemUltimoEvento() {
    PlaybackPanel panel = new PlaybackPanel();
    assertEquals(Optional.empty(), panel.getLatest());

    panel.update(first);
    assertEquals(Optional.of(first), panel.getLatest());

    panel.update(second);
    assertEquals(Optional.of(second), panel.getLatest());
  }

  @Test
  void paineisNaoCompartilhamAtualizacoes() {
    PlaybackPanel left = new PlaybackPanel();
    PlaybackPanel right = new PlaybackPanel();

    left.update(first);
    assertEquals(Optional.of(first), left.getLatest());
    assertEquals(Optional.empty(), right.getLatest());

    right.update(second);
    assertEquals(Optional.of(first), left.getLatest());
    assertEquals(Optional.of(second), right.getLatest());
  }

  @Test
  void historicoComecaVazioEPreservaOrdemERepeticoes() {
    PlaybackHistory history = new PlaybackHistory();
    assertTrue(history.getEvents().isEmpty());

    history.update(first);
    history.update(first);
    history.update(second);

    assertEquals(List.of(first, first, second), history.getEvents());
  }

  @Test
  void historicoDevolveCopiasNaoModificaveisEIndependentes() {
    PlaybackHistory history = new PlaybackHistory();
    history.update(first);
    List<PlaybackEvent> snapshot = history.getEvents();

    assertThrows(UnsupportedOperationException.class, () -> snapshot.add(second));
    assertThrows(UnsupportedOperationException.class, snapshot::clear);

    history.update(second);

    assertEquals(List.of(first), snapshot);
    assertEquals(List.of(first, second), history.getEvents());
  }

  @Test
  void historicosNaoCompartilhamEventos() {
    PlaybackHistory left = new PlaybackHistory();
    PlaybackHistory right = new PlaybackHistory();

    left.update(first);
    assertEquals(List.of(first), left.getEvents());
    assertTrue(right.getEvents().isEmpty());

    right.update(second);
    assertEquals(List.of(first), left.getEvents());
    assertEquals(List.of(second), right.getEvents());
  }

  @Test
  void publicadoraIntegraPainelEHistorico() {
    PlaybackPublisher publisher = new PlaybackEventPublisher();
    PlaybackPanel panel = new PlaybackPanel();
    PlaybackHistory history = new PlaybackHistory();
    publisher.subscribe(panel);
    publisher.subscribe(history);
    publisher.publish(first);

    assertEquals(Optional.of(first), panel.getLatest());
    assertEquals(List.of(first), history.getEvents());

    publisher.unsubscribe(panel);
    publisher.publish(second);

    assertEquals(Optional.of(first), panel.getLatest());
    assertEquals(List.of(first, second), history.getEvents());

    publisher.subscribe(panel);
    assertEquals(Optional.of(first), panel.getLatest());
    publisher.publish(second);

    assertEquals(Optional.of(second), panel.getLatest());
    assertEquals(List.of(first, second, second), history.getEvents());
  }
}
