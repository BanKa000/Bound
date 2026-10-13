package staticmod.core;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Обёртка SavedDataType для StaticSaveData (per-мир, data/staticmod_state.dat).
 * Runtime-объект кэшируется на сервер; save() сбрасывает состояние в SavedData.
 */
public final class StaticData {

    public static SavedDataType<StaticSaveData.State> TYPE = null;
    private static final java.util.WeakHashMap<MinecraftServer, Holder> CACHE = new java.util.WeakHashMap<>();

    private static void init() {
        if (TYPE == null) {
            TYPE = new SavedDataType<>(
                    "staticmod_state",
                    () -> new StaticSaveData().toState(),
                    StaticSaveData.State.CODEC,
                    NbtOps.INSTANCE,
                    Tag.TYPE_COMPOUND
            );
        }
    }

    /** хранилище + runtime-данные одного мира */
    private static final class Holder {
        final StaticSaveData data = new StaticSaveData();
        final SavedData<StaticSaveData.State> saved;

        Holder(MinecraftServer server) {
            init();
            saved = server.getDataStorage().computeIfAbsent(TYPE, data::toState);
            data.load(saved.get());
        }
    }

    public static StaticSaveData get(MinecraftServer server) {
        return CACHE.computeIfAbsent(server, Holder::new).data;
    }

    /** записать runtime-состояние обратно и пометить сохранение */
    public static void save(MinecraftServer server) {
        Holder h = CACHE.get(server);
        if (h == null) return;
        h.saved.set(h.data.toState()); // VanillaSavedData#set обновляет значение и вызывает setDirty()
    }
}
