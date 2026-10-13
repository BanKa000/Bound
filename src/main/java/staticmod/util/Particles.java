package staticmod.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Единая отправка частиц «лично одному» (клиентская ложь: глаза, VHS-помехи и т.п.). */
public final class Particles {

    public static void forOne(ServerPlayer p, ParticleOptions type, Vec3 pos, int count, double spread) {
        ServerLevel lvl = p.serverLevel();
        // одиночный игрок не входит в viewers списка — broadcastAll его не догонит,
        // поэтому шлём packet напрямую ему И всем смотрящим на эту точку
        lvl.sendParticles(p, type, false, pos.x, pos.y, pos.z, count, spread, spread, spread, 0.0);
        lvl.sendParticles(null, type, pos, count, spread, spread, spread, 0.0, true);
    }

    public static void eyesAt(ServerPlayer p, ParticleOptions dust, Vec3 base) {
        Vec3 left = base.add(-0.18, 0, 0), right = base.add(0.18, 0, 0);
        forOne(p, dust, left, 4, 0.02);
        forOne(p, dust, right, 4, 0.02);
    }
}
