package staticmod.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import staticmod.core.ServerState;
import staticmod.core.StaticMod;

/**
 * Единая сеть STATIC. Клиент ничего не «придумывает» сам: он только
 * 1) сообщает серверу то, что знает лишь он (время ПК, оглядывания,
 *    скриншоты), и 2) исполняет приказы сервера (заголовок окна,
 *    свернуть окно, SMPTE-полосы, VHS...). Все пакеты проходят через
 *    этот класс — по одному месту на канал, без копипасты.
 */
public final class StaticNet {

    // ---- Client -> Server ----
    public static final Identifier C2S_PC_TIME    = StaticMod.id("c2s_pc_time");     // long millis
    public static final Identifier C2S_LOOK_SPIKE = StaticMod.id("c2s_look_spike");  // резкое оглядывание
    public static final Identifier C2S_SCREENSHOT = StaticMod.id("c2s_screenshot");  // игрок сделал F2
    public static final Identifier C2S_CORRECTION = StaticMod.id("c2s_correction");  // игрок «заметил и исправил»

    // ---- Server -> Client (приказы исполнению) ----
    public static final Identifier S2C_WINDOW_TITLE  = StaticMod.id("s2c_window_title");   // String
    public static final Identifier S2C_ICONIFY       = StaticMod.id("s2c_iconify");        // int ms
    public static final Identifier S2C_SKY_OVERLAY   = StaticMod.id("s2c_sky_overlay");    // int kind, int ms
    public static final Identifier S2C_FAKE_CRASH    = StaticMod.id("s2c_fake_crash");     // int kind, int ms
    public static final Identifier S2C_CROSSHAIR_EYE = StaticMod.id("s2c_crosshair_eye");  // int ms
    public static final Identifier S2C_HOTBAR_SHIFT  = StaticMod.id("s2c_hotbar_shift");   // void
    public static final Identifier S2C_XP_NEG        = StaticMod.id("s2c_xp_neg");         // void
    public static final Identifier S2C_DEATH_TITLE   = StaticMod.id("s2c_death_title");    // String
    public static final Identifier S2C_FAKE_BOSSBAR  = StaticMod.id("s2c_fake_bossbar");   // int ms
    public static final Identifier S2C_TELEPORT_FX   = StaticMod.id("s2c_teleport_fx");    // int ms (VHS)
    public static final Identifier S2C_MIRROR_INV    = StaticMod.id("s2c_mirror_inv");     // int ms
    public static final Identifier S2C_LOCALE_SWAP   = StaticMod.id("s2c_locale_swap");    // String lang, int ms
    public static final Identifier S2C_SILENCE       = StaticMod.id("s2c_silence");        // int ms (абсолютная тишина)
    public static final Identifier S2C_HUM           = StaticMod.id("s2c_hum");            // float volume 0..1
    public static final Identifier S2C_REVERSE_DISC  = StaticMod.id("s2c_reverse_disc");   // int ms
    public static final Identifier S2C_GLOW_LIE      = StaticMod.id("s2c_glow_lie");       // int x,y,z,r (ложь света)

    /** все S2C-каналы, которые клиент должен уметь принимать (для регистрации/документации) */
    public static final Identifier[] ALL_S2C = {
        S2C_WINDOW_TITLE, S2C_ICONIFY, S2C_SKY_OVERLAY, S2C_FAKE_CRASH, S2C_CROSSHAIR_EYE,
        S2C_HOTBAR_SHIFT, S2C_XP_NEG, S2C_DEATH_TITLE, S2C_FAKE_BOSSBAR, S2C_TELEPORT_FX,
        S2C_MIRROR_INV, S2C_LOCALE_SWAP, S2C_SILENCE, S2C_HUM, S2C_REVERSE_DISC, S2C_GLOW_LIE
    };

    @FunctionalInterface
    public interface Payload { void write(RegistryFriendlyByteBuf buf); }

    /** отправить пакет одному игроку — единая точка для всех S2C */
    public static void toClient(ServerPlayer p, Identifier channel, Payload payload) {
        if (p == null || p.isRemoved()) return;
        ServerPlayNetworking.send(p, channel,
                writeNow(payload));
    }

    /** зарегистрировать приёмники C2S (вызывается один раз из StaticMod) */
    public static void registerServerReceivers(ServerState st) {
        ServerPlayNetworking.registerGlobalReceiver(C2S_PC_TIME, (srv, pl, buf, sender) ->
                st.pcTimeMillis.put(pl.getUUID(), buf.readLong()));
        ServerPlayNetworking.registerGlobalReceiver(C2S_LOOK_SPIKE, (srv, pl, buf, sender) ->
                srv.execute(() -> st.profile(pl).addLook()));
        ServerPlayNetworking.registerGlobalReceiver(C2S_SCREENSHOT, (srv, pl, buf, sender) ->
                srv.execute(() -> st.onScreenshot(srv, pl)));
        ServerPlayNetworking.registerGlobalReceiver(C2S_CORRECTION, (srv, pl, buf, sender) ->
                srv.execute(() -> st.noticedCorrection(pl)));
    }

    private StaticNet() {}
}
