package staticmod.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Безопасный поиск ванильных записей по id (на случай переименований в 26.x). */
public final class Registry {
    public static Item item(String path) {
        var r = BuiltInRegistries.ITEM.getOptional(ResourceLocation.withDefaultNamespace(path));
        return r.orElse(Items.AIR);
    }
}
