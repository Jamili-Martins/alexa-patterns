package br.upe.alexapatterns.playback.state;

import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.track.entity.Track;
import java.util.List;

/**
 * Requisito #4 - State: comportamento do tocador pausado.
 */
public class PausedState implements PlaybackState {

  @Override
  public PlaybackStatus getStatus() {
    return PlaybackStatus.PAUSED;
  }

  @Override
  public void play(MusicPlayer player, List<Track> tracks) {
    throw new UnsupportedOperationException(
            "Implemente PausedState.play");
  }

  @Override
  public void resume(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PausedState.resume");
  }

  @Override
  public void next(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PausedState.next");
  }

  @Override
  public void stop(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PausedState.stop");
  }
}
