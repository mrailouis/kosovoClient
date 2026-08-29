package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.ModuleManager;
import com.mrailouis.kosovoclient.gui.EditHudsScreen;
import com.mrailouis.kosovoclient.render.NanoVGManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Getter
public class HudRenderer {
    private static final HudRenderer INSTANCE = new HudRenderer();

    public static HudRenderer getInstance() {
        return INSTANCE;
    }

    private HudRenderer() {
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI) {
            return;
        }

        if (mc.currentScreen instanceof EditHudsScreen) {
            return;
        }

        for (Module module : ModuleManager.getInstance().getModules()) {
            if (module instanceof HudModule && module.isEnabled()) {
                renderModule((HudModule) module, false, false, false);
            }
        }
    }

    public void renderModule(HudModule module, boolean example, boolean hovered, boolean selected) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();
        float pixelRatio = (float) mc.displayWidth / screenWidth;

        List<String> lines = module.getLines(example);
        if (lines == null || lines.isEmpty()) {
            return;
        }

        boolean customFont = module.getFont().is("Custom");
        float scale = Math.max(0.5f, Math.min(3.0f, module.getScale()));
        float fontSize = customFont ? (13.0f * scale) : (9.0f * scale);
        float mcTextScale = scale;
        float lineHeight = customFont ? fontSize : (9.0f * mcTextScale);
        float lineSpacing = 2.0f * scale;
        float paddingX = (module.getBackground().isEnabled() ? 5.0f : 2.0f) * scale;
        float paddingY = (module.getBackground().isEnabled() ? 4.0f : 2.0f) * scale;
        float cornerRadius = 4.0f * scale;

        NanoVGManager nvg = NanoVGManager.getInstance();
        nvg.init();

        float maxLineWidth = 0.0f;

        for (String line : lines) {
            float lineWidth = customFont
                    ? nvg.getTextWidth(line, NanoVGManager.FONT_INTER, fontSize)
                    : (mc.fontRendererObj.getStringWidth(line) * mcTextScale);
            if (lineWidth > maxLineWidth) {
                maxLineWidth = lineWidth;
            }
        }

        float totalTextHeight = lines.size() * lineHeight + Math.max(0, lines.size() - 1) * lineSpacing;
        float totalWidth = maxLineWidth + paddingX * 2.0f;
        float totalHeight = totalTextHeight + paddingY * 2.0f;

        module.setWidth(totalWidth);
        module.setHeight(totalHeight);

        float x = module.getX();
        float y = module.getY();

        nvg.beginFrame(screenWidth, screenHeight, pixelRatio);

        if (module.getBackground().isEnabled()) {
            nvg.drawRoundedRect(x, y, totalWidth, totalHeight, cornerRadius, 0xBF000000);
        }

        if (example) {
            if (selected) {
                nvg.drawRoundedRectOutline(x, y, totalWidth, totalHeight, cornerRadius, 1.0f, 0xFFFF3333);
            } else if (hovered) {
                nvg.drawRoundedRectOutline(x, y, totalWidth, totalHeight, cornerRadius, 1.0f, 0x88FF3333);
            } else {
                nvg.drawRoundedRectOutline(x, y, totalWidth, totalHeight, cornerRadius, 0.75f, 0x33FFFFFF);
            }
        }

        if (customFont) {
            float curY = y + paddingY;
            for (String line : lines) {
                float textCenterY = curY + (lineHeight / 2.0f);
                nvg.drawText(line, x + paddingX, textCenterY, NanoVGManager.FONT_INTER, fontSize, 0xFFFFFFFF, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
                curY += lineHeight + lineSpacing;
            }
        }

        nvg.endFrame();

        if (!customFont) {
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            float curY = y + paddingY;
            for (String line : lines) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + paddingX, curY, 0.0f);
                GlStateManager.scale(mcTextScale, mcTextScale, 1.0f);
                mc.fontRendererObj.drawStringWithShadow(line, 0, 0, 0xFFFFFFFF);
                GlStateManager.popMatrix();
                curY += lineHeight + lineSpacing;
            }

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();
        }
    }
}
