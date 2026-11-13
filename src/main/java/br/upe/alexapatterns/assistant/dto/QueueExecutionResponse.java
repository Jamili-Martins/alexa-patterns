package br.upe.alexapatterns.assistant.dto;

import br.upe.alexapatterns.playback.dto.PlaybackSnapshot;

/**
 * Resultado de uma execução bem-sucedida da fila de comandos.
 */
public record QueueExecutionResponse(
        int executedCommands,
        int pendingCommands,
        PlaybackSnapshot playback
) {
}