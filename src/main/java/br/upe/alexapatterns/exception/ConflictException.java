package br.upe.alexapatterns.exception;

/**
 * Indica um conflito com os dados existentes.
 */
public class ConflictException extends RuntimeException {

  /**
   * Descreve o conflito encontrado.
   */
  public ConflictException(String message) {
    super(message);
  }
}
