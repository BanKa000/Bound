package staticmod.anomalies.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import staticmod.core.*;
import staticmod.util.Geo;

/** Факел мигает ровно один раз за ночь — только когда ты НЕ смотришь в его сторону. */
public class FlickerTorch extends Anomaly {
    public FlickerTorch() { super("flicker_torch", 0.35, 20_000, true, false, false); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) { return Geo.isNight(p); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        BlockPos origin = p.blockPosition();
        for (BlockPos bp : BlockPos.betweenClosed(origin.offset(-8,-4,-8), origin.offset(8,4,8))) {
            BlockState st = lvl.getBlockState(bp);
            if (st.is(Blocks.TORCH) || st.is(Blocks.WALL_TORCH)) {
                Vec3 v = new Vec3(bp.getX()+0.5, bp.getY()+0.5, bp.getZ()+0.5);
                if (Geo.playerLooksAt(p, v, 90)) continue;   // «всегда когда не смотришь»
                boolean stayDark = Geo.RND.nextInt(3) == 0;  // может остаться незажжённым
                lvl.setBlock(bp, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                if (!stayDark) srv.tell(10, () -> lvl.setBlock(bp, st, Block.UPDATE_ALL));
                else srv.tell(6000, () -> lvl.setBlock(bp, st, Block.UPDATE_ALL)); // вернётся через 5 мин
                return;
            }
        }
    }
}
