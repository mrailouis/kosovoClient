package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase {
    @Inject(method = "shouldCombineTextures", at = @At("HEAD"), cancellable = true)
    private void applyRedArmor(CallbackInfoReturnable<Boolean> cir) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getRedArmour().isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
