package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.upe.alexapatterns.playback.strategy.PlaybackOrderStrategy;
import br.upe.alexapatterns.playback.strategy.SequentialPlaybackStrategy;
import br.upe.alexapatterns.playback.strategy.ShuffledPlaybackStrategy;
import br.upe.alexapatterns.track.entity.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Avalia a ordenação das faixas sem depender dos demais padrões.
 */
@DisplayName("Requisito - #01 - Strategy")
class Req01StrategyTest {

  private final Track first = new Track("Primeira", "Artista", 180);
  private final Track second = new Track("Segunda", "Artista", 200);
  private final Track third = new Track("Terceira", "Artista", 220);

  @Test
  void sequencialPreservaOrdemERepeticoes() {
    List<Track> source = List.of(first, second, first, third);
    PlaybackOrderStrategy strategy = new SequentialPlaybackStrategy();

    assertIterableEquals(source, strategy.order(source));
  }

  @Test
  void sequencialProduzSequenciaIndependente() {
    List<Track> source = new ArrayList<>(List.of(first, second, first));
    List<Track> expected = List.copyOf(source);
    PlaybackOrderStrategy strategy = new SequentialPlaybackStrategy();

    List<Track> result = strategy.order(source);

    assertIterableEquals(expected, source);
    source.clear();
    assertIterableEquals(expected, result);
  }

  @Test
  void embaralhadaPreservaOcorrenciasSemModificarEntrada() {
    List<Track> source = new ArrayList<>(List.of(first, second, first, third));
    List<Track> original = List.copyOf(source);
    PlaybackOrderStrategy strategy = new ShuffledPlaybackStrategy(new Random(42));

    List<Track> result = strategy.order(source);

    assertIterableEquals(original, source);
    assertEquals(original.size(), result.size());
    for (Track track : List.of(first, second, third)) {
      assertEquals(
              Collections.frequency(original, track),
              Collections.frequency(result, track));
    }

    List<Track> snapshot = List.copyOf(result);
    source.clear();
    assertIterableEquals(snapshot, result);
  }

  @Test
  void embaralhadaUsaOGeradorFornecido() {
    ObservedRandom random = new ObservedRandom(42);
    PlaybackOrderStrategy strategy = new ShuffledPlaybackStrategy(random);

    strategy.order(List.of(first, second, third, first));

    assertTrue(random.used, "O embaralhamento deve usar o Random recebido no construtor");
  }

  @Test
  void mesmaSementeReproduzPrimeiraOrdenacao() {
    List<Track> source = List.of(first, second, third, first);
    PlaybackOrderStrategy left = new ShuffledPlaybackStrategy(new Random(123));
    PlaybackOrderStrategy right = new ShuffledPlaybackStrategy(new Random(123));

    assertIterableEquals(left.order(source), right.order(source));
  }

  @Test
  void estrategiasAceitamListasVaziasEUnitarias() {
    List<PlaybackOrderStrategy> strategies = List.of(
            new SequentialPlaybackStrategy(),
            new ShuffledPlaybackStrategy(new Random(42)));

    for (PlaybackOrderStrategy strategy : strategies) {
      assertTrue(strategy.order(List.of()).isEmpty());
      assertIterableEquals(List.of(first), strategy.order(List.of(first)));
    }
  }

  /**
   * Registra o consumo do gerador sem impor um algoritmo de embaralhamento.
   */
  private static class ObservedRandom extends Random {

    private boolean used;

    private ObservedRandom(long seed) {
      super(seed);
    }

    @Override
    protected int next(int bits) {
      used = true;
      return super.next(bits);
    }
  }
}
