package com.mrailouis.kosovoclient.mixins.IMixin;

import net.minecraft.client.particle.EntityFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFX.class)
public interface IMixinEntityFX {
    @Accessor("particleScale")
    float getParticleScale();

    @Accessor("particleScale")
    void setParticleScale(float scale);
}
