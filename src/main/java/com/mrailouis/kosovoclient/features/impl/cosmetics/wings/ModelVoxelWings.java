package com.mrailouis.kosovoclient.features.impl.cosmetics.wings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

public class ModelVoxelWings extends ModelBase {
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;

    public ModelVoxelWings() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.leftWing = new ModelRenderer(this);
        this.leftWing.setRotationPoint(1.5F, 0.0F, 2.0F);

        this.leftWing.setTextureOffset(0, 0).addBox(0.0F, -2.0F, 0.0F, 8, 4, 2);
        this.leftWing.setTextureOffset(0, 8).addBox(8.0F, -1.0F, 0.0F, 8, 5, 2);
        this.leftWing.setTextureOffset(0, 16).addBox(16.0F, 1.0F, 0.0F, 8, 6, 2);
        this.leftWing.setTextureOffset(0, 24).addBox(24.0F, 4.0F, 0.0F, 6, 7, 2);

        this.leftWing.setTextureOffset(24, 0).addBox(2.0F, 2.0F, 0.5F, 6, 8, 1);
        this.leftWing.setTextureOffset(24, 10).addBox(8.0F, 4.0F, 0.5F, 8, 10, 1);
        this.leftWing.setTextureOffset(24, 22).addBox(16.0F, 7.0F, 0.5F, 8, 11, 1);
        this.leftWing.setTextureOffset(24, 34).addBox(24.0F, 11.0F, 0.5F, 5, 8, 1);

        this.rightWing = new ModelRenderer(this);
        this.rightWing.mirror = true;
        this.rightWing.setRotationPoint(-1.5F, 0.0F, 2.0F);

        this.rightWing.setTextureOffset(0, 0).addBox(-8.0F, -2.0F, 0.0F, 8, 4, 2);
        this.rightWing.setTextureOffset(0, 8).addBox(-16.0F, -1.0F, 0.0F, 8, 5, 2);
        this.rightWing.setTextureOffset(0, 16).addBox(-24.0F, 1.0F, 0.0F, 8, 6, 2);
        this.rightWing.setTextureOffset(0, 24).addBox(-30.0F, 4.0F, 0.0F, 6, 7, 2);

        this.rightWing.setTextureOffset(24, 0).addBox(-8.0F, 2.0F, 0.5F, 6, 8, 1);
        this.rightWing.setTextureOffset(24, 10).addBox(-16.0F, 4.0F, 0.5F, 8, 10, 1);
        this.rightWing.setTextureOffset(24, 22).addBox(-24.0F, 7.0F, 0.5F, 8, 11, 1);
        this.rightWing.setTextureOffset(24, 34).addBox(-29.0F, 11.0F, 0.5F, 5, 8, 1);
    }

    public void renderWings(EntityPlayer player, float partialTicks, float scale, float flapSpeed, boolean flapMovingOnly) {
        float speed = 0.0f;
        if (player != null) {
            double dx = player.posX - player.prevPosX;
            double dz = player.posZ - player.prevPosZ;
            speed = (float) Math.sqrt(dx * dx + dz * dz);
        }

        boolean moving = speed > 0.02f || (player != null && (player.isAirBorne || !player.onGround));
        long time = System.currentTimeMillis();
        float animSpeed = (flapMovingOnly && !moving) ? 0.05f : (0.45f + speed * 1.8f) * flapSpeed;
        float cycle = (time % 100000L) / 1000.0f * animSpeed * 8.0f;

        float flap = (float) Math.sin(cycle);

        float angleY = 0.35f + flap * 0.45f;
        float angleZ = 0.10f + flap * 0.20f;
        float angleX = (player != null && player.isSneaking() ? 0.35f : 0.0f);

        this.leftWing.rotateAngleX = angleX;
        this.leftWing.rotateAngleY = -angleY;
        this.leftWing.rotateAngleZ = angleZ;

        this.rightWing.rotateAngleX = angleX;
        this.rightWing.rotateAngleY = angleY;
        this.rightWing.rotateAngleZ = -angleZ;

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale * 0.035f, scale * 0.035f, scale * 0.035f);
        GlStateManager.translate(0.0F, 3.0F, 2.0F);

        GlStateManager.enableRescaleNormal();
        this.leftWing.render(1.0F);
        this.rightWing.render(1.0F);
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }
}
