package br.upe.alexapatterns.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Nome e sequência de músicas da nova playlist.
 */
public record CreatePlaylistRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Size(max = 100) List<@NotNull @Positive Long> trackIds) {
}
