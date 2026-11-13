package br.upe.alexapatterns.assistant.session;

import br.upe.alexapatterns.assistant.command.CommandQueue;
import br.upe.alexapatterns.playback.observer.PlaybackHistory;
import br.upe.alexapatterns.playback.observer.PlaybackPanel;
import br.upe.alexapatterns.playback.observer.PlaybackPublisher;
import br.upe.alexapatterns.playback.state.MusicPlayer;

/**
 * Agrupa os objetos de reprodução pertencentes a uma conta.
 */
public record PlaybackSession(
        MusicPlayer player,
        CommandQueue queue,
        PlaybackPublisher publisher,
        PlaybackPanel panel,
        PlaybackHistory history
) {
}
