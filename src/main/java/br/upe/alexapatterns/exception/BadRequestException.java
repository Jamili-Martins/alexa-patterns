package br.upe.alexapatterns.exception;

/**
 * Indica dados que não atendem às regras da operação.
 */
public class BadRequestException extends RuntimeException {

  /**
   * Descreve a regra que não foi atendida.
   */
  public BadRequestException(String message) {
    super(message);
  }
}
