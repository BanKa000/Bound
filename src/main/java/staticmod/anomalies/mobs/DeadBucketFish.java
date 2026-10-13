package staticmod.anomalies.mobs;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import staticmod.core.*;

/** Рыбка в ведре «была жива»: переименовываем, ставим скрытый лор-маркер. */
public class DeadBucketFish extends Anomaly {
    public DeadBucketFish() { super("dead_fish", 0.05, 600_000, false, false, true); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) {
        for (ItemStack st : p.getInventory().items) if (st.is(Items.TROPICAL_FISH_BUCKET)) return true;
        return false;
    }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        for (ItemStack st : p.getInventory().items) {
            if (st.is(Items.TROPICAL_FISH_BUCKET)) {
                st.set(DataComponents.CUSTOM_NAME, Component.literal("была жива"));
                st.set(DataComponents.LORE, java.util.List.of(Component.literal(""))); // маркер static:dead
                p.onUpdateAbilities(); // триггер обновления инвентаря на клиенте
            }
        }
    }
}
