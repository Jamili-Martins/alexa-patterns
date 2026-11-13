package br.upe.alexapatterns.playlist.controller;

import br.upe.alexapatterns.playlist.dto.CreatePlaylistRequest;
import br.upe.alexapatterns.playlist.dto.PlaylistResponse;
import br.upe.alexapatterns.playlist.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza as playlists da conta autenticada.
 */
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "Playlists")
public class PlaylistController {

  private final PlaylistService service;

  /**
   * Cadastra uma playlist para a conta autenticada.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Cria uma playlist")
  public PlaylistResponse create(
          @Valid @RequestBody CreatePlaylistRequest request,
          @AuthenticationPrincipal Jwt principal) {
    return service.create(request, principal.getSubject());
  }

  /**
   * Lista as playlists da conta autenticada.
   */
  @GetMapping
  @Operation(summary = "Lista minhas playlists")
  public List<PlaylistResponse> findAll(@AuthenticationPrincipal Jwt principal) {
    return service.findAll(principal.getSubject());
  }

  /**
   * Consulta uma playlist da conta autenticada.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Consulta uma das minhas playlists")
  public PlaylistResponse findById(
          @PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
    return service.findById(id, principal.getSubject());
  }
}
