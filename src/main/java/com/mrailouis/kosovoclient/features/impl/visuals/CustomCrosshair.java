package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ColorSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.render.NanoVGManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Getter
public class CustomCrosshair extends Module {
    private static final CustomCrosshair INSTANCE = new CustomCrosshair();

    private final ModeSetting style = new ModeSetting("Style", "Shape and style of the crosshair.", "Cross", new String[]{"Cross", "Dot", "Circle", "Square", "Chevron", "T-Shape"});
    private final ColorSetting color = new ColorSetting("Color", "Color of the crosshair.", 0xFFFFFFFF);
    private final BooleanSetting highlightTarget = new BooleanSetting("Highlight Target", "Change crosshair color when aiming at an entity.", true);
    private final ColorSetting targetColor = new ColorSetting("Target Color", "Color when aiming at an entity.", 0xFFFF3333);
    private final NumberSetting size = new NumberSetting("Size", "Length or radius of the crosshair.", 4.0, 1.0, 20.0, 0.5);
    private final NumberSetting gap = new NumberSetting("Gap", "Gap distance from the center.", 3.0, 0.0, 15.0, 0.5);
    private final NumberSetting thickness = new NumberSetting("Thickness", "Thickness of the crosshair lines.", 1.5, 0.5, 5.0, 0.5);
    private final BooleanSetting centerDot = new BooleanSetting("Center Dot", "Draw a dot in the center of the crosshair.", false);
    private final NumberSetting dotSize = new NumberSetting("Dot Size", "Size of the center dot.", 1.0, 0.5, 4.0, 0.5);
    private final BooleanSetting outline = new BooleanSetting("Outline", "Draw high-contrast outline around crosshair.", true);
    private final ColorSetting outlineColor = new ColorSetting("Outline Color", "Color of the crosshair outline.", 0xAA000000);
    private final BooleanSetting dynamic = new BooleanSetting("Dynamic", "Expand crosshair gap dynamically while moving or jumping.", false);
    private final BooleanSetting thirdPerson = new BooleanSetting("Third Person", "Show crosshair in third person view.", false);

    public static CustomCrosshair getInstance() {
        return INSTANCE;
    }

    private CustomCrosshair() {
        super("Custom Crosshair", "Fully customizable crosshair with custom styles, colors, and dynamic scaling.", Category.VISUALS, true);
        registerSetting(this.style);
        registerSetting(this.color);
        registerSetting(this.highlightTarget);
        registerSetting(this.targetColor);
        registerSetting(this.size);
        registerSetting(this.gap);
        registerSetting(this.thickness);
        registerSetting(this.centerDot);
        registerSetting(this.dotSize);
        registerSetting(this.outline);
        registerSetting(this.outlineColor);
        registerSetting(this.dynamic);
        registerSetting(this.thirdPerson);
    }

    @SubscribeEvent
    public void onRenderCrosshair(RenderGameOverlayEvent.Pre event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        event.setCanceled(true);

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (mc.gameSettings.hideGUI) {
            return;
        }

        if (mc.gameSettings.thirdPersonView != 0 && !this.thirdPerson.isEnabled()) {
            return;
        }

        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            return;
        }

