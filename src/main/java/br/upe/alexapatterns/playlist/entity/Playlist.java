package br.upe.alexapatterns.playlist.entity;

import br.upe.alexapatterns.staff.entity.StaffUser;
import br.upe.alexapatterns.track.entity.Track;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Playlist de uma conta, com faixas em uma ordem definida.
 */
@Entity
@Table(name = "playlists")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private StaffUser owner;

  @Getter(AccessLevel.NONE)
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "playlist_tracks",
          joinColumns = @JoinColumn(name = "playlist_id"),
          inverseJoinColumns = @JoinColumn(name = "track_id"))
  @OrderColumn(name = "track_order")
  private List<Track> tracks = new ArrayList<>();

  /**
   * Cria uma playlist preservando a ordem e as repetições das faixas.
   */
  public Playlist(String name, StaffUser owner, List<Track> tracks) {
    this.name = name;
    this.owner = owner;
    this.tracks = new ArrayList<>(tracks);
  }

  /**
   * Devolve uma cópia não modificável da sequência de faixas.
   */
  public List<Track> getTracks() {
    return List.copyOf(tracks);
  }
}
