package br.upe.alexapatterns.track.service;

import br.upe.alexapatterns.exception.NotFoundException;
import br.upe.alexapatterns.track.dto.CreateTrackRequest;
import br.upe.alexapatterns.track.dto.TrackResponse;
import br.upe.alexapatterns.track.entity.Track;
import br.upe.alexapatterns.track.repository.TrackRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra e consulta as faixas disponíveis.
 */
@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TrackService {

  private final TrackRepository repository;

  /**
   * Cadastra uma faixa mediante autorização de um administrador.
   */
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public TrackResponse create(CreateTrackRequest request) {
    Track track = new Track(
            request.title().strip(), request.artist().strip(), request.durationSeconds());
    return toResponse(repository.save(track));
  }

  /**
   * Lista as faixas por identificador em ordem crescente.
   */
  @Transactional(readOnly = true)
  public List<TrackResponse> findAll() {
    return repository.findAll(Sort.by("id")).stream()
            .map(this::toResponse)
            .toList();
  }

  /**
   * Consulta uma faixa pelo identificador.
   */
  @Transactional(readOnly = true)
  public TrackResponse findById(Long id) {
    Track track = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Música não encontrada"));
    return toResponse(track);
  }

  private TrackResponse toResponse(Track track) {
    return new TrackResponse(
            track.getId(), track.getTitle(), track.getArtist(), track.getDurationSeconds());
  }
}
