package br.upe.alexapatterns.advice;

import br.upe.alexapatterns.dto.ApiError;
import br.upe.alexapatterns.exception.BadRequestException;
import br.upe.alexapatterns.exception.ConflictException;
import br.upe.alexapatterns.exception.NotFoundException;
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

  /**
   * Informa uma regra de negócio que impede a operação.
   */
  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError badRequest(BadRequestException exception) {
    return new ApiError(exception.getMessage());
  }

  /**
   * Informa conflitos com os dados cadastrados.
   */
  @ExceptionHandler(ConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError conflict(ConflictException exception) {
    return new ApiError(exception.getMessage());
  }

  /**
   * Informa que o recurso solicitado não foi encontrado.
   */
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError notFound(NotFoundException exception) {
    return new ApiError(exception.getMessage());
  }
}
