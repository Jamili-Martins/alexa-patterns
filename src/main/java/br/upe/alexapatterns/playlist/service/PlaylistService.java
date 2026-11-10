package br.upe.alexapatterns.playlist.service;

import br.upe.alexapatterns.exception.NotFoundException;
import br.upe.alexapatterns.playlist.dto.CreatePlaylistRequest;
import br.upe.alexapatterns.playlist.dto.PlaylistResponse;
import br.upe.alexapatterns.playlist.entity.Playlist;
import br.upe.alexapatterns.playlist.repository.PlaylistRepository;
import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.staff.repository.StaffUserRepository;
import br.upe.alexapatterns.track.dto.TrackResponse;
import br.upe.alexapatterns.track.entity.Track;
import br.upe.alexapatterns.track.repository.TrackRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra e consulta as playlists da conta autenticada.
 */
@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated() && #username == authentication.name")
public class PlaylistService {

  private final PlaylistRepository repository;
  private final StaffUserRepository users;
  private final TrackRepository tracks;

  /**
   * Cria uma playlist para a conta autenticada.
   */
  @Transactional
  public PlaylistResponse create(CreatePlaylistRequest request, String username) {
    StaffUser owner = users.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Conta não encontrada"));
    Playlist playlist = new Playlist(
            request.name().strip(), owner, resolveTracks(request.trackIds()));
    return toResponse(repository.save(playlist));
  }

  /**
   * Lista somente as playlists da conta autenticada.
   */
  @Transactional(readOnly = true)
  public List<PlaylistResponse> findAll(String username) {
    return repository.findAllByOwnerUsernameOrderByIdAsc(username).stream()
            .map(this::toResponse)
            .toList();
  }

  /**
   * Consulta uma playlist pertencente à conta autenticada.
   */
  @Transactional(readOnly = true)
  public PlaylistResponse findById(Long id, String username) {
    Playlist playlist = repository.findByIdAndOwnerUsername(id, username)
            .orElseThrow(() -> new NotFoundException("Playlist não encontrada"));
    return toResponse(playlist);
  }

  private List<Track> resolveTracks(List<Long> ids) {
    Map<Long, Track> byId = tracks.findAllById(ids).stream()
            .collect(Collectors.toMap(Track::getId, track -> track));
    return ids.stream().map(id -> {
      Track track = byId.get(id);
      if (track == null) {
        throw new NotFoundException("Música não encontrada: " + id);
      }
      return track;
    }).toList();
  }

  private PlaylistResponse toResponse(Playlist playlist) {
    List<TrackResponse> sequence = playlist.getTracks().stream()
            .map(track -> new TrackResponse(
                    track.getId(), track.getTitle(), track.getArtist(), track.getDurationSeconds()))
            .toList();
    return new PlaylistResponse(
            playlist.getId(), playlist.getName(), playlist.getOwner().getUsername(), sequence);
  }
}
