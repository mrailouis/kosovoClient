package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.visuals.ItemPhysics;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem extends Render<EntityItem> {

    @Shadow
    @Final
    private RenderItem itemRenderer;

    protected MixinRenderEntityItem(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityItem;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void renderItemWithPhysics(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        ItemPhysics physics = ItemPhysics.getInstance();
        if (physics.isEnabled()) {
            ci.cancel();
            physics.renderItemPhysics(entity, x, y, z, partialTicks, this.itemRenderer, this.renderManager);
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }
}
