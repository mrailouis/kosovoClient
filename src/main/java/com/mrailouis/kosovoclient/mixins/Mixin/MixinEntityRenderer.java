package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.SneakHandler;
import com.mrailouis.kosovoclient.features.impl.visuals.Fullbright;
import com.mrailouis.kosovoclient.features.impl.visuals.Zoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @Unique
    private float partialTicks;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    public void capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.partialTicks = partialTicks;
    }

    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float modifyEyeHeight_orientCamera(Entity entity) {
        if (this.mc.getRenderViewEntity() != this.mc.thePlayer) {
            return entity.getEyeHeight();
        }
        return SneakHandler.getInstance().getEyeHeight(this.partialTicks);
    }

    @Redirect(method = "renderWorldDirections", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float modifyEyeHeight_renderWorldDirections(Entity entity) {
        if (this.mc.getRenderViewEntity() != this.mc.thePlayer) {
            return entity.getEyeHeight();
        }
        return SneakHandler.getInstance().getEyeHeight(this.partialTicks);
    }

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    private void modifyFovForZoom(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        float fov = cir.getReturnValue();
        cir.setReturnValue(Zoom.getInstance().getFov(fov, partialTicks));
    }

    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"))
    private boolean checkNightVisionActive(EntityPlayerSP player, Potion potion) {
        Fullbright fullbright = Fullbright.getInstance();
        if (fullbright.isEnabled() && fullbright.getMode().is("Night Vision") && potion == Potion.nightVision) {
            return true;
        }
        return player.isPotionActive(potion);
    }

    @Inject(method = "getNightVisionBrightness", at = @At("HEAD"), cancellable = true)
    private void overrideNightVisionBrightness(EntityLivingBase entitylivingbase_in, float partialTicks, CallbackInfoReturnable<Float> cir) {
        Fullbright fullbright = Fullbright.getInstance();
        if (fullbright.isEnabled() && fullbright.getMode().is("Night Vision")) {
            cir.setReturnValue(1.0f);
        }
    }
}
