package staticmod.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import staticmod.core.StaticMod;

/** Регистрация всех сущностей STATIC. */
public final class StaticEntities {
    public static EntityType<WeepingAngelEntity> WEEPING_ANGEL;
    public static EntityType<StalkerEntity> STALKER;

    public static void register() {
        WEEPING_ANGEL = build("weeping_angel", MobCategory.MONSTER, 0.6f, 1.95f,
                (t, l) -> new WeepingAngelEntity(t, l));
        STALKER = build("stalker", MobCategory.MONSTER, 0.6f, 1.9f,
                (t, l) -> new StalkerEntity(t, l));
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> build(
            String name, MobCategory cat, float w, float h, EntityType.EntityFactory<T> factory) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.ENTITY_TYPE, StaticMod.id(name));
        EntityType<T> type = EntityType.Builder.of(factory, cat).sized(w, h).build(key);
        BuiltInRegistries.ENTITY_TYPE.register(key, type);
        return type;
    }
}
