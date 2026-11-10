package br.upe.alexapatterns.playback.observer;

import br.upe.alexapatterns.playback.event.PlaybackEvent;

/**
 * Contrato de inscrição e publicação de eventos.
 */
public interface PlaybackPublisher {

  /**
   * Inscreve um observador sem duplicar uma inscrição existente.
   */
  void subscribe(PlaybackObserver observer);

  /**
   * Remove a inscrição de um observador, se existir.
   */
  void unsubscribe(PlaybackObserver observer);

  /**
   * Notifica os observadores inscritos no início da publicação.
   */
  void publish(PlaybackEvent event);
}
