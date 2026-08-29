package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@Getter
public class ItemPhysics extends Module {
    private static final ItemPhysics INSTANCE = new ItemPhysics();

    private final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", "Speed of item rotation in air.", 1.0, 0.1, 5.0, 0.1);
    private final Random random = new Random();

    public static ItemPhysics getInstance() {
        return INSTANCE;
    }

    private ItemPhysics() {
        super("Item Physics", "Realistic physics and laying effects for dropped items.", Category.VISUALS, true);
        registerSetting(this.rotationSpeed);
    }

    public void renderItemPhysics(EntityItem entity, double x, double y, double z, float partialTicks,
                                  RenderItem itemRenderer, RenderManager renderManager) {
        ItemStack stack = entity.getEntityItem();
        if (stack == null || stack.getItem() == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        this.random.setSeed(187L);

        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.pushMatrix();

        IBakedModel bakedModel = itemRenderer.getItemModelMesher().getItemModel(stack);
        int stackCount = getModelCount(stack);
        boolean is3D = bakedModel.isGui3d();

        Item item = stack.getItem();
        boolean isBlock = item instanceof ItemBlock && Block.getBlockFromItem(item) != null;

        double renderY = y;
        if (!entity.onGround) {
            renderY += 0.1D;
        }

        GlStateManager.translate((float) x, (float) renderY, (float) z);

        float rotSpeed = this.rotationSpeed.getValue().floatValue();
        float age = (entity.getAge() + partialTicks) * rotSpeed;

        if (entity.onGround) {
            if (is3D || isBlock) {
                GlStateManager.translate(0.0F, 0.22F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.translate(0.0F, 0.05F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }
        } else {
            double motion = Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
            float pitch = (float) (age * 18.0D);
            float yaw = entity.rotationYaw + (float) (age * 12.0D * (motion + 0.1D));

            if (is3D || isBlock) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
                GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
            } else {
                GlStateManager.translate(0.0F, 0.15F, 0.0F);
                GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < stackCount; ++i) {
            GlStateManager.pushMatrix();
            if (i > 0) {
                if (is3D || isBlock) {
                    float ox = (this.random.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    float oy = (this.random.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    float oz = (this.random.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    GlStateManager.translate(ox, oy, oz);
                } else {
                    float layerOffset = 0.02F * (float) i;
                    GlStateManager.translate(0.0F, 0.0F, layerOffset);
                }
            }

            if (is3D) {
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            IBakedModel transformedModel = ForgeHooksClient.handleCameraTransforms(bakedModel, ItemCameraTransforms.TransformType.GROUND);
            itemRenderer.renderItem(stack, transformedModel);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    private int getModelCount(ItemStack stack) {
        if (stack.stackSize > 48) return 5;
        if (stack.stackSize > 32) return 4;
        if (stack.stackSize > 16) return 3;
        if (stack.stackSize > 1) return 2;
        return 1;
    }
}
