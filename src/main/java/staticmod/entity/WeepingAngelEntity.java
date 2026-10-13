package staticmod.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import staticmod.util.Geo;

/** Движется только вне поля зрения, замирает при взгляде. */
public class WeepingAngelEntity extends Mob implements Aura {
    private ServerPlayer target;
    private boolean frozen;

    public WeepingAngelEntity(EntityType<? extends Mob> t, Level l) { super(t, l); }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.MAX_HEALTH, 40);
    }

    @Override public void tick() {
        super.tick();
        if (target == null || target.isRemoved())
            target = level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64))
                    .stream().findFirst().orElse(null);
        if (target == null) return;
        boolean looks = Geo.playerLooksAt(target, position(), 100);
        tickBehavior(target.position(), looks);
    }

    @Override public void tickBehavior(Vec3 playerPos, boolean playerLooksAtMe) {
        frozen = playerLooksAtMe;
        setNoAi(frozen); // «замирает при взгляде»
        if (!frozen) {
            getNavigation().moveTo(playerPos.x, playerPos.y, playerPos.z, 1.1);
        }
    }

    @Override public ServerPlayer target() { return target; }
    @Override public boolean isFrozen() { return frozen; }
}
