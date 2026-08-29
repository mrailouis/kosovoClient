package com.mrailouis.kosovoclient.gui;

import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.ModuleManager;
import com.mrailouis.kosovoclient.features.impl.hud.HudModule;
import com.mrailouis.kosovoclient.features.impl.hud.HudRenderer;
import com.mrailouis.kosovoclient.render.NanoVGManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.util.List;

@Getter
public class EditHudsScreen extends GuiScreen {
    private final GuiScreen parentScreen;
    private HudModule draggingModule;
    private float dragOffsetX;
    private float dragOffsetY;

    public EditHudsScreen(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    public EditHudsScreen() {
        this(null);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dw = Mouse.getEventDWheel();
        if (dw != 0) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mc);
            int mouseX = Mouse.getEventX() * sr.getScaledWidth() / mc.displayWidth;
            int mouseY = sr.getScaledHeight() - Mouse.getEventY() * sr.getScaledHeight() / mc.displayHeight - 1;
            HudModule hovered = getHoveredModule(mouseX, mouseY);
            if (hovered != null) {
                float currentScale = hovered.getScale();
                float step = dw > 0 ? 0.05f : -0.05f;
                float newScale = Math.round(Math.max(0.5f, Math.min(3.0f, currentScale + step)) * 20.0f) / 20.0f;
                hovered.setScale(newScale);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, 0x44000000);

        if (this.draggingModule != null) {
            float newX = Math.max(0.0f, Math.min(this.width - this.draggingModule.getWidth(), mouseX - this.dragOffsetX));
            float newY = Math.max(0.0f, Math.min(this.height - this.draggingModule.getHeight(), mouseY - this.dragOffsetY));
            this.draggingModule.setX(newX);
            this.draggingModule.setY(newY);
        }

        HudModule hovered = getHoveredModule(mouseX, mouseY);

        for (Module m : ModuleManager.getInstance().getModules()) {
            if (m instanceof HudModule && m.isEnabled()) {
                HudModule hud = (HudModule) m;
                HudRenderer.getInstance().renderModule(hud, true, hud == hovered, hud == this.draggingModule);
            }
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();
        float pixelRatio = (float) mc.displayWidth / screenWidth;

        NanoVGManager nvg = NanoVGManager.getInstance();
        nvg.beginFrame(screenWidth, screenHeight, pixelRatio);

        float bannerWidth = 240.0f;
        float bannerHeight = 36.0f;
        float bannerX = (screenWidth - bannerWidth) / 2.0f;
        float bannerY = 14.0f;
        nvg.drawGlassPanel(bannerX, bannerY, bannerWidth, bannerHeight, 6.0f, 0xBF000000, 0x22FFFFFF);
        nvg.drawText("Edit HUDs", bannerX + bannerWidth / 2.0f, bannerY + 12.0f, NanoVGManager.FONT_INTER_BOLD, 12.0f, 0xFFFF3333, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
        nvg.drawText("Drag to move • Scroll to scale", bannerX + bannerWidth / 2.0f, bannerY + 25.0f, NanoVGManager.FONT_INTER, 9.0f, 0x99FFFFFF, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

        float doneWidth = 60.0f;
        float doneHeight = 24.0f;
        float doneX = screenWidth - doneWidth - 16.0f;
        float doneY = 14.0f;
        boolean doneHovered = mouseX >= doneX && mouseX <= doneX + doneWidth && mouseY >= doneY && mouseY <= doneY + doneHeight;
        nvg.drawGlassPanel(doneX, doneY, doneWidth, doneHeight, 4.0f, doneHovered ? 0xDDFF3333 : 0xBF000000, doneHovered ? 0xFFFF5555 : 0x22FFFFFF);
        nvg.drawText("Done", doneX + doneWidth / 2.0f, doneY + doneHeight / 2.0f, NanoVGManager.FONT_INTER_BOLD, 11.0f, 0xFFFFFFFF, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

        nvg.endFrame();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float doneWidth = 60.0f;
        float doneHeight = 24.0f;
        float doneX = this.width - doneWidth - 16.0f;
        float doneY = 14.0f;
        if (mouseX >= doneX && mouseX <= doneX + doneWidth && mouseY >= doneY && mouseY <= doneY + doneHeight) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }

        if (mouseButton == 0) {
            HudModule hovered = getHoveredModule(mouseX, mouseY);
            if (hovered != null) {
                this.draggingModule = hovered;
                this.dragOffsetX = mouseX - hovered.getX();
                this.dragOffsetY = mouseY - hovered.getY();
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.draggingModule = null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parentScreen);
            return;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private HudModule getHoveredModule(float mouseX, float mouseY) {
        List<Module> modules = ModuleManager.getInstance().getModules();
        for (int i = modules.size() - 1; i >= 0; i--) {
            Module m = modules.get(i);
            if (m instanceof HudModule && m.isEnabled()) {
                HudModule hud = (HudModule) m;
                if (mouseX >= hud.getX() && mouseX <= hud.getX() + hud.getWidth() &&
                        mouseY >= hud.getY() && mouseY <= hud.getY() + hud.getHeight()) {
                    return hud;
                }
            }
        }
        return null;
    }
}
