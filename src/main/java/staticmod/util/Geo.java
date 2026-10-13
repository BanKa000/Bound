package staticmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/** Общая геометрия/поиск — переиспользуется всеми аномалиями и сущностями. */
public final class Geo {
    public static final Random RND = new Random();

    /** точка в радиусе [minD,maxD] вокруг игрока, предпочтительно загруженная */
    public static BlockPos around(ServerPlayer p, int minD, int maxD) {
        ServerLevel lvl = p.serverLevel();
        for (int i = 0; i < 32; i++) {
            double a = RND.nextDouble() * Math.PI * 2;
            double d = minD + RND.nextDouble() * (maxD - minD);
            BlockPos bp = BlockPos.containing(p.getX() + Math.cos(a) * d, p.getY(), p.getZ() + Math.sin(a) * d);
            if (lvl.isLoaded(bp)) return bp.below(lvl.getHighestBlockYAt(bp) == 0 ? 0 : 0).atY(lvl.getHeight(bp));
        }
        return p.blockPosition().offset(minD, 0, 0);
    }

    /** позиция на земле рядом */
    public static Vec3 groundNear(ServerPlayer p, double dist) {
        BlockPos bp = around(p, (int) dist, (int) dist + 6);
        ServerLevel lvl = p.serverLevel();
        int y = 16 + RND.nextInt(Math.max(1, lvl.getMinBuildHeight() * -1 + 128));
        // простой поиск поверхности сверху вниз
        for (int yy = lvl.getMaxBuildHeight() - 1; yy > lvl.getMinBuildHeight(); yy--) {
            BlockPos cur = bp.atY(yy);
            if (lvl.getBlockState(cur).isAir() && !lvl.getBlockState(cur.below()).isAir()) {
                return Vec3.atBottomCenterOf(cur);
            }
        }
        return Vec3.atBottomCenterOf(bp);
    }

    public static boolean isNight(Player p) {
        long t = p.level().getDayTime() % 24000L;
        return t > 13000 && t < 23500;
    }

    public static <T extends Entity> List<T> nearby(ServerLevel lvl, Vec3 c, double r, Class<T> type) {
        return lvl.getEntitiesOfClass(type, c.cube(r));
    }

    public static LivingEntity nearestMob(ServerLevel lvl, Vec3 c, double r) {
        LivingEntity best = null; double bd = r * r;
        for (LivingEntity e : lvl.getEntitiesOfClass(LivingEntity.class, c.cube(r), e -> !(e instanceof Player))) {
            double d = e.distanceToSqr(c);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    /** смотрит ли игрок на точку (для Weeping Angel / глаз) */
    public static boolean playerLooksAt(ServerPlayer p, Vec3 target, double fovDeg) {
        Vec3 look = p.getLookAngle();
        Vec3 to = target.subtract(p.getEyePosition()).normalize();
        double dot = look.dot(to);
        double cos = Math.cos(Math.toRadians(fovDeg / 2));
        return dot > cos;
    }
}
