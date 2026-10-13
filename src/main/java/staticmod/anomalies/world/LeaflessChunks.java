package staticmod.anomalies.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import staticmod.core.*;
import staticmod.util.Geo;

/** В 5 чанках вокруг игрока пропадает вся листва. */
public class LeaflessChunks extends Anomaly {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public LeaflessChunks() { super("leafless_chunks", 0.05, 900_000, false, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        int cx = p.blockPosition().getX() >> 4, cz = p.blockPosition().getZ() >> 4;
        for (int i = 0; i < 5; i++) {
            int ox = cx + Geo.RND.nextInt(7) - 3, oz = cz + Geo.RND.nextInt(7) - 3;
            BlockPos from = new BlockPos(ox << 4, Math.max(-64, lvl.getMinBuildHeight()), oz << 4);
            BlockPos to = new BlockPos((ox << 4) + 15, Math.min(319, lvl.getMaxBuildHeight() - 1), (oz << 4) + 15);
            BlockPos.MutableCursor mc = new BlockPos.MutableCursor();
            for (int bx = from.getX(); bx <= to.getX(); bx++)
                for (int bz = from.getZ(); bz <= to.getZ(); bz++)
                    for (mc.set(bx, from.getY(), bz); mc.getY() <= to.getY(); mc.setY(mc.getY() + 1))
                        if (lvl.getBlockState(mc).getBlock() instanceof LeavesBlock)
                            lvl.setBlock(mc, AIR, Block.UPDATE_ALL);
        }
    }
}
