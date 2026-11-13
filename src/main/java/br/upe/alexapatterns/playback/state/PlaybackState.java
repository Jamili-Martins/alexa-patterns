package br.upe.alexapatterns.playback.state;

import br.upe.alexapatterns.exception.ConflictException;
import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.track.entity.Track;
import java.util.List;

/**
 * Define as ações disponíveis conforme o estado do tocador.
 */
public interface PlaybackState {

  /**
   * Identifica o estado atual.
   */
  PlaybackStatus getStatus();

  /**
   * Inicia uma nova reprodução.
   */
  void play(MusicPlayer player, List<Track> tracks);

  /**
   * Pausa a reprodução quando o estado permitir.
   */
  default void pause(MusicPlayer player) {
    throw invalid(PlaybackAction.PAUSE);
  }

  /**
   * Continua a reprodução quando o estado permitir.
   */
  default void resume(MusicPlayer player) {
    throw invalid(PlaybackAction.RESUME);
  }

  /**
   * Avança a reprodução quando o estado permitir.
   */
  default void next(MusicPlayer player) {
    throw invalid(PlaybackAction.NEXT);
  }

  /**
   * Encerra a reprodução ou mantém o tocador parado.
   */
  void stop(MusicPlayer player);

  private ConflictException invalid(PlaybackAction action) {
    return new ConflictException(
            "Ação " + action + " não permitida no estado " + getStatus());
  }
}