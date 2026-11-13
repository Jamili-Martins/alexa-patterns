package br.upe.alexapatterns.assistant.dto;

/**
 * Quantidade de pedidos aguardando execução na fila da conta.
 */
public record CommandQueueResponse(int pendingCommands) {
}