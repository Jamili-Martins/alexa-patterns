package br.upe.alexapatterns.playlist.dto;

import br.upe.alexapatterns.track.dto.TrackResponse;
import java.util.List;

/**
 * Dados da playlist e de suas músicas na ordem escolhida.
 */
public record PlaylistResponse(
        Long id, String name, String owner, List<TrackResponse> tracks) {
}
