package br.upe.alexapatterns.playlist.iterator;

import br.upe.alexapatterns.track.entity.Track;
import java.util.Iterator;
import java.util.List;

/**
 * Requisito #2 - Iterator: controla um percurso independente pelas faixas.
 */
public class PlaylistIterator implements Iterator<Track> {

  /**
   * Inicia o percurso sobre uma cópia da sequência recebida.
   */
  public PlaylistIterator(List<Track> tracks) {
    throw new UnsupportedOperationException(
            "Requisito #2 - Iterator: implemente o construtor de PlaylistIterator");
  }

  @Override
  public boolean hasNext() {
    throw new UnsupportedOperationException(
            "Requisito #2 - Iterator: implemente PlaylistIterator.hasNext");
  }

  @Override
  public Track next() {
    throw new UnsupportedOperationException(
            "Requisito #2 - Iterator: implemente PlaylistIterator.next");
  }
}
