package br.upe.alexapatterns.config;

import br.upe.alexapatterns.playback.entity.PlaybackMode;
import br.upe.alexapatterns.playback.strategy.PlaybackOrderStrategy;
import br.upe.alexapatterns.playback.strategy.SequentialPlaybackStrategy;
import br.upe.alexapatterns.playback.strategy.ShuffledPlaybackStrategy;
import java.util.Map;
import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Disponibiliza as estratégias de ordenação ao tocador.
 */
@Configuration(proxyBeanMethods = false)
public class PlaybackConfig {

  /**
   * Associa cada modo de reprodução à sua estratégia.
   */
  @Bean
  public Map<PlaybackMode, PlaybackOrderStrategy> playbackStrategies() {
    return Map.of(
            PlaybackMode.SEQUENTIAL, new SequentialPlaybackStrategy(),
            PlaybackMode.SHUFFLED, new ShuffledPlaybackStrategy(new Random()));
  }
}
