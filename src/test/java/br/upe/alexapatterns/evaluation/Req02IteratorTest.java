package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.upe.alexapatterns.playlist.iterator.PlaylistIterator;
import br.upe.alexapatterns.playlist.iterator.PlaylistSequence;
import br.upe.alexapatterns.track.entity.Track;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Avalia o percurso das faixas sem depender das estratégias.
 */
@DisplayName("Requisito - #02 - Iterator")
class Req02IteratorTest {

  private final Track first = new Track("Primeira", "Artista", 180);
  private final Track second = new Track("Segunda", "Artista", 200);

  @Test
  void percorreNaOrdemPreservandoRepeticoes() {
    Iterator<Track> iterator = new PlaylistIterator(List.of(first, second, first));

    assertSame(first, iterator.next());
    assertSame(second, iterator.next());
    assertSame(first, iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void hasNextNaoConsomeFaixas() {
    Iterator<Track> iterator = new PlaylistIterator(List.of(first, second));

    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext());
    assertSame(first, iterator.next());
    assertTrue(iterator.hasNext());
    assertTrue(iterator.hasNext());
    assertSame(second, iterator.next());
    assertFalse(iterator.hasNext());
    assertFalse(iterator.hasNext());
  }

  @Test
  void esgotamentoMantemFimELancaExcecao() {
    Iterator<Track> iterator = new PlaylistIterator(List.of(first));

    assertSame(first, iterator.next());
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertThrows(NoSuchElementException.class, iterator::next);
    assertFalse(iterator.hasNext());
  }

  @Test
  void listaVaziaJaComecaEsgotada() {
    Iterator<Track> iterator = new PlaylistIterator(List.of());

    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
    assertFalse(iterator.hasNext());
  }

  @Test
  void iteradoresDaMesmaSequenciaSaoIndependentes() {
    PlaylistSequence sequence = new PlaylistSequence(List.of(first, second));
    Iterator<Track> left = sequence.iterator();
    Iterator<Track> right = sequence.iterator();

    assertNotSame(left, right);
    assertSame(first, left.next());
    assertSame(second, left.next());
    assertFalse(left.hasNext());
    assertTrue(right.hasNext());
    assertSame(first, right.next());
    assertSame(second, right.next());
    assertFalse(right.hasNext());
  }

  @Test
  void sequenciaEIteradorCapturamUmaCopiaDaEntrada() {
    List<Track> source = new ArrayList<>(List.of(first, second));
    Iterator<Track> direct = new PlaylistIterator(source);
    PlaylistSequence sequence = new PlaylistSequence(source);

    source.clear();

    for (Iterator<Track> iterator : List.of(direct, sequence.iterator())) {
      assertSame(first, iterator.next());
      assertSame(second, iterator.next());
      assertFalse(iterator.hasNext());
    }
  }

  @Test
  void removeNaoEPermitidoENaoAlteraPercurso() {
    Iterator<Track> iterator = new PlaylistIterator(List.of(first, second));

    assertSame(first, iterator.next());
    assertThrows(UnsupportedOperationException.class, iterator::remove);
    assertSame(second, iterator.next());
    assertFalse(iterator.hasNext());
  }
}