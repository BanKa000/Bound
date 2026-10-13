package staticmod.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Контракт одной аномалии. Реализации лежат в пакете anomalies/,
 * по одному маленькому классу на механику — без дублирования:
 * шансы / «раз за ночь» / кулдауны обрабатывает ядро, реализация
 * пишет только саму «странность».
 */
public abstract class Anomaly {
    /** уникальный ключ, используется в сейве для cooldown/once-флагов */
    public final String key;
    /** базовый шанс сработать при попадании в жребий тика, 0..1 */
    public final double chance;
    /** минимальный интервал между срабатываниями, мс реального времени */
    public final long cooldownMs;
    /** true = не чаще одного раза за игровую ночь */
    public final boolean oncePerNight;
    /** true = срабатывает один раз за всё время игры */
    public final boolean onceEver;
    /** true = только когда на сервере один игрок («одиночный тест») */
    public final boolean soloOnly;

    protected Anomaly(String key, double chance, long cooldownMs,
                      boolean oncePerNight, boolean onceEver, boolean soloOnly) {
        this.key = key;
        this.chance = chance;
        this.cooldownMs = cooldownMs;
        this.oncePerNight = oncePerNight;
        this.onceEver = onceEver;
        this.soloOnly = soloOnly;
    }

    public Anomaly(String key) { this(key, 0.05, 60_000, false, false, false); }

    /** может ли вообще появиться сейчас (условия мира/игрока) */
    public boolean canFire(StaticSaveData data, ServerPlayer player) { return true; }

    /** собственно эффект. Вызывается с серверного потока. */
    public abstract void fire(MinecraftServer server, ServerPlayer player, StaticSaveData data);

    /** вес для «режиссёра»: страшилки, привязанные к страху игрока, весят больше */
    public double weight(StaticSaveData data, ServerPlayer player) { return 1.0; }
}
