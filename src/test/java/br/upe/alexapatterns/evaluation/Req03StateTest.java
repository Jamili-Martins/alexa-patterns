package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.upe.alexapatterns.exception.BadRequestException;
import br.upe.alexapatterns.exception.ConflictException;
import br.upe.alexapatterns.playback.dto.PlaybackSnapshot;
import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.playback.event.PlaybackEvent;
import br.upe.alexapatterns.playback.observer.PlaybackObserver;
import br.upe.alexapatterns.playback.observer.PlaybackPublisher;
import br.upe.alexapatterns.playback.state.MusicPlayer;
import br.upe.alexapatterns.track.dto.TrackResponse;
import br.upe.alexapatterns.track.entity.Track;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Avalia os estados usando colaboradores independentes dos demais exercícios.
 */
@DisplayName("Requisito - #03 - State")
class Req03StateTest {

  private final Instant instant = Instant.parse("2026-01-01T00:00:00Z");
  private final RecordingPublisher publisher = new RecordingPublisher();
  private final MusicPlayer player = new MusicPlayer(
          List::copyOf, List::iterator, publisher, Clock.fixed(instant, ZoneOffset.UTC));
  private final Track first = new Track("Primeira", "Artista", 180);
  private final Track second = new Track("Segunda", "Artista", 200);
  private final Track third = new Track("Terceira", "Artista", 220);

  @Test
  void paradoRejeitaAcoesInvalidasEStopNaoPublica() {
    assertPlayback(PlaybackStatus.STOPPED, null);
    assertRejected(player::pause);
    assertRejected(player::resume);
    assertRejected(player::next);

    player.stop();
    player.stop();

    assertPlayback(PlaybackStatus.STOPPED, null);
    assertActions();
  }

  @Test
  void playSelecionaPrimeiraFaixaEPublicaEventoCompleto() {
    player.play(List.of(first, second));

    assertPlayback(PlaybackStatus.PLAYING, first);
    assertEquals(List.of(new PlaybackEvent(
                    PlaybackAction.PLAY, PlaybackStatus.PLAYING, response(first), instant)),
            publisher.events);
  }

