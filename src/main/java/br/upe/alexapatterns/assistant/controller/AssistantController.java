package br.upe.alexapatterns.assistant.controller;

import br.upe.alexapatterns.assistant.dto.CommandRequest;
import br.upe.alexapatterns.assistant.dto.PlaybackModeRequest;
import br.upe.alexapatterns.assistant.service.AssistantService;
import br.upe.alexapatterns.playback.dto.PlaybackSnapshot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza o controle do tocador da conta autenticada.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@Tag(name = "Assistente")
public class AssistantController {

  private final AssistantService service;

  /**
   * Consulta o estado e a faixa atuais.
   */
  @GetMapping
  @Operation(summary = "Consulta meu tocador")
  public PlaybackSnapshot snapshot(@AuthenticationPrincipal Jwt principal) {
    return service.snapshot(principal.getSubject());
  }

  /**
   * Define o modo de ordenação das próximas reproduções.
   */
  @PutMapping("/mode")
  @Operation(summary = "Configura o modo de reprodução")
  public PlaybackSnapshot setMode(
          @Valid @RequestBody PlaybackModeRequest request, @AuthenticationPrincipal Jwt principal) {
    return service.setMode(request, principal.getSubject());
  }

  /**
   * Executa um comando imediatamente.
   */
  @PostMapping("/commands")
  @Operation(summary = "Executa um comando")
  public PlaybackSnapshot execute(
          @Valid @RequestBody CommandRequest request, @AuthenticationPrincipal Jwt principal) {
    return service.execute(request, principal.getSubject());
  }
}
