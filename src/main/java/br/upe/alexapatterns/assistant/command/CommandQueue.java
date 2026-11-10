package br.upe.alexapatterns.assistant.command;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Requisito #5 - Command: armazena pedidos para execução posterior em ordem de inclusão.
 */
public class CommandQueue {

  private final Queue<PlaybackCommand> commands = new ArrayDeque<>();

  /**
   * Registra um comando não nulo, sem executá-lo.
   */
  public void enqueue(PlaybackCommand command) {
    throw new UnsupportedOperationException(
            "Implemente CommandQueue.enqueue");
  }

  /**
   * Remove e executa o próximo comando; devolve false se a fila estiver vazia.
   */
  public boolean executeNext() {
    throw new UnsupportedOperationException(
            "Implemente CommandQueue.executeNext");
  }

  /**
   * Executa os pedidos em ordem e devolve a quantidade concluída, ou propaga o erro.
   */
  public int executeAll() {
    throw new UnsupportedOperationException(
            "Implemente CommandQueue.executeAll");
  }

  /**
   * Informa quantos comandos ainda estão pendentes.
   */
  public int size() {
    throw new UnsupportedOperationException(
            "Implemente CommandQueue.size");
  }

  /**
   * Descarta os comandos pendentes, sem alterar o tocador.
   */
  public void clear() {
    throw new UnsupportedOperationException(
            "Implemente CommandQueue.clear");
  }
}
