package br.upe.alexapatterns.track.dto;

/**
 * Dados de uma faixa musical cadastrada.
 */
public record TrackResponse(
        Long id, String title, String artist, int durationSeconds) {
}
