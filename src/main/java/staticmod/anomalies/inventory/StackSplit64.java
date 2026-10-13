package staticmod.anomalies.inventory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import staticmod.core.*;
import staticmod.util.Geo;

/** Стак 64 разделился на 63+1, единица переехала в последний слот. */
public class StackSplit64 extends Anomaly {
    public StackSplit64() { super("stack_split", 0.08, 600_000, false, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        var items = p.getInventory().items;
        int idx = -1;
        for (int i = 0; i < items.size() - 1; i++) {
            ItemStack st = items.get(i);
            if (st.getCount() == 64) { idx = i; break; }
        }
        if (idx < 0) return;
        ItemStack big = items.get(idx);
        ItemStack one = big.split(1);
        int last = items.size() - 1;
        if (items.get(last).isEmpty()) items.set(last, one);
        else big.grow(1); // если последний занят — просто оставляем 63+1 рядом
        p.onUpdateAbilities();
    }
}
