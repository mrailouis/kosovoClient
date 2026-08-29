package com.mrailouis.kosovoclient.features.impl.cosmetics.capes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CapeRenderer {
    private static final int SEGMENTS = 16;
    private static final float CAPE_WIDTH = 0.625f;
    private static final float CAPE_HEIGHT = 1.0f;
    private static final float CAPE_DEPTH = 0.0625f;

    public static void render(AbstractClientPlayer player, CapeTexture capeTexture, String style, float waveSpeed, float waveAmount, boolean idleFlow, boolean physics, float partialTicks) {
        if (player == null || capeTexture == null) {
            return;
        }

        ResourceLocation loc = capeTexture.getActiveLocation();
        if (loc == null) {
            return;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);

        double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * (double) partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks);
        double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * (double) partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks);
        double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * (double) partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks);
        float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = MathHelper.sin(f * (float) Math.PI / 180.0F);
        double d4 = -MathHelper.cos(f * (float) Math.PI / 180.0F);
        float f1 = (float) d1 * 10.0F;
        f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
        if (f2 < 0.0F) {
            f2 = 0.0F;
        }
        float f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        f1 = f1 + MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

        if (player.isSneaking()) {
            f1 += 25.0F;
        }

        boolean isFlowing = "Flowing".equalsIgnoreCase(style) || "Wave".equalsIgnoreCase(style);
        float[] uv = capeTexture.getUVBounds();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 0.125F);
        GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        if (isFlowing) {
            renderFlowingCape(player, uv, waveSpeed, waveAmount, idleFlow, physics, partialTicks, f2, f3);
        } else {
            renderFlatCape(uv);
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private static void renderFlatCape(float[] uv) {
        float u0 = uv[0];
        float u1 = uv[1];
        float u2 = uv[2];
        float u3 = uv[3];
        float u4 = uv[4];
        float v0 = uv[5];
        float v1 = uv[6];
        float v2 = uv[7];

        float w = CAPE_WIDTH;
        float h = CAPE_HEIGHT;
        float d = CAPE_DEPTH;

        float halfW = w / 2.0f;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        wr.pos(-halfW, 0.0, 0.0).tex(u1, v1).normal(0.0F, 0.0F, -1.0F).endVertex();
        wr.pos(halfW, 0.0, 0.0).tex(u2, v1).normal(0.0F, 0.0F, -1.0F).endVertex();
        wr.pos(halfW, h, 0.0).tex(u2, v2).normal(0.0F, 0.0F, -1.0F).endVertex();
        wr.pos(-halfW, h, 0.0).tex(u1, v2).normal(0.0F, 0.0F, -1.0F).endVertex();

        wr.pos(halfW, 0.0, d).tex(u3, v1).normal(0.0F, 0.0F, 1.0F).endVertex();
        wr.pos(-halfW, 0.0, d).tex(u4, v1).normal(0.0F, 0.0F, 1.0F).endVertex();
        wr.pos(-halfW, h, d).tex(u4, v2).normal(0.0F, 0.0F, 1.0F).endVertex();
        wr.pos(halfW, h, d).tex(u3, v2).normal(0.0F, 0.0F, 1.0F).endVertex();

        wr.pos(-halfW, 0.0, d).tex(u0, v1).normal(-1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(-halfW, 0.0, 0.0).tex(u1, v1).normal(-1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(-halfW, h, 0.0).tex(u1, v2).normal(-1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(-halfW, h, d).tex(u0, v2).normal(-1.0F, 0.0F, 0.0F).endVertex();

        wr.pos(halfW, 0.0, 0.0).tex(u2, v1).normal(1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(halfW, 0.0, d).tex(u3, v1).normal(1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(halfW, h, d).tex(u3, v2).normal(1.0F, 0.0F, 0.0F).endVertex();
        wr.pos(halfW, h, 0.0).tex(u2, v2).normal(1.0F, 0.0F, 0.0F).endVertex();

        wr.pos(-halfW, 0.0, d).tex(u1, v0).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(halfW, 0.0, d).tex(u2, v0).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(halfW, 0.0, 0.0).tex(u2, v1).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(-halfW, 0.0, 0.0).tex(u1, v1).normal(0.0F, -1.0F, 0.0F).endVertex();

        wr.pos(-halfW, h, 0.0).tex(u2, v0).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(halfW, h, 0.0).tex(u3, v0).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(halfW, h, d).tex(u3, v1).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(-halfW, h, d).tex(u2, v1).normal(0.0F, 1.0F, 0.0F).endVertex();

        tess.draw();
    }

    private static void renderFlowingCape(AbstractClientPlayer player, float[] uv, float waveSpeed, float waveAmount, boolean idleFlow, boolean physics, float partialTicks, float forwardSpeed, float sideSpeed) {
        float u0 = uv[0];
        float u1 = uv[1];
        float u2 = uv[2];
        float u3 = uv[3];
        float u4 = uv[4];
        float v0 = uv[5];
        float v1 = uv[6];
        float v2 = uv[7];

        float w = CAPE_WIDTH;
        float halfW = w / 2.0f;
        float d = CAPE_DEPTH;

        long time = System.currentTimeMillis();
        float timeFactor = (time % 100000L) / 1000.0f * waveSpeed * 4.0f;

        float[] segY = new float[SEGMENTS + 1];
        float[] segZ = new float[SEGMENTS + 1];
        float[] segX = new float[SEGMENTS + 1];

        float totalHeight = CAPE_HEIGHT;
        float segHeight = totalHeight / SEGMENTS;

        float moveIntensity = Math.min(1.5f, (forwardSpeed + Math.abs(sideSpeed)) / 40.0f);
        float idleIntensity = idleFlow ? 0.35f : 0.05f;
        float currentIntensity = Math.max(idleIntensity, moveIntensity) * waveAmount;

        for (int i = 0; i <= SEGMENTS; i++) {
            float progress = (float) i / SEGMENTS;
            segY[i] = i * segHeight;

            float wave = (float) Math.sin(timeFactor - i * 0.45f);
            float curve = (float) Math.pow(progress, 1.3);

            float zOffset = wave * 0.06f * currentIntensity * curve;
            if (physics) {
                zOffset += curve * (forwardSpeed / 120.0f) * 0.25f;
            }
            segZ[i] = zOffset;

            float xOffset = (float) Math.cos(timeFactor * 0.5f - i * 0.3f) * 0.02f * currentIntensity * curve;
            if (physics) {
                xOffset += curve * (sideSpeed / 100.0f) * 0.15f;
            }
            segX[i] = xOffset;
        }

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int i = 0; i < SEGMENTS; i++) {
            float vTop = v1 + (v2 - v1) * ((float) i / SEGMENTS);
            float vBot = v1 + (v2 - v1) * ((float) (i + 1) / SEGMENTS);

            float y0 = segY[i];
            float y1 = segY[i + 1];
            float z0 = segZ[i];
            float z1 = segZ[i + 1];
            float x0 = segX[i];
            float x1 = segX[i + 1];

            wr.pos(-halfW + x0, y0, z0).tex(u1, vTop).normal(0.0F, 0.0F, -1.0F).endVertex();
            wr.pos(halfW + x0, y0, z0).tex(u2, vTop).normal(0.0F, 0.0F, -1.0F).endVertex();
            wr.pos(halfW + x1, y1, z1).tex(u2, vBot).normal(0.0F, 0.0F, -1.0F).endVertex();
            wr.pos(-halfW + x1, y1, z1).tex(u1, vBot).normal(0.0F, 0.0F, -1.0F).endVertex();

            wr.pos(halfW + x0, y0, z0 + d).tex(u3, vTop).normal(0.0F, 0.0F, 1.0F).endVertex();
            wr.pos(-halfW + x0, y0, z0 + d).tex(u4, vTop).normal(0.0F, 0.0F, 1.0F).endVertex();
            wr.pos(-halfW + x1, y1, z1 + d).tex(u4, vBot).normal(0.0F, 0.0F, 1.0F).endVertex();
            wr.pos(halfW + x1, y1, z1 + d).tex(u3, vBot).normal(0.0F, 0.0F, 1.0F).endVertex();

            wr.pos(-halfW + x0, y0, z0 + d).tex(u0, vTop).normal(-1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(-halfW + x0, y0, z0).tex(u1, vTop).normal(-1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(-halfW + x1, y1, z1).tex(u1, vBot).normal(-1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(-halfW + x1, y1, z1 + d).tex(u0, vBot).normal(-1.0F, 0.0F, 0.0F).endVertex();

            wr.pos(halfW + x0, y0, z0).tex(u2, vTop).normal(1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(halfW + x0, y0, z0 + d).tex(u3, vTop).normal(1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(halfW + x1, y1, z1 + d).tex(u3, vBot).normal(1.0F, 0.0F, 0.0F).endVertex();
            wr.pos(halfW + x1, y1, z1).tex(u2, vBot).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        wr.pos(-halfW + segX[0], segY[0], segZ[0] + d).tex(u1, v0).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(halfW + segX[0], segY[0], segZ[0] + d).tex(u2, v0).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(halfW + segX[0], segY[0], segZ[0]).tex(u2, v1).normal(0.0F, -1.0F, 0.0F).endVertex();
        wr.pos(-halfW + segX[0], segY[0], segZ[0]).tex(u1, v1).normal(0.0F, -1.0F, 0.0F).endVertex();

        wr.pos(-halfW + segX[SEGMENTS], segY[SEGMENTS], segZ[SEGMENTS]).tex(u2, v0).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(halfW + segX[SEGMENTS], segY[SEGMENTS], segZ[SEGMENTS]).tex(u3, v0).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(halfW + segX[SEGMENTS], segY[SEGMENTS], segZ[SEGMENTS] + d).tex(u3, v1).normal(0.0F, 1.0F, 0.0F).endVertex();
        wr.pos(-halfW + segX[SEGMENTS], segY[SEGMENTS], segZ[SEGMENTS] + d).tex(u2, v1).normal(0.0F, 1.0F, 0.0F).endVertex();

        tess.draw();
    }
}
