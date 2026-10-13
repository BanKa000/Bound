package staticmod.entity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import staticmod.util.Geo;

/** Бежит на игрока, когда тот смотрит; в 4 блоках — слепота/тошнота/усталость и исчезает. */
public class StalkerEntity extends Mob implements Aura {
    private ServerPlayer target;
    private boolean frozen;

    public StalkerEntity(EntityType<? extends Mob> t, Level l) { super(t, l); }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.MAX_HEALTH, 30);
    }

    @Override public void tick() {
        super.tick();
        if (target == null || target.isRemoved())
            target = level().getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(48))
                    .stream().findFirst().orElse(null);
        if (target == null) return;
        boolean looks = Geo.playerLooksAt(target, position(), 90);
        tickBehavior(target.position(), looks);
        double dist = distanceTo(target);
        if (looks && dist < 16) getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.4);
        if (dist <= 4) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            if (Geo.RND.nextBoolean()) target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
            if (Geo.RND.nextBoolean()) target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN /*slowness*/, 200));
            discard(); // «сущность пропадает»
        }
    }

    @Override public void tickBehavior(Vec3 playerPos, boolean playerLooksAtMe) {
        frozen = !playerLooksAtMe;
        setNoAi(frozen); // без взгляда стоит
    }

    @Override public ServerPlayer target() { return target; }
    @Override public boolean isFrozen() { return frozen; }
}
