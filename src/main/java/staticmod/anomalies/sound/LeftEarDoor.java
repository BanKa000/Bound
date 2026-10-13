package staticmod.anomalies.sound;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import staticmod.core.*;
import staticmod.util.Sfx;

/** Скрип двери слышен только в левом наушнике. */
public class LeftEarDoor extends Anomaly {
    public LeftEarDoor() { super("left_door", 0.15, 60_000, true, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        Sfx.leftEarOnly(p, SoundEvents.DOOR_OPEN);
    }
}
