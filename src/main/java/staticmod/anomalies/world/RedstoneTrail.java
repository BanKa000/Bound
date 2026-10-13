package staticmod.anomalies.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import staticmod.core.*;
import staticmod.util.Geo;

/** Линия красной пыли от двери игрока на 40 блоков в чащу. */
public class RedstoneTrail extends Anomaly {
    private static final BlockState WIRE = Blocks.REDSTONE_WIRE.defaultBlockState();
    public RedstoneTrail() { super("redstone_trail", 0.06, 600_000, true, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        double a = Geo.RND.nextDouble() * Math.PI * 2;
        double x = p.getX(), z = p.getZ();
        int y = p.blockPosition().getY();
        for (int i = 0; i < 40; i++) {
            x += Math.cos(a); z += Math.sin(a);
            BlockPos cur = BlockPos.containing(x, y, z);
            // поднимайся/спускайся по рельефу
            while (!lvl.getBlockState(cur).isAir() && cur.getY() < lvl.getMaxBuildHeight()) cur = cur.above();
            while (cur.getY() > lvl.getMinBuildHeight() && lvl.getBlockState(cur.below()).isAir()) cur = cur.below();
            if (lvl.getBlockState(cur).isAir() && !lvl.getBlockState(cur.below()).isAir())
                lvl.setBlock(cur, WIRE, Block.UPDATE_ALL);
        }
    }
}
