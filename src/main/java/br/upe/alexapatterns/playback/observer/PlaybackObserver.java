package br.upe.alexapatterns.playback.observer;

import br.upe.alexapatterns.playback.event.PlaybackEvent;

/**
 * Contrato dos assinantes de eventos de reprodução.
 */
@FunctionalInterface
public interface PlaybackObserver {

  /**
   * Recebe um evento publicado pelo tocador.
   */
  void update(PlaybackEvent event);
}
