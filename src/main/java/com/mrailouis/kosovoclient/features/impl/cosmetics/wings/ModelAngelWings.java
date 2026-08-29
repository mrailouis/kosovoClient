package com.mrailouis.kosovoclient.features.impl.cosmetics.wings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

public class ModelAngelWings extends ModelBase {
    private final ModelRenderer leftWing;
    private final ModelRenderer rightWing;

    public ModelAngelWings() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.leftWing = new ModelRenderer(this);
        this.leftWing.setRotationPoint(1.0F, 0.0F, 2.0F);

        this.leftWing.setTextureOffset(0, 0).addBox(0.0F, -2.0F, 0.0F, 10, 3, 1);
        this.leftWing.setTextureOffset(0, 6).addBox(8.0F, -4.0F, 0.0F, 10, 4, 1);
        this.leftWing.setTextureOffset(0, 14).addBox(16.0F, -6.0F, 0.0F, 8, 5, 1);

        this.leftWing.setTextureOffset(24, 0).addBox(2.0F, 1.0F, 0.0F, 8, 8, 1);
        this.leftWing.setTextureOffset(24, 10).addBox(8.0F, 0.0F, 0.0F, 10, 12, 1);
        this.leftWing.setTextureOffset(24, 24).addBox(16.0F, -1.0F, 0.0F, 8, 16, 1);
        this.leftWing.setTextureOffset(44, 0).addBox(22.0F, -3.0F, 0.0F, 4, 14, 1);

        this.rightWing = new ModelRenderer(this);
        this.rightWing.mirror = true;
        this.rightWing.setRotationPoint(-1.0F, 0.0F, 2.0F);

        this.rightWing.setTextureOffset(0, 0).addBox(-10.0F, -2.0F, 0.0F, 10, 3, 1);
        this.rightWing.setTextureOffset(0, 6).addBox(-18.0F, -4.0F, 0.0F, 10, 4, 1);
        this.rightWing.setTextureOffset(0, 14).addBox(-24.0F, -6.0F, 0.0F, 8, 5, 1);

        this.rightWing.setTextureOffset(24, 0).addBox(-10.0F, 1.0F, 0.0F, 8, 8, 1);
        this.rightWing.setTextureOffset(24, 10).addBox(-18.0F, 0.0F, 0.0F, 10, 12, 1);
        this.rightWing.setTextureOffset(24, 24).addBox(-24.0F, -1.0F, 0.0F, 8, 16, 1);
        this.rightWing.setTextureOffset(44, 0).addBox(-26.0F, -3.0F, 0.0F, 4, 14, 1);
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

        float angleY = 0.30f + flap * 0.40f;
        float angleZ = 0.12f + flap * 0.18f;
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
