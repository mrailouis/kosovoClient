package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ColorSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@Getter
public class CustomNametags extends Module {
    private static final CustomNametags INSTANCE = new CustomNametags();

    private final BooleanSetting showHealth = new BooleanSetting("Show Health", "Display player health.", true);
    private final BooleanSetting showPing = new BooleanSetting("Show Ping", "Display network latency.", true);
    private final BooleanSetting background = new BooleanSetting("Background", "Draw dark background behind nametag.", true);
    private final ColorSetting backgroundColor = new ColorSetting("Background Color", "Color of the nametag background.", 0xBF000000);
    private final NumberSetting scale = new NumberSetting("Scale", "Overall scale of the nametag.", 1.0, 0.5, 2.5, 0.1);

    public static CustomNametags getInstance() {
        return INSTANCE;
    }

    private CustomNametags() {
        super("Custom Nametags", "Enhanced and customizable player nametags with health and ping.", Category.VISUALS, true);
        registerSetting(this.showHealth);
        registerSetting(this.showPing);
        registerSetting(this.background);
        registerSetting(this.backgroundColor);
        registerSetting(this.scale);
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (!isEnabled()) {
            return;
        }

        EntityPlayer player = event.entityPlayer;
        Minecraft mc = Minecraft.getMinecraft();
        if (player == null || player == mc.thePlayer || player.isInvisible()) {
            return;
        }

        renderNametag(player, event.x, event.y, event.z, mc.getRenderManager());
    }

    private void renderNametag(EntityPlayer player, double x, double y, double z, RenderManager renderManager) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRendererObj;

        float distance = player.getDistanceToEntity(renderManager.livingPlayer);
        float tagScale = Math.max(0.018f, 0.0018f + (0.003f * distance)) * this.scale.getValue().floatValue();

        double renderY = y + player.height + 0.5D;
        if (player.isSneaking()) {
            renderY -= 0.25D;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, renderY, z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-tagScale, -tagScale, tagScale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        String name = player.getDisplayName().getFormattedText();
        StringBuilder tagBuilder = new StringBuilder();

        if (this.showPing.isEnabled()) {
            int ping = getPlayerPing(player);
            tagBuilder.append(getPingColorCode(ping)).append(ping).append("ms §r");
        }

        tagBuilder.append(name);

        if (this.showHealth.isEnabled()) {
            float health = (float) Math.ceil(player.getHealth() + player.getAbsorptionAmount());
            tagBuilder.append(" ").append(getHealthColorCode(health)).append((int) health).append("❤§r");
        }

        String fullTag = tagBuilder.toString();
        int strWidth = fontRenderer.getStringWidth(fullTag);
        int halfWidth = strWidth / 2;

        if (this.background.isEnabled()) {
            drawRect(-halfWidth - 3, -2, halfWidth + 3, 10, this.backgroundColor.getColor());
        }

        fontRenderer.drawStringWithShadow(fullTag, -halfWidth, 0, 0xFFFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private int getPlayerPing(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() != null && player.getUniqueID() != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getUniqueID());
            if (info != null) {
                return Math.max(0, info.getResponseTime());
            }
        }
        return 0;
    }

    private String getPingColorCode(int ping) {
        if (ping <= 50) return "§a";
        if (ping <= 100) return "§e";
        if (ping <= 150) return "§6";
        return "§c";
    }

    private String getHealthColorCode(float health) {
        if (health >= 15.0f) return "§a";
        if (health >= 10.0f) return "§e";
        if (health >= 5.0f) return "§6";
        return "§c";
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int temp = left;
            left = right;
            right = temp;
        }
        if (top < bottom) {
            int temp = top;
            top = bottom;
            bottom = temp;
        }

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
