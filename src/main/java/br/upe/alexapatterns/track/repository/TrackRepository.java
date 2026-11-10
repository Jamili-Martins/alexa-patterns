package br.upe.alexapatterns.track.repository;

import br.upe.alexapatterns.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistência das faixas musicais.
 */
public interface TrackRepository extends JpaRepository<Track, Long> {
}
