package br.upe.alexapatterns.playback.dto;

import br.upe.alexapatterns.playback.entity.PlaybackStatus;
import br.upe.alexapatterns.track.dto.TrackResponse;

/**
 * Estado do tocador e dados da faixa atual.
 */
public record PlaybackSnapshot(
        PlaybackStatus status,
        TrackResponse currentTrack
) {
}