  @Test
  void pauseEResumePreservamFaixaEPosicao() {
    player.play(List.of(first, second));
    assertRejected(player::resume);

    player.pause();
    assertPlayback(PlaybackStatus.PAUSED, first);
    assertRejected(player::pause);

    player.resume();
    assertPlayback(PlaybackStatus.PLAYING, first);

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, second);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.PAUSE,
            PlaybackAction.RESUME, PlaybackAction.NEXT);
  }

  @Test
  void nextTocandoPreservaRepeticoesEEncerraNoFim() {
    player.play(List.of(first, first, second));

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, first);
    assertEquals(2, publisher.events.size());

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, second);

    player.next();
    assertPlayback(PlaybackStatus.STOPPED, null);
    assertRejected(player::next);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.NEXT,
            PlaybackAction.NEXT, PlaybackAction.NEXT);
  }

  @Test
  void nextPausadoMantemEstadoELimpaFaixaAoTerminar() {
    player.play(List.of(first, second));
    player.pause();

    player.next();
    assertPlayback(PlaybackStatus.PAUSED, second);

    player.next();
    assertPlayback(PlaybackStatus.STOPPED, null);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.PAUSE,
            PlaybackAction.NEXT, PlaybackAction.NEXT);
  }

  @Test
  void stopTocandoLimpaPercursoEPermiteReiniciar() {
    List<Track> tracks = List.of(first, second, third);
    player.play(tracks);
    player.next();

    player.stop();
    assertPlayback(PlaybackStatus.STOPPED, null);
    assertRejected(player::resume);
    assertRejected(player::next);
    player.stop();

    player.play(tracks);
    assertPlayback(PlaybackStatus.PLAYING, first);
    player.next();
    assertPlayback(PlaybackStatus.PLAYING, second);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.NEXT, PlaybackAction.STOP,
            PlaybackAction.PLAY, PlaybackAction.NEXT);
  }

  @Test
  void stopPausadoEncerraSessaoSemPermitirResume() {
    player.play(List.of(first, second));
    player.pause();

    player.stop();
    assertPlayback(PlaybackStatus.STOPPED, null);
    assertRejected(player::resume);
    assertRejected(player::next);
    player.stop();
    assertActions(PlaybackAction.PLAY, PlaybackAction.PAUSE, PlaybackAction.STOP);

    player.play(List.of(third));
    assertPlayback(PlaybackStatus.PLAYING, third);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.PAUSE,
            PlaybackAction.STOP, PlaybackAction.PLAY);
  }

  @Test
  void playTocandoReiniciaSequenciaMesmoComMesmaFaixaAtual() {
    List<Track> tracks = List.of(first, second);
    player.play(tracks);
    player.next();

    player.play(tracks);
    assertPlayback(PlaybackStatus.PLAYING, first);
    player.play(tracks);
    assertPlayback(PlaybackStatus.PLAYING, first);

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, second);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.NEXT, PlaybackAction.PLAY,
            PlaybackAction.PLAY, PlaybackAction.NEXT);
  }

  @Test
  void playPausadoSubstituiPlaylistEVoltaATocar() {
    player.play(List.of(first, second));
    player.pause();

    player.play(List.of(third, first));
    assertPlayback(PlaybackStatus.PLAYING, third);

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, first);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.PAUSE,
            PlaybackAction.PLAY, PlaybackAction.NEXT);
  }

  @Test
  void playVazioParadoPreservaEstadoESemEventos() {
    assertThrows(BadRequestException.class, () -> player.play(List.of()));

    assertPlayback(PlaybackStatus.STOPPED, null);
    assertActions();

    player.play(List.of(first));
    assertPlayback(PlaybackStatus.PLAYING, first);
    assertActions(PlaybackAction.PLAY);
  }

  @Test
  void playVazioTocandoPreservaFaixaEPosicao() {
    player.play(List.of(first, second, third));
    player.next();
    PlaybackSnapshot before = player.snapshot();

    assertThrows(BadRequestException.class, () -> player.play(List.of()));

    assertEquals(before, player.snapshot());
    assertActions(PlaybackAction.PLAY, PlaybackAction.NEXT);

    player.next();
    assertPlayback(PlaybackStatus.PLAYING, third);
    assertActions(PlaybackAction.PLAY, PlaybackAction.NEXT, PlaybackAction.NEXT);
  }

  @Test
  void playVazioPausadoPreservaEstadoEPosicao() {
    player.play(List.of(first, second, third));
    player.next();
    player.pause();
    PlaybackSnapshot before = player.snapshot();

    assertThrows(BadRequestException.class, () -> player.play(List.of()));

    assertEquals(before, player.snapshot());
    assertActions(PlaybackAction.PLAY, PlaybackAction.NEXT, PlaybackAction.PAUSE);

    player.next();
    assertPlayback(PlaybackStatus.PAUSED, third);
    assertActions(
            PlaybackAction.PLAY, PlaybackAction.NEXT,
            PlaybackAction.PAUSE, PlaybackAction.NEXT);
  }

  private void assertPlayback(PlaybackStatus status, Track track) {
    TrackResponse current = track == null ? null : response(track);
    assertEquals(new PlaybackSnapshot(status, current), player.snapshot());
  }

  private void assertRejected(Runnable operation) {
    PlaybackSnapshot before = player.snapshot();
    int count = publisher.events.size();

    assertThrows(ConflictException.class, operation::run);

    assertEquals(before, player.snapshot());
    assertEquals(count, publisher.events.size());
  }

  private void assertActions(PlaybackAction... actions) {
    assertEquals(
            List.of(actions),
            publisher.events.stream().map(PlaybackEvent::action).toList());
  }

  private TrackResponse response(Track track) {
    return new TrackResponse(
            track.getId(), track.getTitle(), track.getArtist(), track.getDurationSeconds());
  }

  /**
   * Captura publicações sem executar a implementação do exercício Observer.
   */
  private static class RecordingPublisher implements PlaybackPublisher {

    private final List<PlaybackEvent> events = new ArrayList<>();

    @Override
    public void subscribe(PlaybackObserver observer) {
    }

    @Override
    public void unsubscribe(PlaybackObserver observer) {
    }

    @Override
    public void publish(PlaybackEvent event) {
      events.add(event);
    }
  }
}