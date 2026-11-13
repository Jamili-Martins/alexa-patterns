package br.upe.alexapatterns.playlist.repository;

import br.upe.alexapatterns.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistência das playlists com consulta por proprietário.
 */
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

  @EntityGraph(attributePaths = {"owner", "tracks"})
  List<Playlist> findAllByOwnerUsernameOrderByIdAsc(String username);

  @EntityGraph(attributePaths = {"owner", "tracks"})
  Optional<Playlist> findByIdAndOwnerUsername(Long id, String username);
}
