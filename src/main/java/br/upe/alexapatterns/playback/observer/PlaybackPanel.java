package br.upe.alexapatterns.playback.observer;

import br.upe.alexapatterns.playback.event.PlaybackEvent;
import java.util.Optional;

/**
 * Requisito #3 - Observer: mantém a última atualização do painel.
 */
public class PlaybackPanel implements PlaybackObserver {

  @Override
  public void update(PlaybackEvent event) {
    throw new UnsupportedOperationException(
            "Implemente PlaybackPanel.update");
  }

  /**
   * Consulta a última atualização ou devolve Optional.empty antes do primeiro evento.
   */
  public Optional<PlaybackEvent> getLatest() {
    throw new UnsupportedOperationException(
            "Implemente PlaybackPanel.getLatest");
  }
}
