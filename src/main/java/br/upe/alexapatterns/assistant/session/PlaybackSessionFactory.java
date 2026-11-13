package br.upe.alexapatterns.assistant.session;

import br.upe.alexapatterns.assistant.command.CommandQueue;
import br.upe.alexapatterns.playback.entity.PlaybackMode;
import br.upe.alexapatterns.playback.observer.PlaybackEventPublisher;
import br.upe.alexapatterns.playback.observer.PlaybackHistory;
import br.upe.alexapatterns.playback.observer.PlaybackPanel;
import br.upe.alexapatterns.playback.state.MusicPlayer;
import br.upe.alexapatterns.playback.strategy.PlaybackOrderStrategy;
import br.upe.alexapatterns.playlist.iterator.PlaylistSequence;
import java.time.Clock;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Monta os colaboradores de uma nova sessão de reprodução.
 */
@Component
@RequiredArgsConstructor
public class PlaybackSessionFactory {

  private final Map<PlaybackMode, PlaybackOrderStrategy> strategies;

  /**
   * Cria uma sessão com reprodução sequencial, painel e histórico inscritos.
   */
  public PlaybackSession create() {
    PlaybackEventPublisher publisher = new PlaybackEventPublisher();
    PlaybackPanel panel = new PlaybackPanel();
    PlaybackHistory history = new PlaybackHistory();
    publisher.subscribe(panel);
    publisher.subscribe(history);
    MusicPlayer player = new MusicPlayer(
            strategies.get(PlaybackMode.SEQUENTIAL),
            tracks -> new PlaylistSequence(tracks).iterator(),
            publisher, Clock.systemUTC());
    return new PlaybackSession(player, new CommandQueue(), publisher, panel, history);
  }
}
