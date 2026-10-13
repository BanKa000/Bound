package staticmod.util;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Единый планировщик отложенных задач на стороне сервера.
 * Заменяет srv.tell(...) во всех аномалиях — один файл вместо дублей.
 */
public final class Sched {
    private record Task(long dueTick, Runnable action) {}

    private static final Deque<Task> QUEUE = new ArrayDeque<>();

    private Sched() {}

    /** Выполнить действие через {@code delayTicks} тиков сервера. */
    public static void later(MinecraftServer srv, int delayTicks, Runnable action) {
        synchronized (QUEUE) {
            QUEUE.add(new Task(srv.getTickCount() + delayTicks, action));
        }
    }

    /** Вызывается из ServerTickEvents.END_SERVER_TICK каждый тик. */
    public static void tick(MinecraftServer srv) {
        List<Task> ready = null;
        synchronized (QUEUE) {
            while (!QUEUE.isEmpty() && QUEUE.peekFirst().dueTick() <= srv.getTickCount()) {
                if (ready == null) ready = new ArrayList<>();
                ready.add(QUEUE.pollFirst());
            }
        }
        if (ready != null) for (Task t : ready) t.action().run();
    }
}
