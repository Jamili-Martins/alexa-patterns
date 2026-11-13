package br.upe.alexapatterns.assistant.session;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mantém uma sessão em memória por conta e serializa suas operações.
 */
@Component
@RequiredArgsConstructor
public class PlaybackSessions {

  private final PlaybackSessionFactory factory;
  private final Map<String, PlaybackSession> sessions = new ConcurrentHashMap<>();

  /**
   * Executa uma operação com acesso exclusivo à sessão da conta informada.
   */
  public <T> T withSession(String username, Function<PlaybackSession, T> operation) {
    Objects.requireNonNull(username, "username");
    Objects.requireNonNull(operation, "operation");
    PlaybackSession session = sessions.computeIfAbsent(username, key -> factory.create());
    synchronized (session) {
      return operation.apply(session);
    }
  }
}
