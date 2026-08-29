package com.mrailouis.kosovoclient.features.impl.animations;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AnimationHandler {
    @Getter
    private static final AnimationHandler instance = new AnimationHandler();

    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private float prevSwingProgress;
    @Getter
    private float swingProgress;
    private int swingProgressInt;
    private boolean isSwingInProgress;

    public float getSwingProgress(float partialTickTime) {
        float currentProgress = this.swingProgress - this.prevSwingProgress;

        if (!this.isSwingInProgress) {
            return this.mc.thePlayer != null ? this.mc.thePlayer.getSwingProgress(partialTickTime) : 0.0f;
        }

        if (currentProgress < 0.0F) {
            ++currentProgress;
        }

        return this.prevSwingProgress + currentProgress * partialTickTime;
    }

    private int getArmSwingAnimationEnd(EntityPlayerSP player) {
        if (player.isPotionActive(Potion.digSpeed)) {
            return 5 - player.getActivePotionEffect(Potion.digSpeed).getAmplifier();
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            return 8 + player.getActivePotionEffect(Potion.digSlowdown).getAmplifier() * 2;
        }
        return 6;
    }

    private void updateSwingProgress() {
        final EntityPlayerSP player = this.mc.thePlayer;
        if (player == null) {
            return;
        }

        this.prevSwingProgress = this.swingProgress;
        int max = getArmSwingAnimationEnd(player);

        OldAnimations mod = OldAnimations.getInstance();
        boolean punching = mod.isEnabled() && mod.getPunchDuringUsage().isEnabled();

        if (punching && this.mc.gameSettings.keyBindAttack.isKeyDown() &&
                this.mc.objectMouseOver != null &&
                this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (!this.isSwingInProgress || this.swingProgressInt >= max >> 1 || this.swingProgressInt < 0) {
                this.isSwingInProgress = true;
                this.swingProgressInt = -1;
            }
        }

        if (this.isSwingInProgress) {
            ++this.swingProgressInt;
            if (this.swingProgressInt >= max) {
                this.swingProgressInt = 0;
                this.isSwingInProgress = false;
            }
        } else {
            this.swingProgressInt = 0;
        }

        this.swingProgress = (float) this.swingProgressInt / (float) max;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateSwingProgress();
        }
    }

    public void doSwordBlock3rdPersonTransform() {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getThirdPersonBlock().isEnabled()) {
            GlStateManager.translate(-0.15f, -0.2f, 0.0f);
            GlStateManager.rotate(70.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.translate(0.119f, 0.2f, -0.024f);
        }
    }

    public boolean doFirstPersonTransform(ItemStack stack) {
        if (stack == null) {
            return true;
        }

        OldAnimations mod = OldAnimations.getInstance();
        if (!mod.isEnabled()) {
            return true;
        }

        switch (stack.getItemUseAction()) {
            case BOW:
                if (!mod.getBowPosition().isEnabled()) {
                    return true;
                }
                break;
            case EAT:
            case DRINK:
                if (!mod.getEatingDrinking().isEnabled()) {
                    return true;
                }
                break;
            case BLOCK:
                if (!mod.getSwordPosition().isEnabled()) {
                    return true;
                }
                break;
            case NONE:
                if (!(stack.getItem() instanceof ItemFishingRod && mod.getFishingRod().isEnabled())) {
                    return true;
                }
                break;
            default:
                return true;
        }

        GlStateManager.translate(0.58800083f, 0.36999986f, -0.77000016f);
        GlStateManager.translate(0.0f, -0.3f, 0.0f);
        GlStateManager.scale(1.5f, 1.5f, 1.5f);
        GlStateManager.rotate(50.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(335.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.translate(-0.9375f, -0.0625f, 0.0f);
        GlStateManager.scale(-2.0f, 2.0f, -2.0f);

        if (this.mc.getRenderItem().shouldRenderItemIn3D(stack)) {
            GlStateManager.scale(0.58823526f, 0.58823526f, 0.58823526f);
            GlStateManager.rotate(-25.0f, 0.0f, 0.0f, 1.0f);
            GlStateManager.rotate(0.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
            GlStateManager.translate(0.0f, -0.25f, -0.125f);
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            return true;
        }

        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        return false;
    }
}
