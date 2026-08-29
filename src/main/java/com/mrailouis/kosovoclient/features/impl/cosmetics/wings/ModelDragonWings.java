package com.mrailouis.kosovoclient.features.impl.cosmetics.wings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class ModelDragonWings extends ModelBase {
    private final ModelRenderer wingLeft;
    private final ModelRenderer wingLeftTip;
    private final ModelRenderer wingRight;
    private final ModelRenderer wingRightTip;

    public ModelDragonWings() {
        this.textureWidth = 256;
        this.textureHeight = 256;

        this.wingLeft = new ModelRenderer(this, 112, 88);
        this.wingLeft.setRotationPoint(1.0F, 1.0F, 2.0F);
        this.wingLeft.addBox(0.0F, -2.0F, -1.0F, 18, 4, 2);

        this.wingLeftTip = new ModelRenderer(this, 112, 136);
        this.wingLeftTip.setRotationPoint(18.0F, 0.0F, 0.0F);
        this.wingLeftTip.addBox(0.0F, -1.5F, -1.0F, 16, 3, 2);
        this.wingLeft.addChild(this.wingLeftTip);

        this.wingRight = new ModelRenderer(this, 112, 88);
        this.wingRight.mirror = true;
        this.wingRight.setRotationPoint(-1.0F, 1.0F, 2.0F);
        this.wingRight.addBox(-18.0F, -2.0F, -1.0F, 18, 4, 2);

        this.wingRightTip = new ModelRenderer(this, 112, 136);
        this.wingRightTip.mirror = true;
        this.wingRightTip.setRotationPoint(-18.0F, 0.0F, 0.0F);
        this.wingRightTip.addBox(-16.0F, -1.5F, -1.0F, 16, 3, 2);
        this.wingRight.addChild(this.wingRightTip);
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
        float cycle = (time % 100000L) / 1000.0f * animSpeed * 10.0f;

        float flap = (float) Math.sin(cycle);
        float flapTip = (float) Math.sin(cycle - 0.6f);

        float angleY = 0.25f + flap * 0.40f;
        float angleZ = 0.15f + flap * 0.25f;
        float angleX = 0.10f + (player != null && player.isSneaking() ? 0.35f : 0.0f);

        if (player != null && (player.isAirBorne || !player.onGround)) {
            angleY += 0.25f;
            angleZ += 0.20f;
        }

        this.wingLeft.rotateAngleX = angleX;
        this.wingLeft.rotateAngleY = -angleY;
        this.wingLeft.rotateAngleZ = angleZ;
        this.wingLeftTip.rotateAngleY = -Math.abs(flapTip) * 0.50f;
        this.wingLeftTip.rotateAngleZ = -Math.abs(flapTip) * 0.20f;

        this.wingRight.rotateAngleX = angleX;
        this.wingRight.rotateAngleY = angleY;
        this.wingRight.rotateAngleZ = -angleZ;
        this.wingRightTip.rotateAngleY = Math.abs(flapTip) * 0.50f;
        this.wingRightTip.rotateAngleZ = Math.abs(flapTip) * 0.20f;

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale * 0.035f, scale * 0.035f, scale * 0.035f);
        GlStateManager.translate(0.0F, 3.0F, 2.0F);

        GlStateManager.enableCull();
        this.wingLeft.render(1.0F);
        this.wingRight.render(1.0F);

        renderMembrane(this.wingLeft, this.wingLeftTip, false, scale);
        renderMembrane(this.wingRight, this.wingRightTip, true, scale);

        GlStateManager.popMatrix();
    }

    private void renderMembrane(ModelRenderer base, ModelRenderer tip, boolean mirror, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glTexCoord2f(mirror ? 0.0F : 1.0F, 0.0F);
        GL11.glVertex3f(0.0F, 0.0F, 0.0F);

        float side = mirror ? -1.0F : 1.0F;
        float bx = side * 18.0F;
        float tx = side * 34.0F;

        GL11.glTexCoord2f(0.5F, 0.0F);
        GL11.glVertex3f(bx, -2.0F, 0.0F);

        GL11.glTexCoord2f(mirror ? 1.0F : 0.0F, 0.5F);
        GL11.glVertex3f(tx, 4.0F, 0.0F);

        GL11.glTexCoord2f(mirror ? 0.7F : 0.3F, 1.0F);
        GL11.glVertex3f(tx * 0.75F, 16.0F, 0.0F);

        GL11.glTexCoord2f(mirror ? 0.3F : 0.7F, 1.0F);
        GL11.glVertex3f(bx * 0.5F, 12.0F, 0.0F);

        GL11.glTexCoord2f(mirror ? 0.0F : 1.0F, 1.0F);
        GL11.glVertex3f(0.0F, 8.0F, 0.0F);

        GL11.glEnd();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
