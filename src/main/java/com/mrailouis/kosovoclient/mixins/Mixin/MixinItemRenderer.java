package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.AnimationHandler;
import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import com.mrailouis.kosovoclient.mixins.IMixin.IMixinItemFood;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    private ItemStack itemToRender;

    @Shadow
    private float equippedProgress;

    @Shadow
    private int equippedItemSlot;

    @Shadow
    protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);

    @Unique
    private float currentPartialTicks;

    @Inject(method = "doBowTransformations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V"))
    private void preBowScale(float partialTicks, AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getBowPosition().isEnabled()) {
            GlStateManager.rotate(-335.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, 0.5F, 0.0F);
        }
    }

    @Inject(method = "doBowTransformations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V", shift = At.Shift.AFTER))
    private void postBowScale(float partialTicks, AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getBowPosition().isEnabled()) {
            GlStateManager.translate(0.0F, -0.5F, 0.0F);
            GlStateManager.rotate(50.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(335.0F, 0.0F, 0.0F, 1.0F);
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    private void capturePartialTicks(float partialTicks, CallbackInfo ci) {
        this.currentPartialTicks = partialTicks;
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getItemInUseCount()I"))
    private int overrideItemInUseCount(AbstractClientPlayer player) {
        int useCount = player.getItemInUseCount();
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getPunchDuringUsage().isEnabled() && useCount <= 0 && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown()) {
            if (this.itemToRender != null) {
                EnumAction action = this.itemToRender.getItemUseAction();
                Item item = this.itemToRender.getItem();
                boolean block = action == EnumAction.BLOCK;
                boolean bow = action == EnumAction.BOW;
                boolean consume = false;
                if (item instanceof ItemFood) {
                    boolean alwaysEdible = ((IMixinItemFood) item).isAlwaysEdible();
                    if (player.canEat(alwaysEdible)) {
                        consume = action == EnumAction.EAT || action == EnumAction.DRINK;
                    }
                } else if (action == EnumAction.DRINK) {
                    consume = true;
                }
                if (block || consume || bow) {
                    return 1;
                }
            }
        }
        return useCount;
    }

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V"))
    private void redirectTransformFirstPersonItem(ItemRenderer instance, float equipProgress, float swingProgress) {
        if (this.itemToRender == null) {
            this.transformFirstPersonItem(equipProgress, swingProgress);
            return;
        }

        OldAnimations mod = OldAnimations.getInstance();
        EnumAction action = this.itemToRender.getItemUseAction();

        if (action == EnumAction.BLOCK) {
            if (mod.isEnabled() && mod.getBlockHit().isEnabled()) {
                swingProgress = AnimationHandler.getInstance().getSwingProgress(this.currentPartialTicks);
            }
        } else if (action == EnumAction.EAT || action == EnumAction.DRINK || action == EnumAction.BOW) {
            if (mod.isEnabled() && mod.getPunchDuringUsage().isEnabled()) {
                swingProgress = AnimationHandler.getInstance().getSwingProgress(this.currentPartialTicks);
            }
        }

        this.transformFirstPersonItem(equipProgress, swingProgress);
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V"), index = 2)
    private ItemCameraTransforms.TransformType renderItemTransform(ItemCameraTransforms.TransformType transform) {
        if (this.itemToRender == null) {
            return transform;
        }

        OldAnimations mod = OldAnimations.getInstance();
        if (this.itemToRender.getItem() instanceof ItemFishingRod && mod.isEnabled() && mod.getFishingRod().isEnabled()) {
            GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
        }

        if (AnimationHandler.getInstance().doFirstPersonTransform(this.itemToRender)) {
            return ItemCameraTransforms.TransformType.FIRST_PERSON;
        } else {
            return ItemCameraTransforms.TransformType.NONE;
        }
    }

    @ModifyArg(method = "updateEquippedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MathHelper;clamp_float(FFF)F"), index = 0)
    private float handleItemSwitch(float original) {
        EntityPlayer entityplayer = Minecraft.getMinecraft().thePlayer;
        if (entityplayer == null || entityplayer.inventory == null) {
            return original;
        }
        ItemStack itemstack = entityplayer.inventory.getCurrentItem();
        OldAnimations mod = OldAnimations.getInstance();
        if (mod.isEnabled() && mod.getItemSwitch().isEnabled() && this.equippedItemSlot == entityplayer.inventory.currentItem && ItemStack.areItemsEqual(this.itemToRender, itemstack)) {
            return 1.0f - this.equippedProgress;
        }
        return original;
    }
}
