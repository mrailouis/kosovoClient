package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.FishingLineHandler;
import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import net.minecraft.client.renderer.entity.RenderFish;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderFish.class, priority = 1001)
public abstract class MixinRenderFish {

    @Redirect(method = "doRender", at = @At(value = "NEW", target = "net/minecraft/util/Vec3", ordinal = 0))
    private Vec3 redirectFishingLineOffset(double x, double y, double z) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getFishingRod().isEnabled()) {
            return FishingLineHandler.getInstance().getOffset();
        }
        return new Vec3(x, y, z);
    }
}
