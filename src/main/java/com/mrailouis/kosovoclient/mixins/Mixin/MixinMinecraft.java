package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import com.mrailouis.kosovoclient.features.impl.visuals.Zoom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    public void rightClickMouse(CallbackInfo ci) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getPunchDuringUsage().isEnabled() &&
                Minecraft.getMinecraft().playerController != null &&
                Minecraft.getMinecraft().playerController.getIsHittingBlock() &&
                Minecraft.getMinecraft().thePlayer != null &&
                Minecraft.getMinecraft().thePlayer.getHeldItem() != null &&
                (Minecraft.getMinecraft().thePlayer.getHeldItem().getItemUseAction() != EnumAction.NONE ||
                        Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemBlock)) {
            Minecraft.getMinecraft().playerController.resetBlockRemoving();
        }
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"))
    private boolean allowPunchDuringUsage(EntityPlayerSP player) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getPunchDuringUsage().isEnabled()) {
            return false;
        }
        return player.isUsingItem();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;changeCurrentItem(I)V"), cancellable = true)
    private void cancelHotbarScrollDuringZoom(CallbackInfo ci) {
        Zoom zoom = Zoom.getInstance();
        if (zoom.isEnabled() && zoom.isZooming()) {
            ci.cancel();
        }
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I", remap = false))
    private void handleZoomMouseScroll(CallbackInfo ci) {
        int dWheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (dWheel != 0) {
            Zoom.getInstance().handleMouseScroll(dWheel);
        }
    }
}
