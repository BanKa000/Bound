package staticmod.anomalies.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import staticmod.core.*;
import staticmod.util.Geo;

/** В тултипе случайного предмета появился суффикс «· их». */
public class TooltipSuffixIh extends Anomaly {
    public TooltipSuffixIh() { super("tooltip_ih", 0.06, 300_000, false, false, true); }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        var items = p.getInventory().items.stream().filter(st -> !st.isEmpty()).toList();
        if (items.isEmpty()) return;
        ItemStack st = items.get(Geo.RND.nextInt(items.size()));
        var lore = new java.util.ArrayList<>(st.getOrDefaultValue(DataComponents.LORE, java.util.List.of()));
        lore.add(Component.literal("· их").withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
        st.set(DataComponents.LORE, lore);
        p.onUpdateAbilities();
    }
}
