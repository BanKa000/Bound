package staticmod.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Общий контракт «сущность, привязанная к игроку». Убирает дублирование между Angel/Stalker. */
public interface Aura {
    ServerPlayer target();
    boolean isFrozen();
    void tickBehavior(Vec3 playerPos, boolean playerLooksAtMe);
}
