package br.upe.alexapatterns.track.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Faixa musical disponível no catálogo.
 */
@Entity
@Table(name = "tracks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Track {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 100)
  private String artist;

  @Column(name = "duration_seconds", nullable = false)
  private int durationSeconds;

  /**
   * Cria uma faixa com duração em segundos.
   */
  public Track(String title, String artist, int durationSeconds) {
    this.title = title;
    this.artist = artist;
    this.durationSeconds = durationSeconds;
  }
}
