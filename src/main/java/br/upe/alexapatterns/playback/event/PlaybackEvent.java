package br.upe.alexapatterns.playback.event;

import br.upe.alexapatterns.playback.entity.PlaybackAction;
import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.track.dto.TrackResponse;
import java.time.Instant;
import java.util.Objects;

/**
 * Registro imutável de uma alteração na reprodução.
 */
public record PlaybackEvent(
        PlaybackAction action, PlaybackStatus status,
        TrackResponse currentTrack, Instant occurredAt) {

  /**
   * Valida os campos obrigatórios e permite ausência da faixa atual.
   */
  public PlaybackEvent {
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(occurredAt, "occurredAt");
  }
}
