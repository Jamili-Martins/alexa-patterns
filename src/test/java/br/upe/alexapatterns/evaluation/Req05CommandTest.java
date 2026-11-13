package br.upe.alexapatterns.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import br.upe.alexapatterns.assistant.command.CommandQueue;
import br.upe.alexapatterns.assistant.command.NextCommand;
import br.upe.alexapatterns.assistant.command.PauseCommand;
import br.upe.alexapatterns.assistant.command.PlayCommand;
import br.upe.alexapatterns.assistant.command.PlaybackCommand;
import br.upe.alexapatterns.assistant.command.ResumeCommand;
import br.upe.alexapatterns.assistant.command.StopCommand;
import br.upe.alexapatterns.playback.state.MusicPlayer;
import br.upe.alexapatterns.track.entity.Track;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Avalia comandos e fila sem executar os demais padrões.
 */
@DisplayName("Requisito - #05 - Command")
class Req05CommandTest {

  private final MusicPlayer player = mock(MusicPlayer.class);
  private final Track first = new Track("Primeira", "Artista", 180);
  private final Track second = new Track("Segunda", "Artista", 200);

  @Test
  void playDelegaUmaVezComCopiaDosArgumentos() {
    List<Track> source = new ArrayList<>(List.of(first, second, first));
    PlaybackCommand command = new PlayCommand(player, source);
    source.clear();
    verifyNoInteractions(player);

    command.execute();

    verify(player).play(List.of(first, second, first));
    verifyNoMoreInteractions(player);
  }

  @Test
  void pauseDelegaUmaVezSomenteAoExecutar() {
    PlaybackCommand command = new PauseCommand(player);
    verifyNoInteractions(player);

    command.execute();

    verify(player).pause();
    verifyNoMoreInteractions(player);
  }

  @Test
  void resumeDelegaUmaVezSomenteAoExecutar() {
    PlaybackCommand command = new ResumeCommand(player);
    verifyNoInteractions(player);

    command.execute();

    verify(player).resume();
    verifyNoMoreInteractions(player);
  }

  @Test
  void nextDelegaUmaVezSomenteAoExecutar() {
    PlaybackCommand command = new NextCommand(player);
    verifyNoInteractions(player);

    command.execute();

    verify(player).next();
    verifyNoMoreInteractions(player);
  }

  @Test
  void stopDelegaUmaVezSomenteAoExecutar() {
    PlaybackCommand command = new StopCommand(player);
    verifyNoInteractions(player);

    command.execute();

    verify(player).stop();
    verifyNoMoreInteractions(player);
  }

  @Test
  void comandosPropagamMesmaExcecaoSemRepetirExecucao() {
    IllegalStateException failure = new IllegalStateException("Falha no tocador");
    doThrow(failure).when(player).play(anyList());
    doThrow(failure).when(player).pause();
    doThrow(failure).when(player).resume();
    doThrow(failure).when(player).next();
    doThrow(failure).when(player).stop();
    List<PlaybackCommand> commands = List.of(
            new PlayCommand(player, List.of(first)),
            new PauseCommand(player), new ResumeCommand(player),
            new NextCommand(player), new StopCommand(player));
    verifyNoInteractions(player);

    for (PlaybackCommand command : commands) {
      assertSame(failure, assertThrows(IllegalStateException.class, command::execute));
    }

    verify(player).play(List.of(first));
    verify(player).pause();
    verify(player).resume();
    verify(player).next();
    verify(player).stop();
    verifyNoMoreInteractions(player);
  }

  @Test
  void filaVaziaRetornaZeroENaoExecutaPedidos() {
    CommandQueue queue = new CommandQueue();

    assertEquals(0, queue.size());
    assertFalse(queue.executeNext());
    assertEquals(0, queue.executeAll());
    assertEquals(0, queue.size());
  }

  @Test
  void enfileirarNaoExecutaEPreservaOrdemDeInclusao() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    queue.enqueue(() -> calls.add("primeiro"));
    queue.enqueue(() -> calls.add("segundo"));
    queue.enqueue(() -> calls.add("terceiro"));

    assertTrue(calls.isEmpty());
    assertEquals(3, queue.size());

