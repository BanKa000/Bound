package staticmod.anomalies.sound;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import staticmod.core.*;
import staticmod.util.Geo;
import staticmod.util.Sfx;

/** Шаги «сверху», когда игрок глубоко в шахте (Y < 0). */
public class StepsFromAbove extends Anomaly {
    public StepsFromAbove() { super("steps_above", 0.25, 45_000, false, false, false); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) { return p.getY() < 0; }

    @Override public double weight(StaticSaveData d, ServerPlayer p) { return 1 + d.profile(p.getUUID()).mineDescents * 0.1; }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        Sfx.playFor(p, SoundEvents.PLAYER_FOOTSTEPS_STONE, p.getX(), p.getY() + 6.5, p.getZ(), 0.6f, 0.8f);
        srv.tell(20, () -> Sfx.playFor(p, SoundEvents.PLAYER_FOOTSTEPS_STONE, p.getX()+1, p.getY()+6.5, p.getZ(), 0.6f, 0.9f));
        srv.tell(40, () -> Sfx.playFor(p, SoundEvents.PLAYER_FOOTSTEPS_STONE, p.getX()+2, p.getY()+6.5, p.getZ(), 0.6f, 0.85f));
    }
}
