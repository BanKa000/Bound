package staticmod.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * «Режиссёр»: выбирает, какая аномалия сработает сейчас.
 * Учитывает кривую напряжения L4D (на пике страха — тишина),
 * профиль страха (веса), одиночность сервера, кулдауны и «раз за ночь».
 */
public class Director {

    private static final Random RND = new Random();
    /** окно тишины после всплеска событий, тики */
    private int quietTicks = 0;
    private int eventsRecent = 0;
    private int eventWindowTick = 0;

    public void tick(MinecraftServer server, StaticSaveData data) {
        if (quietTicks > 0) { quietTicks--; return; }

        // скользящее окно событий: 12k тиков (~6 мин)
        if (server.getTickCount() - eventWindowTick > 12000) { eventsRecent = 0; eventWindowTick = server.getTickCount(); }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return;
        ServerPlayer player = players.get(RND.nextInt(players.size()));
        FearProfile fp = data.profile(player.getUUID());

        // кривая напряжения: на пике страха — ноль событий (тишина как наказание)
        double tension = Math.min(1.0, fp.fear + eventsRecent * 0.2);
        if (tension >= 0.9) {
            quietTicks = 1200 + RND.nextInt(1200); // 1–2 минуты тишины
            return;
        }

        long dayTime = player.level().getDayTime() % 24000L;
        boolean night = dayTime > 13000 && dayTime < 23500;

        // жребий раз в ~20 секунд
        if (server.getTickCount() % 400 != 0) return;
        double roll = RND.nextDouble();
        double threshold = 0.25 + tension * 0.4; // чем напряжённее мир, тем чаще странности
        if (roll > threshold) return;

        Anomaly picked = pick(data, player, night, fp);
        if (picked != null) {
            fire(picked, server, player, data);
            eventsRecent++;
        }
    }

    private Anomaly pick(StaticSaveData data, ServerPlayer player, boolean night, FearProfile fp) {
        long now = System.currentTimeMillis();
        List<Anomaly> pool = new ArrayList<>();
        double totalW = 0;
        for (Anomaly a : AnomalyRegistry.ALL) {
            if (!eligible(a, data, player, night, now)) continue;
            double w = a.weight(data, player);
            if (w <= 0) continue;
            totalW += w;
            pool.add(a);
        }
        if (pool.isEmpty()) return null;
        double r = RND.nextDouble() * totalW;
        for (Anomaly a : pool) { r -= a.weight(data, player); if (r <= 0) return a; }
        return pool.get(pool.size() - 1);
    }

    /** единая проверка доступности — все аномалии пользуются ею, не дублируя логику */
    public static boolean eligible(Anomaly a, StaticSaveData data, ServerPlayer player, boolean night, long now) {
        if (a.soloOnly && player.getServer().getPlayerList().getCount() > 1) return false;
        if (data.cooldownEnds.getOrDefault(a.key, 0L) > now) return false;
        if (a.onceEver && data.firedEver.contains(a.key)) return false;
        if (a.oncePerNight && night && data.nightFired.contains(a.key)) return false;
        try { return a.canFire(data, player); } catch (Exception e) { return false; }
    }

    public static void fire(Anomaly a, MinecraftServer server, ServerPlayer player, StaticSaveData data) {
        long now = System.currentTimeMillis();
        data.cooldownEnds.put(a.key, now + a.cooldownMs);
        if (a.onceEver) data.firedEver.add(a.key);
        long dayTime = player.level().getDayTime() % 24000L;
        boolean night = dayTime > 13000 && dayTime < 23500;
        if (a.oncePerNight && night) data.nightFired.add(a.key);
        StaticMod.LOGGER.info("[STATIC] аномалия: {}", a.key);
        try {
            a.fire(server, player, data);
        } catch (Exception e) {
            StaticMod.LOGGER.warn("[STATIC] {} упала: {}", a.key, e.toString());
        }
    }
}
