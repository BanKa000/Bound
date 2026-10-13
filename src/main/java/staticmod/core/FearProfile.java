package staticmod.core;

/**
 * Профиль страха игрока: мод считает оглядывания, стены, факелы,
 * побеги в шахту. Влияет на подбор событий («режиссёр»).
 */
public class FearProfile {
    public int lookAround;      // резкие движения мышью (оглядывания)
    public int wallsBuilt;      // поставленных стеновых блоков
    public int torchesPlaced;   // поставленных факелов
    public int mineDescents;    // спусков глубоко в шахту (Y < 0)
    public int sprintsAway;     // убеганий от сущности (спринт + сущность близко)
    public int screenshots;     // карма скриншотов
    public float fear;          // 0..1 сводная метрика

    public void tickDecay() {
        fear = Math.max(0f, fear - 0.0002f);
    }

    public void addLook() { if (++lookAround % 3 == 0) fear = Math.min(1f, fear + 0.01f); }
    public void addWall() { wallsBuilt++; fear = Math.min(1f, fear + 0.004f); }
    public void addTorch() { torchesPlaced++; }
    public void addMine() { mineDescents++; fear = Math.min(1f, fear + 0.02f); }
    public void addSprint() { sprintsAway++; fear = Math.min(1f, fear + 0.03f); }
    public void addShot() { screenshots++; }

    /** обнёсся стеной → контрмера: следующий стук будет изнутри периметра */
    public boolean copingWalls() { return wallsBuilt > 30; }

    public String serialize() {
        return lookAround + "," + wallsBuilt + "," + torchesPlaced + "," + mineDescents
                + "," + sprintsAway + "," + screenshots + "," + fear;
    }

    public static FearProfile deserialize(String s) {
        FearProfile p = new FearProfile();
        if (s == null || s.isEmpty()) return p;
        String[] a = s.split(",");
        try {
            p.lookAround = Integer.parseInt(a[0]); p.wallsBuilt = Integer.parseInt(a[1]);
            p.torchesPlaced = Integer.parseInt(a[2]); p.mineDescents = Integer.parseInt(a[3]);
            p.sprintsAway = Integer.parseInt(a[4]); p.screenshots = Integer.parseInt(a[5]);
            p.fear = Float.parseFloat(a[6]);
        } catch (Exception ignored) {}
        return p;
    }
}
