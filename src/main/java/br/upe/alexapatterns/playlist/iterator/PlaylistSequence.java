package br.upe.alexapatterns.playlist.iterator;

import br.upe.alexapatterns.track.entity.Track;
import java.util.Iterator;
import java.util.List;

/**
 * Sequência de reprodução que oferece percursos independentes.
 */
public class PlaylistSequence implements Iterable<Track> {

  private final List<Track> tracks;

  /**
   * Guarda uma cópia não modificável da ordem escolhida para a reprodução.
   */
  public PlaylistSequence(List<Track> tracks) {
    this.tracks = List.copyOf(tracks);
  }

  @Override
  public Iterator<Track> iterator() {
    return new PlaylistIterator(tracks);
  }
}
