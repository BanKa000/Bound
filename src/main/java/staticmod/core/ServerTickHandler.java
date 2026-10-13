package staticmod.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/** Жизненный цикл: держим реестр/данные, крутим «режиссёра», сохраняем мир. */
public final class ServerTickHandler {
    private static Director director;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            director = new Director();
            StaticData.get(server); // прогрев загрузки сейва
            if (StaticData.get(server).worldCreatedRealMillis < 0) {
                StaticData.get(server).worldCreatedRealMillis = System.currentTimeMillis();
                StaticData.save(server);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (director == null) return;
            StaticSaveData data = StaticData.get(server);
            director.tick(server, data);
            long t = server.getTickCount();
            if (t % 6000 == 0) StaticData.save(server);           // автосейв раз в 5 минут
            if (t % 1200 == 0) trackNight(server, data);          // смена дня/ночи
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> StaticData.save(server));
    }

    /** сбрасываем «раз за ночь» на рассвете, считаем полнолуния */
    private static void trackNight(MinecraftServer server, StaticSaveData data) {
        var over = server.overworld();
        if (over == null) return;
        long dayTime = over.getDayTime() % 24000L;
        long day = over.getDayTime() / 24000L;
        String nightKey = "night_" + day;
        if (!nightKey.equals(data.nightKeyFired) && dayTime > 23500 || dayTime < 1000) {
            // рассвет: новая ночь → чистим набор oncePerNight
            if (!data.nightKeyFired.isEmpty() && !data.nightKeyFired.equals(nightKey)) {
                data.nightFired.clear();
            }
            data.nightKeyFired = nightKey;
            // полнолуние: фаза луны = 0 каждые 8 дней
            if (day % 8 == 0) data.fullMoonsSinceWorldCreate++;
        }
    }
}
