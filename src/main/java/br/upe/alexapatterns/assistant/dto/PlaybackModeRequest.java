package br.upe.alexapatterns.assistant.dto;

import br.upe.alexapatterns.playback.entity.PlaybackMode;
import jakarta.validation.constraints.NotNull;

/**
 * Define a ordem que será aplicada na próxima reprodução.
 */
public record PlaybackModeRequest(@NotNull PlaybackMode mode) {
}
