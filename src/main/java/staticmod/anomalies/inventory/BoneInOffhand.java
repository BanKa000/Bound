package staticmod.anomalies.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import staticmod.core.*;

/** В оффхенде кость с именем «ты», которую ты не брал. */
public class BoneInOffhand extends Anomaly {
    public BoneInOffhand() { super("bone_offhand", 0.05, 900_000, false, false, true); }

    @Override public boolean canFire(StaticSaveData d, ServerPlayer p) {
        return p.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }

    @Override public void fire(MinecraftServer srv, ServerPlayer p, StaticSaveData d) {
        ItemStack bone = new ItemStack(Items.BONE);
        bone.set(DataComponents.CUSTOM_NAME, Component.literal("ты").withStyle(net.minecraft.ChatFormatting.ITALIC));
        p.setItemSlot(EquipmentSlot.OFFHAND, bone);
    }
}
