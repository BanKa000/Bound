package staticmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

/**
 * Единая отправка звуков. «Только в левое ухо», «задом наперёд» и т.п.
 * делаются параметрами, а не копипастой.
 */
public final class Sfx {

    /** сыграть всем в радиусе (обычный способ) */
    public static void play(ServerLevel lvl, BlockPos pos, SoundEvent sound, float vol, float pitch) {
        lvl.playSound(null, pos, sound, SoundSource.RECORDS, vol, pitch);
    }

    /** сыграть Лично одному игроку (никто другой не услышит) */
    public static void playFor(ServerPlayer p, SoundEvent sound, double x, double y, double z, float vol, float pitch) {
        p.connection.send(new ClientboundSoundPacket(
                Holder.direct(sound), SoundSource.PLAYERS, x, y, z, vol, pitch, p.level().getRandom().nextLong()));
    }

    /**
     * «Скрип только в левом наушнике»: позиционируем источник строго слева
     * от точки взгляда игрока на близкой дистанции — стерео-панорама Minecraft
     * сама сделает «только лево».
     */
    public static void leftEarOnly(ServerPlayer p, SoundEvent sound) {
        var look = p.getLookAngle().cross(net.minecraft.world.phys.Vec3.UNIT_Y).normalize().reverse();
        var pos = p.position().add(look.scale(1.2)).add(0, 0.3, 0);
        p.connection.send(new ClientboundSoundPacket(
                Holder.direct(sound), SoundSource.PLAYERS, pos.x, pos.y, pos.z, 0.7f, 0.8f, p.level().getRandom().nextLong()));
    }

    /** стук по слогам сообщения — ритм задаётся списком задержек */
    public static void knockPattern(ServerPlayer p, SoundEvent knock, int[] delaysTicks) {
        for (int d : delaysTicks) {
            p.serverLevel().getServer().tell(d, () ->
                    playFor(p, knock, p.getX(), p.getY(), p.getZ(), 1.0f, 0.9f + (d % 3) * 0.05f));
        }
    }
}
