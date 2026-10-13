package staticmod.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Единственный объект сохраняемых данных STATIC (per-мир).
 * Всё состояние аномалий, профили страха, счётчики ночей/полнолуний — здесь.
 */
public class StaticSaveData {

    /** сериализуемое состояние */
    public record State(
            long lastRealJoinMillis,
            int fullMoonsSinceWorldCreate,
            long worldCreatedRealMillis,
            Map<String, Long> cooldownEnds,     // key anomaly -> System.currentTimeMillis() до которого нельзя
            Set<String> firedEver,              // onceEver ключи
            String nightKeyFired,               // "дн.фаза:tickday" — набор сработавших за ночь
            Set<String> nightFired,
            Map<String, String> fearProfiles,   // uuid -> FearProfile.serialize()
            Map<String, double[]> lastLogout,   // uuid -> [x,y,z,dim]
            List<String> deathCoords,           // уже показанные координаты смертей (не повторяются)
            Optional<BlockPos> buriedStructure, // «погребённая структура» для стрелки из звёзд
            String markedPlayer,                // uuid «помеченного»
            Map<String, double[]> bases         // uuid -> база [x,y,z,dim]
    ) {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("last_real_join").orElse(-1L).forGetter(State::lastRealJoinMillis),
                Codec.INT.fieldOf("full_moons").orElse(0).forGetter(State::fullMoonsSinceWorldCreate),
                Codec.LONG.fieldOf("world_created").orElse(-1L).forGetter(State::worldCreatedRealMillis),
                Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("cooldowns").orElse(Map.of()).forGetter(State::cooldownEnds),
                Codec.STRING.setOf().fieldOf("fired_ever").orElse(Set.of()).forGetter(State::firedEver),
                Codec.STRING.optionalFieldOf("night_key").forGetter(s -> Optional.ofNullable(s.nightKeyFired())),
                Codec.STRING.setOf().fieldOf("night_fired").orElse(Set.of()).forGetter(State::nightFired),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("fear").orElse(Map.of()).forGetter(State::fearProfiles),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE.listOf(2, 4)).fieldOf("last_logout").orElse(Map.of()).forGetter(State::lastLogout),
                Codec.STRING.listOf().fieldOf("death_coords").orElse(List.of()).forGetter(State::deathCoords),
                BlockPos.CODEC.optionalFieldOf("buried").forGetter(State::buriedStructure),
                Codec.STRING.fieldOf("marked").orElse("").forGetter(State::markedPlayer),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE.listOf(2, 4)).fieldOf("bases").orElse(Map.of()).forGetter(State::bases)
        ).apply(i, State::new));
    }

    public final Map<String, Long> cooldownEnds = new HashMap<>();
    public final Set<String> firedEver = new HashSet<>();
    public final Set<String> nightFired = new HashSet<>();
    public String nightKeyFired = "";
    public final Map<UUID, FearProfile> fear = new HashMap<>();
    public final Map<UUID, double[]> lastLogoutPos = new HashMap<>();
    public final List<String> shownDeathCoords = new ArrayList<>();
    /** «помеченный» игрок (uuid) — сущность охотится только на него */
    public String markedPlayer = "";
    /** база игрока для телепорта после погони: uuid -> [x,y,z,dim] */
    public final Map<UUID, double[]> playerBase = new HashMap<>();
    public long lastRealJoinMillis = -1;
    public long worldCreatedRealMillis = -1;
    public int fullMoonsSinceWorldCreate = 0;
    public BlockPos buriedStructure = null;

    public FearProfile profile(UUID id) {
        return fear.computeIfAbsent(id, k -> new FearProfile());
    }

    public boolean isNightOver(long dayTime) { return dayTime > 23000 || dayTime < 12000; }

    public State toState() {
        Map<String, String> fp = new HashMap<>();
        fear.forEach((k, v) -> fp.put(k.toString(), v.serialize()));
        Map<String, double[]> ll = new HashMap<>();
        lastLogoutPos.forEach((k, v) -> ll.put(k.toString(), v));
        Map<String, double[]> bs = new HashMap<>();
        playerBase.forEach((k, v) -> bs.put(k.toString(), v));
        return new State(lastRealJoinMillis, fullMoonsSinceWorldCreate, worldCreatedRealMillis,
                new HashMap<>(cooldownEnds), new HashSet<>(firedEver), nightKeyFired,
                new HashSet<>(nightFired), fp, ll, new ArrayList<>(shownDeathCoords),
                Optional.ofNullable(buriedStructure), markedPlayer, bs);
    }

    public void load(State s) {
        lastRealJoinMillis = s.lastRealJoinMillis();
        worldCreatedRealMillis = s.worldCreatedRealMillis();
        fullMoonsSinceWorldCreate = s.fullMoonsSinceWorldCreate();
        cooldownEnds.clear(); cooldownEnds.putAll(s.cooldownEnds());
        firedEver.clear(); firedEver.addAll(s.firedEver());
        nightKeyFired = s.nightKeyFired() == null ? "" : s.nightKeyFired();
        nightFired.clear(); nightFired.addAll(s.nightFired());
        fear.clear();
        s.fearProfiles().forEach((k, v) -> { try { fear.put(UUID.fromString(k), FearProfile.deserialize(v)); } catch (Exception ignored) {} });
        lastLogoutPos.clear();
        s.lastLogout().forEach((k, v) -> { try { lastLogoutPos.put(UUID.fromString(k), v); } catch (Exception ignored) {} });
        shownDeathCoords.clear(); shownDeathCoords.addAll(s.deathCoords());
        buriedStructure = s.buriedStructure().orElse(null);
        markedPlayer = s.markedPlayer() == null ? "" : s.markedPlayer();
        playerBase.clear();
        s.bases().forEach((k, v) -> { try { playerBase.put(UUID.fromString(k), v); } catch (Exception ignored) {} });
    }
}
