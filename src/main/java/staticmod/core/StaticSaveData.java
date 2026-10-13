package staticmod.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

/**
 * Единственный объект сохраняемых данных STATIC (per-мир, data/staticmod_state.dat).
 * Всё состояние аномалий, профили страха, счётчики ночей/полнолуний — здесь.
 */
public class StaticSaveData extends SavedData {

    /** сериализуемое состояние */
    public record State(
            long lastRealJoinMillis,
            int fullMoonsSinceWorldCreate,
            long worldCreatedRealMillis,
            Map<String, Long> cooldownEnds,
            Set<String> firedEver,
            String nightKeyFired,
            Set<String> nightFired,
            Map<String, String> fearProfiles,
            Map<String, List<Double>> lastLogout,
            List<String> deathCoords,
            Optional<BlockPos> buriedStructure,
            String markedPlayer,
            Map<String, List<Double>> bases
    ) {
        private static final Codec<List<Double>> VEC =
                Codec.DOUBLE.listOf(Codec.DOUBLE, 2, 4);

        public static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("last_real_join").orElse(-1L).forGetter(State::lastRealJoinMillis),
                Codec.INT.fieldOf("full_moons").orElse(0).forGetter(State::fullMoonsSinceWorldCreate),
                Codec.LONG.fieldOf("world_created").orElse(-1L).forGetter(State::worldCreatedRealMillis),
                Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("cooldowns").orElse(Map.of()).forGetter(State::cooldownEnds),
                Codec.STRING.listOf(Codec.STRING).fieldOf("fired_ever").orElse(List.of()).xmap(HashSet::new, ArrayList::new).forGetter(State::firedEver),
                Codec.STRING.fieldOf("night_key").orElse("").forGetter(s -> s.nightKeyFired() == null ? "" : s.nightKeyFired()),
                Codec.STRING.listOf(Codec.STRING).fieldOf("night_fired").orElse(List.of()).xmap(HashSet::new, ArrayList::new).forGetter(State::nightFired),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("fear").orElse(Map.of()).forGetter(State::fearProfiles),
                Codec.unboundedMap(Codec.STRING, VEC).fieldOf("last_logout").orElse(Map.of()).forGetter(State::lastLogout),
                Codec.STRING.listOf(Codec.STRING).fieldOf("death_coords").orElse(List.of()).forGetter(State::deathCoords),
                BlockPos.CODEC.optionalFieldOf("buried").forGetter(State::buriedStructure),
                Codec.STRING.fieldOf("marked").orElse("").forGetter(State::markedPlayer),
                Codec.unboundedMap(Codec.STRING, VEC).fieldOf("bases").orElse(Map.of()).forGetter(State::bases)
        ).apply(i, State::new));
    }

    /** мост: SavedData хранит сериализуемый State и перекладывает его в runtime-поля */
    public static final class Wrapper extends SavedData {
        public State state = fresh();
        public static State fresh() {
            return new State(-1L, 0, -1L, Map.of(), Set.of(), "", Set.of(),
                    Map.of(), Map.of(), List.of(), Optional.empty(), "", Map.of());
        }
    }

    public static final SavedDataType<Wrapper> TYPE = new SavedDataType<>(
            Identifier.withDefaultNamespace("staticmod_state"),
            Wrapper::new,
            State.CODEC.xmap(s -> { Wrapper w = new Wrapper(); w.state = s; return w; }, w -> w.state),
            DataFixTypes.LEVEL);

    // ---- runtime-поля ----
    public final Map<String, Long> cooldownEnds = new HashMap<>();
    public final Set<String> firedEver = new HashSet<>();
    public final Set<String> nightFired = new HashSet<>();
    public String nightKeyFired = "";
    public final Map<UUID, FearProfile> fear = new HashMap<>();
    public final Map<UUID, double[]> lastLogoutPos = new HashMap<>();
    public final List<String> shownDeathCoords = new ArrayList<>();
    public String markedPlayer = "";
    public final Map<UUID, double[]> playerBase = new HashMap<>();
    public long lastRealJoinMillis = -1;
    public long worldCreatedRealMillis = -1;
    public int fullMoonsSinceWorldCreate = 0;
    public BlockPos buriedStructure = null;

    public FearProfile profile(UUID id) {
        return fear.computeIfAbsent(id, k -> new FearProfile());
    }

    public boolean isNightOver(long dayTime) { return dayTime > 23000 || dayTime < 12000; }

    private static List<Double> toList(double[] a) {
        List<Double> l = new ArrayList<>(a.length);
        for (double d : a) l.add(d);
        return l;
    }

    public State toState() {
        Map<String, String> fp = new HashMap<>();
        fear.forEach((k, v) -> fp.put(k.toString(), v.serialize()));
        Map<String, List<Double>> ll = new HashMap<>();
        lastLogoutPos.forEach((k, v) -> ll.put(k.toString(), toList(v)));
        Map<String, List<Double>> bs = new HashMap<>();
        playerBase.forEach((k, v) -> bs.put(k.toString(), toList(v)));
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
        s.lastLogout().forEach((k, v) -> { try { lastLogoutPos.put(UUID.fromString(k), v.stream().mapToDouble(Double::doubleValue).toArray()); } catch (Exception ignored) {} });
        shownDeathCoords.clear(); shownDeathCoords.addAll(s.deathCoords());
        buriedStructure = s.buriedStructure().orElse(null);
        markedPlayer = s.markedPlayer() == null ? "" : s.markedPlayer();
        playerBase.clear();
        s.bases().forEach((k, v) -> { try { playerBase.put(UUID.fromString(k), v.stream().mapToDouble(Double::doubleValue).toArray()); } catch (Exception ignored) {} });
    }
}
