package staticmod.anomalies.sound;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.util.Calendar;
import staticmod.core.*;
import staticmod.util.Sfx;

/** Тиканье часов, синхронное с реальными часами ПК игрока (редко). */
public class ClockTick extends Anomaly {
    public ClockTick() { super("clock_tick", 0.08, 120_000, false, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        // тон выше днём, ниже ночью; тикает ровно час
        for (int i = 0; i < Math.max(1, hour % 12 == 0 ? 12 : hour % 12); i++) {
            final float pitch = 1.4f - (hour >= 22 || hour <= 5 ? 0.3f : 0f);
            srv.tell(i * 20, () -> Sfx.playFor(p, SoundEvents.NOTE_BLOCK_HARP, p.getX(), p.getY(), p.getZ(), 0.35f, pitch));
        }
    }
}
