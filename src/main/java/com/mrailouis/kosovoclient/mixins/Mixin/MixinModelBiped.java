package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped extends ModelBase {
    @ModifyConstant(method = "setRotationAngles", constant = @Constant(floatValue = -0.5235988F))
    private float cancelRotation(float original) {
        OldAnimations mod = OldAnimations.getInstance();
        return (mod.isEnabled() && mod.getThirdPersonBlock().isEnabled()) ? 0.0F : original;
    }
}
