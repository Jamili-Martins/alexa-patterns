package br.upe.alexapatterns.assistant.controller;

import br.upe.alexapatterns.assistant.service.AssistantService;
import br.upe.alexapatterns.playback.event.PlaybackEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza painel e histórico da conta autenticada.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@Tag(name = "Painel e histórico")
public class PlaybackObservationController {

  private final AssistantService service;

  /**
   * Consulta o painel ou devolve 204 se ele ainda não recebeu eventos.
   */
  @GetMapping("/panel")
  @Operation(summary = "Consulta meu painel")
  public ResponseEntity<PlaybackEvent> panel(@AuthenticationPrincipal Jwt principal) {
    return service.panel(principal.getSubject())
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
  }

  /**
   * Consulta os eventos registrados no histórico da conta.
   */
  @GetMapping("/history")
  @Operation(summary = "Consulta meu histórico")
  public List<PlaybackEvent> history(@AuthenticationPrincipal Jwt principal) {
    return service.history(principal.getSubject());
  }

  /**
   * Inscreve o painel para receber os próximos eventos.
   */
  @PutMapping("/panel/subscription")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Inscreve meu painel")
  public void subscribe(@AuthenticationPrincipal Jwt principal) {
    service.subscribePanel(principal.getSubject());
  }

  /**
   * Cancela a inscrição do painel sem apagar sua última notificação.
   */
  @DeleteMapping("/panel/subscription")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Remove a inscrição do meu painel")
  public void unsubscribe(@AuthenticationPrincipal Jwt principal) {
    service.unsubscribePanel(principal.getSubject());
  }
}