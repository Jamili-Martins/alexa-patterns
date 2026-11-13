package br.upe.alexapatterns.assistant.dto;

import br.upe.alexapatterns.playback.entity.PlaybackAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Pedido de reprodução, com playlist obrigatória somente para PLAY.
 */
public record CommandRequest(
        @NotNull PlaybackAction action,
        @Positive Long playlistId
) {

  /**
   * Confere se a presença da playlist corresponde à ação solicitada.
   */
  @JsonIgnore
  @AssertTrue(message = "Informe playlistId somente para a ação PLAY")
  public boolean isPlaylistValid() {
    if (action == PlaybackAction.PLAY) {
      return playlistId != null;
    }
    return playlistId == null;
  }
}
