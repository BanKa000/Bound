package staticmod.anomalies.world;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import staticmod.core.*;
import staticmod.util.Geo;

/** Табличка с координатами кровати игрока (у каждого — свои). Редко. */
public class BedSignCoords extends Anomaly {
    private static final BlockState SIGN = Blocks.OAK_SIGN.defaultBlockState();
    public BedSignCoords() { super("bed_sign", 0.04, 300_000, false, false, false); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        BlockPos bed = p.getRespawnPosition() != null ? p.getRespawnPosition().pos() : null;
        if (bed == null) return;
        BlockPos at = Geo.around(p, 4, 9);
        lvl.setBlock(at, SIGN, Block.UPDATE_ALL);
        if (lvl.getBlockEntity(at) instanceof SignBlockEntity sign) {
            var text = sign.getText(false);
            text.setMessage(0, Component.literal("спи здесь"), true);
            text.setMessage(1, Component.literal(bed.getX() + " " + bed.getY() + " " + bed.getZ()), true);
            sign.updateText(text, true);
        }
    }
}
