package br.upe.alexapatterns.assistant.service;

import br.upe.alexapatterns.assistant.command.NextCommand;
import br.upe.alexapatterns.assistant.command.PauseCommand;
import br.upe.alexapatterns.assistant.command.PlayCommand;
import br.upe.alexapatterns.assistant.command.PlaybackCommand;
import br.upe.alexapatterns.assistant.command.ResumeCommand;
import br.upe.alexapatterns.assistant.command.StopCommand;
import br.upe.alexapatterns.assistant.dto.CommandRequest;
import br.upe.alexapatterns.assistant.dto.PlaybackModeRequest;
import br.upe.alexapatterns.assistant.session.PlaybackSessions;
import br.upe.alexapatterns.exception.NotFoundException;
import br.upe.alexapatterns.playback.dto.PlaybackSnapshot;
import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.entity.PlaybackMode;
import br.upe.alexapatterns.playback.state.MusicPlayer;
import br.upe.alexapatterns.playback.strategy.PlaybackOrderStrategy;
import br.upe.alexapatterns.playlist.repository.PlaylistRepository;
import br.upe.alexapatterns.track.entity.Track;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Conecta os pedidos autenticados à sessão de reprodução da conta.
 */
@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() && #username == authentication.name")
public class AssistantService {

  private final PlaybackSessions sessions;
  private final PlaylistRepository playlists;
  private final Map<PlaybackMode, PlaybackOrderStrategy> strategies;

  /**
   * Consulta a situação atual do tocador da conta autenticada.
   */
  public PlaybackSnapshot snapshot(String username) {
    return sessions.withSession(username, session -> session.player().snapshot());
  }

  /**
   * Configura a estratégia usada na próxima reprodução da conta.
   */
  public PlaybackSnapshot setMode(PlaybackModeRequest request, String username) {
    return sessions.withSession(username, session -> {
      session.player().setStrategy(strategies.get(request.mode()));
      return session.player().snapshot();
    });
  }

  /**
   * Executa um pedido após carregar a playlist da própria conta, quando necessária.
   */
  @Transactional(readOnly = true)
  public PlaybackSnapshot execute(CommandRequest request, String username) {
    List<Track> tracks = resolveTracks(request, username);
    return sessions.withSession(username, session -> {
      PlaybackCommand command = createCommand(request.action(), session.player(), tracks);
      command.execute();
      return session.player().snapshot();
    });
  }

  private List<Track> resolveTracks(CommandRequest request, String username) {
    if (request.action() != PlaybackAction.PLAY) {
      return List.of();
    }
    return playlists.findByIdAndOwnerUsername(request.playlistId(), username)
            .orElseThrow(() -> new NotFoundException("Playlist não encontrada"))
            .getTracks();
  }

  private PlaybackCommand createCommand(
          PlaybackAction action, MusicPlayer player, List<Track> tracks) {
    return switch (action) {
      case PLAY -> new PlayCommand(player, tracks);
      case PAUSE -> new PauseCommand(player);
      case RESUME -> new ResumeCommand(player);
      case NEXT -> new NextCommand(player);
      case STOP -> new StopCommand(player);
    };
  }
}
