package staticmod.core;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import staticmod.command.AnomalyCommands;
import staticmod.entity.StaticEntities;

/**
 * STATIC — хоррор-мод. Точка входа (серверная часть).
 */
public class StaticMod implements ModInitializer {
    public static final String MOD_ID = "staticmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[STATIC] инициализация...");
        StaticEntities.register();
        AnomalyRegistry.bootstrap();
        AnomalyCommands.register();
        ServerTickHandler.register();
        LOGGER.info("[STATIC] загружено аномалий: {}", AnomalyRegistry.size());
    }
}
