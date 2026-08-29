package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.mixins.IMixin.IMixinEntityFX;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Getter
public class Particles extends Module {
    private static final Particles INSTANCE = new Particles();

    private final ModeSetting hitParticleType = new ModeSetting("Hit Particle", "Particle type spawned on hit.", "Sharpness", new String[]{"Sharpness", "Critical", "Flame", "Heart", "Portal", "Redstone", "Smoke", "None"});
    private final NumberSetting multiplier = new NumberSetting("Multiplier", "Particle count multiplier.", 2.0, 1.0, 10.0, 1.0);
    private final BooleanSetting sharpnessParticles = new BooleanSetting("Always Sharpness", "Always spawn sharpness particles on hit.", true);
    private final BooleanSetting criticalParticles = new BooleanSetting("Always Criticals", "Always spawn critical particles on hit.", false);
    private final NumberSetting particleSize = new NumberSetting("Particle Size", "Scale of spawned particles.", 1.0, 0.2, 3.0, 0.1);

    private final Random random = new Random();

    public static Particles getInstance() {
        return INSTANCE;
    }

    private Particles() {
        super("Particles", "Customizes hit particles, multipliers, and particle scale.", Category.VISUALS, true);
        registerSetting(this.hitParticleType);
        registerSetting(this.multiplier);
        registerSetting(this.sharpnessParticles);
        registerSetting(this.criticalParticles);
        registerSetting(this.particleSize);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (event.entityPlayer != mc.thePlayer) {
            return;
        }

        Entity target = event.target;
        if (target == null) {
            return;
        }

        spawnHitParticles(target);
    }

    public void spawnHitParticles(Entity target) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.effectRenderer == null) {
            return;
        }

        int count = this.multiplier.getValue().intValue();
        float size = this.particleSize.getValue().floatValue();

        if (this.sharpnessParticles.isEnabled()) {
            spawnParticles(target, EnumParticleTypes.CRIT_MAGIC, count, size);
        }

        if (this.criticalParticles.isEnabled()) {
            spawnParticles(target, EnumParticleTypes.CRIT, count, size);
        }

        EnumParticleTypes selectedType = getSelectedParticleType();
        if (selectedType != null && !this.hitParticleType.is("None")) {
            if (selectedType != EnumParticleTypes.CRIT_MAGIC && selectedType != EnumParticleTypes.CRIT) {
                spawnParticles(target, selectedType, count, size);
            } else if (selectedType == EnumParticleTypes.CRIT_MAGIC && !this.sharpnessParticles.isEnabled()) {
                spawnParticles(target, EnumParticleTypes.CRIT_MAGIC, count, size);
            } else if (selectedType == EnumParticleTypes.CRIT && !this.criticalParticles.isEnabled()) {
                spawnParticles(target, EnumParticleTypes.CRIT, count, size);
            }
        }
    }

    private void spawnParticles(Entity target, EnumParticleTypes type, int count, float size) {
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < count; i++) {
            double ox = (this.random.nextDouble() - 0.5D) * target.width;
            double oy = this.random.nextDouble() * target.height;
            double oz = (this.random.nextDouble() - 0.5D) * target.width;

            double sx = (this.random.nextDouble() - 0.5D) * 0.4D;
            double sy = (this.random.nextDouble() - 0.5D) * 0.4D;
            double sz = (this.random.nextDouble() - 0.5D) * 0.4D;

            EntityFX fx = mc.effectRenderer.spawnEffectParticle(
                    type.getParticleID(),
                    target.posX + ox,
                    target.posY + oy,
                    target.posZ + oz,
                    sx, sy, sz
            );

            if (fx != null && Math.abs(size - 1.0f) > 0.01f && fx instanceof IMixinEntityFX) {
                IMixinEntityFX mixinFx = (IMixinEntityFX) fx;
                mixinFx.setParticleScale(mixinFx.getParticleScale() * size);
            }
        }
    }

    private EnumParticleTypes getSelectedParticleType() {
        String val = this.hitParticleType.getValue();
        if ("Sharpness".equalsIgnoreCase(val)) return EnumParticleTypes.CRIT_MAGIC;
        if ("Critical".equalsIgnoreCase(val)) return EnumParticleTypes.CRIT;
        if ("Flame".equalsIgnoreCase(val)) return EnumParticleTypes.FLAME;
        if ("Heart".equalsIgnoreCase(val)) return EnumParticleTypes.HEART;
        if ("Portal".equalsIgnoreCase(val)) return EnumParticleTypes.PORTAL;
        if ("Redstone".equalsIgnoreCase(val)) return EnumParticleTypes.REDSTONE;
        if ("Smoke".equalsIgnoreCase(val)) return EnumParticleTypes.SMOKE_NORMAL;
        return null;
    }

    public void handleVanillaEmitter(Entity target, EnumParticleTypes type) {
        if (!isEnabled()) {
            return;
        }

        int multiplierVal = this.multiplier.getValue().intValue();
        int count = multiplierVal - 1;
        float size = this.particleSize.getValue().floatValue();

        if (count > 0) {
            spawnParticles(target, type, count, size);
        }
    }
}
