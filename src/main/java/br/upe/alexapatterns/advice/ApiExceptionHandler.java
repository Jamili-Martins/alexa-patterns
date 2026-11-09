package br.upe.alexapatterns.advice;

import br.upe.alexapatterns.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte erros dos controllers em respostas HTTP.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  /**
   * Informa a falha de autenticação sem identificar qual credencial está incorreta.
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError invalidCredentials() {
    return new ApiError("Usuário ou senha inválidos");
  }

  /**
   * Rejeita JSON inválido e campos que não atendem às restrições.
   */
  @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError invalidRequest() {
    return new ApiError("Dados da requisição inválidos");
  }
}
