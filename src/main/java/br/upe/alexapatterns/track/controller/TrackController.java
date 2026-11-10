package br.upe.alexapatterns.track.controller;

import br.upe.alexapatterns.track.dto.CreateTrackRequest;
import br.upe.alexapatterns.track.dto.TrackResponse;
import br.upe.alexapatterns.track.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Disponibiliza o catálogo de músicas.
 */
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
@Tag(name = "Músicas")
public class TrackController {

  private final TrackService service;

  /**
   * Cadastra uma faixa musical.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Cadastra uma música")
  public TrackResponse create(@Valid @RequestBody CreateTrackRequest request) {
    return service.create(request);
  }

  /**
   * Lista as faixas disponíveis.
   */
  @GetMapping
  @Operation(summary = "Lista as músicas do catálogo")
  public List<TrackResponse> findAll() {
    return service.findAll();
  }

  /**
   * Consulta uma faixa musical pelo identificador.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Consulta uma música pelo identificador")
  public TrackResponse findById(@PathVariable Long id) {
    return service.findById(id);
  }
}