    assertEquals(3, queue.executeAll());
    assertEquals(List.of("primeiro", "segundo", "terceiro"), calls);
    assertEquals(0, queue.size());
    assertEquals(0, queue.executeAll());
    assertEquals(List.of("primeiro", "segundo", "terceiro"), calls);
  }

  @Test
  void executeNextRemoveSomenteUmPedidoPorChamada() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    queue.enqueue(() -> calls.add("primeiro"));
    queue.enqueue(() -> calls.add("segundo"));

    assertTrue(queue.executeNext());
    assertEquals(List.of("primeiro"), calls);
    assertEquals(1, queue.size());

    assertTrue(queue.executeNext());
    assertEquals(List.of("primeiro", "segundo"), calls);
    assertEquals(0, queue.size());

    assertFalse(queue.executeNext());
    assertEquals(List.of("primeiro", "segundo"), calls);
  }

  @Test
  void aceitaMesmaInstanciaDeComandoMaisDeUmaVez() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    PlaybackCommand command = () -> calls.add("pedido");

    queue.enqueue(command);
    queue.enqueue(command);

    assertEquals(2, queue.size());
    assertEquals(2, queue.executeAll());
    assertEquals(List.of("pedido", "pedido"), calls);
    assertEquals(0, queue.size());
  }

  @Test
  void filaMisturaComandosPreservandoArgumentosEDestinatarios() {
    CommandQueue queue = new CommandQueue();
    MusicPlayer other = mock(MusicPlayer.class);
    queue.enqueue(new PlayCommand(player, List.of(first)));
    queue.enqueue(new PauseCommand(player));
    queue.enqueue(new PlayCommand(other, List.of(second)));
    queue.enqueue(new ResumeCommand(player));
    queue.enqueue(new NextCommand(other));
    queue.enqueue(new StopCommand(other));
    verifyNoInteractions(player, other);

    assertEquals(6, queue.executeAll());

    InOrder order = inOrder(player, other);
    order.verify(player).play(List.of(first));
    order.verify(player).pause();
    order.verify(other).play(List.of(second));
    order.verify(player).resume();
    order.verify(other).next();
    order.verify(other).stop();
    order.verifyNoMoreInteractions();
    verifyNoMoreInteractions(player, other);
    assertEquals(0, queue.size());
  }

  @Test
  void falhaNoExecuteAllInterrompeERemovePedidoQueFalhou() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    IllegalStateException failure = new IllegalStateException("Falha no pedido");
    queue.enqueue(() -> calls.add("primeiro"));
    queue.enqueue(() -> {
      calls.add("falhou");
      throw failure;
    });
    queue.enqueue(() -> calls.add("ultimo"));

    assertSame(failure, assertThrows(IllegalStateException.class, queue::executeAll));

    assertEquals(List.of("primeiro", "falhou"), calls);
    assertEquals(1, queue.size());

    assertEquals(1, queue.executeAll());
    assertEquals(List.of("primeiro", "falhou", "ultimo"), calls);
    assertEquals(0, queue.size());
  }

  @Test
  void falhaNoExecuteNextMantemPedidosSeguintes() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    IllegalStateException failure = new IllegalStateException("Falha no pedido");
    queue.enqueue(() -> {
      calls.add("falhou");
      throw failure;
    });
    queue.enqueue(() -> calls.add("ultimo"));

    assertSame(failure, assertThrows(IllegalStateException.class, queue::executeNext));

    assertEquals(List.of("falhou"), calls);
    assertEquals(1, queue.size());

    assertTrue(queue.executeNext());
    assertEquals(List.of("falhou", "ultimo"), calls);
    assertEquals(0, queue.size());
  }

  @Test
  void clearDescartaPendenciasSemExecutarEPermiteReutilizarFila() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    queue.enqueue(() -> calls.add("descartado"));

    queue.clear();

    assertTrue(calls.isEmpty());
    assertEquals(0, queue.size());
    assertEquals(0, queue.executeAll());
    queue.clear();

    queue.enqueue(() -> calls.add("novo"));
    assertEquals(1, queue.executeAll());
    assertEquals(List.of("novo"), calls);
  }

  @Test
  void rejeitaNullSemAlterarPedidosExistentes() {
    CommandQueue queue = new CommandQueue();
    List<String> calls = new ArrayList<>();
    queue.enqueue(() -> calls.add("valido"));

    assertThrows(NullPointerException.class, () -> queue.enqueue(null));

    assertEquals(1, queue.size());
    assertEquals(1, queue.executeAll());
    assertEquals(List.of("valido"), calls);
  }

  @Test
  void filasNaoCompartilhamPedidos() {
    CommandQueue left = new CommandQueue();
    CommandQueue right = new CommandQueue();
    List<String> calls = new ArrayList<>();
    left.enqueue(() -> calls.add("esquerda"));
    assertEquals(0, right.size());

    right.enqueue(() -> calls.add("direita"));
    assertEquals(1, left.size());
    assertEquals(1, right.size());

    assertEquals(1, left.executeAll());
    assertEquals(List.of("esquerda"), calls);
    assertEquals(1, right.size());

    assertEquals(1, right.executeAll());
    assertEquals(List.of("esquerda", "direita"), calls);
    assertEquals(0, left.size());
    assertEquals(0, right.size());
  }
}
