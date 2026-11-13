package br.upe.alexapatterns.assistant.controller;

import br.upe.alexapatterns.assistant.dto.CommandQueueResponse;
import br.upe.alexapatterns.assistant.dto.CommandRequest;
import br.upe.alexapatterns.assistant.dto.QueueExecutionResponse;
import br.upe.alexapatterns.assistant.service.AssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza a fila de comandos da conta autenticada.
 */
@RestController
@RequestMapping("/api/assistant/queue")
@RequiredArgsConstructor
@Tag(name = "Fila de comandos")
public class CommandQueueController {

  private final AssistantService service;

  /**
   * Consulta a quantidade de comandos pendentes.
   */
  @GetMapping
  @Operation(summary = "Consulta minha fila")
  public CommandQueueResponse status(@AuthenticationPrincipal Jwt principal) {
    return service.queueStatus(principal.getSubject());
  }

  /**
   * Registra um comando para execução posterior.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Adiciona um comando à minha fila")
  public CommandQueueResponse enqueue(
          @Valid @RequestBody CommandRequest request, @AuthenticationPrincipal Jwt principal) {
    return service.enqueue(request, principal.getSubject());
  }

  /**
   * Executa somente o próximo comando pendente.
   */
  @PostMapping("/execute-next")
  @Operation(summary = "Executa o próximo comando da minha fila")
  public QueueExecutionResponse executeNext(@AuthenticationPrincipal Jwt principal) {
    return service.executeNext(principal.getSubject());
  }

  /**
   * Executa os comandos pendentes até concluir ou ocorrer um erro.
   */
  @PostMapping("/execute")
  @Operation(summary = "Executa minha fila de comandos")
  public QueueExecutionResponse execute(@AuthenticationPrincipal Jwt principal) {
    return service.executeQueue(principal.getSubject());
  }

  /**
   * Descarta todos os comandos pendentes.
   */
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Limpa minha fila")
  public void clear(@AuthenticationPrincipal Jwt principal) {
    service.clearQueue(principal.getSubject());
  }
}