        renderCustomCrosshair();
    }

    private void renderCustomCrosshair() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();
        float pixelRatio = (float) mc.displayWidth / screenWidth;

        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;

        boolean isTargeting = this.highlightTarget.isEnabled() && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit instanceof EntityLivingBase;

        int activeColor = isTargeting ? this.targetColor.getColor() : this.color.getColor();
        int activeOutlineColor = this.outlineColor.getColor();
        boolean drawOutline = this.outline.isEnabled();

        float sizeVal = this.size.getValue().floatValue();
        float gapVal = this.gap.getValue().floatValue();
        float thicknessVal = this.thickness.getValue().floatValue();
        float dotSizeVal = this.dotSize.getValue().floatValue();

        if (this.dynamic.isEnabled() && mc.thePlayer != null) {
            if (!mc.thePlayer.onGround) {
                gapVal += 2.5f;
            } else if (mc.thePlayer.isSprinting()) {
                gapVal += 1.5f;
            } else if (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f) {
                gapVal += 1.0f;
            }
        }

        NanoVGManager nvg = NanoVGManager.getInstance();
        nvg.beginFrame(screenWidth, screenHeight, pixelRatio);

        String styleVal = this.style.getValue();

        if (this.centerDot.isEnabled() || "Dot".equalsIgnoreCase(styleVal)) {
            if (drawOutline) {
                nvg.drawCircle(centerX, centerY, dotSizeVal + 0.75f, activeOutlineColor);
            }
            nvg.drawCircle(centerX, centerY, dotSizeVal, activeColor);
        }

        if ("Cross".equalsIgnoreCase(styleVal) || "T-Shape".equalsIgnoreCase(styleVal)) {
            boolean isT = "T-Shape".equalsIgnoreCase(styleVal);
            float halfThickness = thicknessVal / 2.0f;

            float topX = centerX - halfThickness;
            float topY = centerY - gapVal - sizeVal;

            float botX = centerX - halfThickness;
            float botY = centerY + gapVal;

            float leftX = centerX - gapVal - sizeVal;
            float leftY = centerY - halfThickness;

            float rightX = centerX + gapVal;
            float rightY = centerY - halfThickness;

            if (drawOutline) {
                float o = 1.0f;
                if (!isT) {
                    nvg.drawRect(topX - o, topY - o, thicknessVal + o * 2.0f, sizeVal + o * 2.0f, activeOutlineColor);
                }
                nvg.drawRect(botX - o, botY - o, thicknessVal + o * 2.0f, sizeVal + o * 2.0f, activeOutlineColor);
                nvg.drawRect(leftX - o, leftY - o, sizeVal + o * 2.0f, thicknessVal + o * 2.0f, activeOutlineColor);
                nvg.drawRect(rightX - o, rightY - o, sizeVal + o * 2.0f, thicknessVal + o * 2.0f, activeOutlineColor);
            }

            if (!isT) {
                nvg.drawRect(topX, topY, thicknessVal, sizeVal, activeColor);
            }
            nvg.drawRect(botX, botY, thicknessVal, sizeVal, activeColor);
            nvg.drawRect(leftX, leftY, sizeVal, thicknessVal, activeColor);
            nvg.drawRect(rightX, rightY, sizeVal, thicknessVal, activeColor);
        } else if ("Circle".equalsIgnoreCase(styleVal)) {
            float radius = sizeVal + gapVal;
            if (drawOutline) {
                nvg.drawCircleOutline(centerX, centerY, radius, thicknessVal + 1.5f, activeOutlineColor);
            }
            nvg.drawCircleOutline(centerX, centerY, radius, thicknessVal, activeColor);
        } else if ("Square".equalsIgnoreCase(styleVal)) {
            float halfSize = sizeVal + gapVal;
            float x = centerX - halfSize;
            float y = centerY - halfSize;
            float w = halfSize * 2.0f;
            float h = halfSize * 2.0f;

            if (drawOutline) {
                nvg.drawRoundedRectOutline(x, y, w, h, 0.0f, thicknessVal + 1.5f, activeOutlineColor);
            }
            nvg.drawRoundedRectOutline(x, y, w, h, 0.0f, thicknessVal, activeColor);
        } else if ("Chevron".equalsIgnoreCase(styleVal)) {
            float leftX = centerX - gapVal - sizeVal;
            float rightX = centerX + gapVal + sizeVal;
            float topY = centerY - gapVal;
            float botY = centerY + sizeVal;

            if (drawOutline) {
                nvg.drawLine(leftX, botY, centerX, topY, thicknessVal + 1.5f, activeOutlineColor);
                nvg.drawLine(centerX, topY, rightX, botY, thicknessVal + 1.5f, activeOutlineColor);
            }
            nvg.drawLine(leftX, botY, centerX, topY, thicknessVal, activeColor);
            nvg.drawLine(centerX, topY, rightX, botY, thicknessVal, activeColor);
        }

        nvg.endFrame();
    }
}
