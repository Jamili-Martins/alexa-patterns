package br.upe.alexapatterns.playback.strategy;

import br.upe.alexapatterns.track.entity.Track;
import java.util.List;

/**
 * Define a ordem das faixas de uma reprodução.
 */
@FunctionalInterface
public interface PlaybackOrderStrategy {

  /**
   * Devolve uma sequência independente, preservando todas as ocorrências das faixas.
   */
  List<Track> order(List<Track> tracks);
}
