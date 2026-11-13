package br.upe.alexapatterns.track.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para cadastrar uma faixa musical.
 */
public record CreateTrackRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 100) String artist,
        @NotNull @Positive Integer durationSeconds) {
}
