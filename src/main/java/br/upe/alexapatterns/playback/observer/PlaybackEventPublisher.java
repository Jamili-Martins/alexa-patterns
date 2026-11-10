package br.upe.alexapatterns.playback.observer;

import br.upe.alexapatterns.playback.event.PlaybackEvent;

/**
 * Requisito #3 - Observer: gerencia inscrições e publica eventos.
 */
public class PlaybackEventPublisher implements PlaybackPublisher {

  @Override
  public void subscribe(PlaybackObserver observer) {
    throw new UnsupportedOperationException(
            "Implemente PlaybackEventPublisher.subscribe");
  }

  @Override
  public void unsubscribe(PlaybackObserver observer) {
    throw new UnsupportedOperationException(
            "Implemente PlaybackEventPublisher.unsubscribe");
  }

  @Override
  public void publish(PlaybackEvent event) {
    throw new UnsupportedOperationException(
            "Implemente PlaybackEventPublisher.publish");
  }
}
