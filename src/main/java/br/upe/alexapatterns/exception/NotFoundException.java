package br.upe.alexapatterns.exception;

/**
 * Indica que o recurso solicitado não foi encontrado.
 */
public class NotFoundException extends RuntimeException {

  /**
   * Descreve o recurso que não foi encontrado.
   */
  public NotFoundException(String message) {
    super(message);
  }
}
