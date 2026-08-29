package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.render.NanoVGManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.nanovg.NanoVG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {

    private static final int KOSOVO_COLOR = 0xFFFF3333;
    private static final int CLIENT_COLOR = 0xFFFFFFFF;
    private static final float TITLE_FONT_SIZE = 42.0f;
    private static final float TITLE_CENTER_Y = 52.0f;

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;drawTexturedModalRect(IIIIII)V"))
    private void cancelMinecraftLogo(GuiMainMenu instance, int x, int y, int textureX, int textureY, int width, int height) {
    }

    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;drawCenteredString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V"))
    private void cancelSplashText(GuiMainMenu instance, FontRenderer fontRenderer, String text, int x, int y, int color) {
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private void renderKosovoClientTitle(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (this.mc == null) {
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        float screenWidth = scaledResolution.getScaledWidth();
        float screenHeight = scaledResolution.getScaledHeight();
        float pixelRatio = (float) this.mc.displayWidth / screenWidth;

        NanoVGManager nvg = NanoVGManager.getInstance();
        nvg.beginFrame(screenWidth, screenHeight, pixelRatio);

        float kosovoWidth = nvg.getTextWidth("kosovo", NanoVGManager.FONT_INTER_BOLD, TITLE_FONT_SIZE);
        float clientWidth = nvg.getTextWidth("client", NanoVGManager.FONT_INTER_BOLD, TITLE_FONT_SIZE);
        float totalWidth = kosovoWidth + clientWidth;

        float startX = (screenWidth - totalWidth) / 2.0f;

        nvg.drawText("kosovo", startX, TITLE_CENTER_Y, NanoVGManager.FONT_INTER_BOLD, TITLE_FONT_SIZE, KOSOVO_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        nvg.drawText("client", startX + kosovoWidth, TITLE_CENTER_Y, NanoVGManager.FONT_INTER_BOLD, TITLE_FONT_SIZE, CLIENT_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        nvg.endFrame();
    }
}
