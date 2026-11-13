package br.upe.alexapatterns.playback.state;

import br.upe.alexapatterns.exception.BadRequestException;
import br.upe.alexapatterns.playback.dto.PlaybackSnapshot;
import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.event.PlaybackEvent;
import br.upe.alexapatterns.playback.observer.PlaybackPublisher;
import br.upe.alexapatterns.playback.strategy.PlaybackOrderStrategy;
import br.upe.alexapatterns.track.dto.TrackResponse;
import br.upe.alexapatterns.track.entity.Track;
import java.time.Clock;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Contexto que delega as decisões aos estados e publica alterações.
 */
public class MusicPlayer {

  private final Function<List<Track>, Iterator<Track>> iteratorFactory;
  private final PlaybackPublisher publisher;
  private final Clock clock;
  private PlaybackOrderStrategy strategy;
  private PlaybackState state = new StoppedState();
  private Iterator<Track> cursor = Collections.emptyIterator();
  private Track currentTrack;
  private long revision;

  /**
   * Recebe os colaboradores usados na reprodução e na publicação de eventos.
   */
  public MusicPlayer(
          PlaybackOrderStrategy strategy,
          Function<List<Track>, Iterator<Track>> iteratorFactory,
          PlaybackPublisher publisher, Clock clock) {
    this.strategy = Objects.requireNonNull(strategy, "strategy");
    this.iteratorFactory = Objects.requireNonNull(iteratorFactory, "iteratorFactory");
    this.publisher = Objects.requireNonNull(publisher, "publisher");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Define a estratégia que será aplicada na próxima reprodução.
   */
  public void setStrategy(PlaybackOrderStrategy strategy) {
    this.strategy = Objects.requireNonNull(strategy, "strategy");
  }

  /**
   * Delega ao estado atual o início de uma reprodução.
   */
  public void play(List<Track> tracks) {
    apply(PlaybackAction.PLAY, () -> state.play(this, tracks));
  }

  /**
   * Delega ao estado atual a pausa da reprodução.
   */
  public void pause() {
    apply(PlaybackAction.PAUSE, () -> state.pause(this));
  }

  /**
   * Delega ao estado atual a continuação da reprodução.
   */
  public void resume() {
    apply(PlaybackAction.RESUME, () -> state.resume(this));
  }

  /**
   * Delega ao estado atual o avanço para a próxima faixa.
   */
  public void next() {
    apply(PlaybackAction.NEXT, () -> state.next(this));
  }

  /**
   * Delega ao estado atual o encerramento da reprodução.
   */
  public void stop() {
    apply(PlaybackAction.STOP, () -> state.stop(this));
  }

  /**
   * Devolve uma representação imutável da situação atual.
   */
  public PlaybackSnapshot snapshot() {
    return new PlaybackSnapshot(state.getStatus(), currentTrackResponse());
  }

  void beginPlaylist(List<Track> tracks) {
    List<Track> source = List.copyOf(tracks);
    if (source.isEmpty()) {
      throw new BadRequestException("A playlist precisa ter ao menos uma música");
    }
    List<Track> ordered = List.copyOf(strategy.order(source));
    Iterator<Track> nextCursor = Objects.requireNonNull(iteratorFactory.apply(ordered));
    if (!nextCursor.hasNext()) {
      throw new BadRequestException("Não há músicas para reproduzir");
    }
    Track first = Objects.requireNonNull(nextCursor.next());
    cursor = nextCursor;
    currentTrack = first;
    revision++;
  }

  boolean advance() {
    if (!cursor.hasNext()) {
      return false;
    }
    currentTrack = Objects.requireNonNull(cursor.next());
    revision++;
    return true;
  }

  void clearPlaylist() {
    cursor = Collections.emptyIterator();
    currentTrack = null;
    revision++;
  }

  void changeState(PlaybackState nextState) {
    state = Objects.requireNonNull(nextState, "nextState");
    revision++;
  }

  private void apply(PlaybackAction action, Runnable operation) {
    long before = revision;
    operation.run();
    if (revision != before) {
      PlaybackSnapshot current = snapshot();
      publisher.publish(new PlaybackEvent(
              action, current.status(), current.currentTrack(), clock.instant()));
    }
  }

  private TrackResponse currentTrackResponse() {
    if (currentTrack == null) {
      return null;
    }
    return new TrackResponse(
            currentTrack.getId(), currentTrack.getTitle(),
            currentTrack.getArtist(), currentTrack.getDurationSeconds());
  }
}
