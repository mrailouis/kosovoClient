package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.cosmetics.capes.LayerCustomCape;
import com.mrailouis.kosovoclient.features.impl.cosmetics.wings.LayerWings;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer extends RendererLivingEntity<AbstractClientPlayer> {

    public MixinRenderPlayer(RenderManager renderManagerIn, ModelPlayer modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn, modelBaseIn, shadowSizeIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V", at = @At("RETURN"))
    private void addCosmeticsLayers(RenderManager renderManager, boolean useSmallArms, CallbackInfo ci) {
        RenderPlayer renderer = (RenderPlayer) (Object) this;
        this.addLayer(new LayerCustomCape(renderer));
        this.addLayer(new LayerWings(renderer));
    }
}
