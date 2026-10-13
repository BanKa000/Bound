package staticmod.core;

import java.util.ArrayList;
import java.util.List;

/** Реестр всех аномалий. Каждая — маленький класс в anomalies/, регистрируется здесь один раз. */
public final class AnomalyRegistry {
    public static final List<Anomaly> ALL = new ArrayList<>();

    private static void add(Anomaly a) { ALL.add(a); }

    public static int size() { return ALL.size(); }

    /** вызывается из StaticMod.onInitialize */
    public static void bootstrap() {
        // мир и мелкие аномалии
        add(new staticmod.anomalies.world.FlickerTorch());
        add(new staticmod.anomalies.world.ParadisePortal());
        add(new staticmod.anomalies.world.LeaflessChunks());
        add(new staticmod.anomalies.world.RedstoneTrail());
        add(new staticmod.anomalies.world.BedSignCoords());
        // звук
        add(new staticmod.anomalies.sound.StepsFromAbove());
        add(new staticmod.anomalies.sound.LeftEarDoor());
        add(new staticmod.anomalies.sound.ClockTick());
        // мобы
        add(new staticmod.anomalies.mobs.SelfShearedSheep());
        add(new staticmod.anomalies.mobs.DeadBucketFish());
        // инвентарь
        add(new staticmod.anomalies.inventory.BoneInOffhand());
        add(new staticmod.anomalies.inventory.StackSplit64());
        add(new staticmod.anomalies.inventory.TooltipSuffixIh());
    }
}
