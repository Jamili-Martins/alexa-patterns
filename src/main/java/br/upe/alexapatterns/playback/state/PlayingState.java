package br.upe.alexapatterns.playback.state;

import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.track.entity.Track;
import java.util.List;

/**
 * Requisito #4 - State: comportamento durante a reprodução.
 */
public class PlayingState implements PlaybackState {

  @Override
  public PlaybackStatus getStatus() {
    return PlaybackStatus.PLAYING;
  }

  @Override
  public void play(MusicPlayer player, List<Track> tracks) {
    throw new UnsupportedOperationException(
            "Implemente PlayingState.play");
  }

  @Override
  public void pause(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PlayingState.pause");
  }

  @Override
  public void next(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PlayingState.next");
  }

  @Override
  public void stop(MusicPlayer player) {
    throw new UnsupportedOperationException(
            "Implemente PlayingState.stop");
  }
}
