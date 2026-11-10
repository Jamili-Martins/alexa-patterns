package br.upe.alexapatterns.playback.observer;

import br.upe.alexapatterns.playback.event.PlaybackEvent;
import java.util.List;

/**
 * Requisito #3 Observer: registra os eventos recebidos em ordem.
 */
public class PlaybackHistory implements PlaybackObserver {

  @Override
  public void update(PlaybackEvent event) {
    throw new UnsupportedOperationException(
            "Implemente PlaybackHistory.update");
  }

  /**
   * Devolve uma cópia não modificável dos eventos recebidos.
   */
  public List<PlaybackEvent> getEvents() {
    throw new UnsupportedOperationException(
            "Implemente PlaybackHistory.getEvents");
  }
}
