package staticmod.anomalies.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import staticmod.core.*;
import staticmod.util.Geo;

/** «Портал в рай»: рамка ad-портала из светящегося камня, внутри разлита вода. */
public class ParadisePortal extends Anomaly {
    public ParadisePortal() { super("paradise_portal", 0.08, 600_000, true, false, true); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) {
        return Geo.isNight(p) && p.level().dimension() == Level.OVERWORLD;
    }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        BlockPos c = Geo.around(p, 24, 48);
        BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        int x = c.getX(), z = c.getZ();
        for (int dx = 0; dx < 4; dx++) for (int dy = 0; dy < 5; dy++) {
            boolean edge = dx == 0 || dx == 3 || dy == 0 || dy == 4;
            BlockPos bp = new BlockPos(x + dx, c.getY() + dy, z);
            lvl.setBlock(bp, edge ? glow : water, Block.UPDATE_ALL);
        }
    }
}
