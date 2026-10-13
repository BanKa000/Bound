package staticmod.anomalies.mobs;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import staticmod.core.*;
import staticmod.util.Geo;
import staticmod.util.Registry;

/** Овцы за ночь «остригли себя сами»: комки шерсти вокруг, ножниц нет. */
public class SelfShearedSheep extends Anomaly {
    public SelfShearedSheep() { super("sheared_sheep", 0.12, 300_000, true, false, true); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) { return Geo.isNight(p); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ServerLevel lvl = p.serverLevel();
        for (Sheep s : Geo.nearby(lvl, p.position(), 48, Sheep.class)) {
            if (!s.isSheared()) {
                s.setSheared(true);
                DyeColor c = s.getColor();
                ItemStack wool = new ItemStack(Registry.item(c == null ? "white_wool" : c.getSerializedName() + "_wool"));
                s.spawnAtLocation(wool, 0.5f);
                s.spawnAtLocation(wool.copy(), 0.5f);
            }
        }
    }
}
