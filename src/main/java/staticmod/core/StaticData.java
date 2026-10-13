package staticmod.core;

import net.minecraft.server.MinecraftServer;

/**
 * Доступ к состоянию STATIC per-мир через единый SavedData (StaticSaveData.TYPE).
 * Кэширует runtime-объект на сервер; save() сбрасывает состояние в обёртку.
 */
public final class StaticData {
    private StaticData() {}

    private static final class Holder {
        final StaticSaveData data = new StaticSaveData();
        final StaticSaveData.Wrapper saved;
        Holder(MinecraftServer server) {
            saved = server.getDataStorage().computeIfAbsent(StaticSaveData.TYPE);
            data.load(saved.state);
        }
    }

    private static final java.util.WeakHashMap<MinecraftServer, Holder> CACHE = new java.util.WeakHashMap<>();

    public static StaticSaveData get(MinecraftServer server) {
        return CACHE.computeIfAbsent(server, Holder::new).data;
    }

    /** записать runtime-состояние обратно и пометить сохранение */
    public static void save(MinecraftServer server) {
        Holder h = CACHE.get(server);
        if (h == null) return;
        h.saved.state = h.data.toState();
        h.saved.setDirty(true);
    }
}
