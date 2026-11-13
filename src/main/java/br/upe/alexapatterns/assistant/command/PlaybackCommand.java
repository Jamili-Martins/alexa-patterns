package br.upe.alexapatterns.assistant.command;

/**
 * Representa um pedido de controle da reprodução.
 */
@FunctionalInterface
public interface PlaybackCommand {

  /**
   * Executa o pedido no tocador associado.
   */
  void execute();
}
