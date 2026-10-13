package staticmod.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import staticmod.core.*;
import staticmod.entity.StaticEntities;
import staticmod.entity.WeepingAngelEntity;
import staticmod.util.Geo;

/** /static — тестовые команды механик. */
public final class AnomalyCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
            dispatcher.register(Commands.literal("static")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("list").executes(AnomalyCommands::list))
                .then(Commands.literal("fire")
                    .then(Commands.argument("key", StringArgumentType.greedyString())
                        .executes(ctx -> fireByName(ctx.getSource(), StringArgumentType.getString(ctx, "key")))))
                .then(Commands.literal("angel").executes(AnomalyCommands::spawnAngel))
                .then(Commands.literal("fear")
                    .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 1f))
                        .executes(AnomalyCommands::setFear)))
                .then(Commands.literal("reset").executes(AnomalyCommands::reset))
                .then(Commands.literal("info").executes(AnomalyCommands::info))));
    }

    private static int list(CommandSourceStack src) {
        StringBuilder sb = new StringBuilder();
        for (Anomaly a : AnomalyRegistry.ALL) sb.append(a.key).append(' ');
        src.sendSuccess(() -> Component.literal(sb.toString()).withStyle(ChatFormatting.GRAY), false);
        return AnomalyRegistry.ALL.size();
    }

    private static int fireByName(CommandSourceStack src, String key) {
        ServerPlayer p = src.getPlayerOrException();
        MinecraftServer srv = src.getServer();
        StaticSaveData data = StaticData.get(srv);
        for (Anomaly a : AnomalyRegistry.ALL) {
            if (a.key.equalsIgnoreCase(key.trim())) {
                Director.fire(a, srv, p, data);
                StaticData.save(srv);
                src.sendSuccess(() -> Component.literal("вызвано: " + a.key), true);
                return 1;
            }
        }
        src.sendFailure(Component.literal("нет такой аномалии: " + key));
        return 0;
    }

    private static int spawnAngel(CommandSourceStack src) {
        ServerPlayer p = src.getPlayerOrException();
        ServerLevel lvl = p.serverLevel();
        WeepingAngelEntity angel = StaticEntities.WEEPING_ANGEL.create(lvl, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (angel == null) { src.sendFailure(Component.literal("сущность не зарегистрирована")); return 0; }
        angel.moveTo(Geo.groundNear(p, 12), 0, 0);
        lvl.addFreshEntity(angel);
        src.sendSuccess(() -> Component.literal("ангел спавнен в 12 блоках"), true);
        return 1;
    }

    private static int setFear(CommandSourceStack src) {
        ServerPlayer p = src.getPlayerOrException();
        float v = FloatArgumentType.getFloat(src, "value");
        StaticData.get(src.getServer()).profile(p.getUUID()).fear = v;
        src.sendSuccess(() -> Component.literal("fear=" + v), true);
        return 1;
    }

    private static int reset(CommandSourceStack src) {
        MinecraftServer srv = src.getServer();
        StaticSaveData d = StaticData.get(srv);
        d.cooldownEnds.clear(); d.firedEver.clear(); d.nightFired.clear();
        StaticData.save(srv);
        src.sendSuccess(() -> Component.literal("кулдауны и once-флаги сброшены"), true);
        return 1;
    }

    private static int info(CommandSourceStack src) {
        ServerPlayer p = src.getPlayerOrException();
        StaticSaveData d = StaticData.get(src.getServer());
        FearProfile fp = d.profile(p.getUUID());
        src.sendSuccess(() -> Component.literal(String.format(
            "fear=%.2f look=%d walls=%d torches=%d mines=%d runs=%d shots=%d moons=%d",
            fp.fear, fp.lookAround, fp.wallsBuilt, fp.torchesPlaced,
            fp.mineDescents, fp.sprintsAway, fp.screenshots, d.fullMoonsSinceWorldCreate)), false);
        return 1;
    }
}
