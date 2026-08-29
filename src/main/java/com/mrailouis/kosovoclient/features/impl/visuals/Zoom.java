package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.KeybindSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Getter
public class Zoom extends Module {
    private static final Zoom INSTANCE = new Zoom();

    private final KeybindSetting keybind = new KeybindSetting("Keybind", "Key used to activate zoom.", Keyboard.KEY_C);
    private final ModeSetting keyMode = new ModeSetting("Key Mode", "Whether to hold or toggle zoom.", "Hold", new String[]{"Hold", "Toggle"});
    private final NumberSetting targetFov = new NumberSetting("Target FOV", "Base FOV to zoom into.", 30.0, 5.0, 90.0, 1.0);
    private final BooleanSetting cinematicCamera = new BooleanSetting("Cinematic Camera", "Smooth camera drag while zooming.", true);
    private final BooleanSetting interpolateCamera = new BooleanSetting("Interpolate Camera", "Smoothly animate FOV and scroll changes.", true);

    private boolean zooming = false;
    private double scrollZoomOffset = 0.0;
    private double targetScrollOffset = 0.0;
    private float zoomAnimationProgress = 0.0f;
    private float prevZoomAnimationProgress = 0.0f;
    private boolean originalSmoothCamera = false;
    private long lastAnimTime = System.currentTimeMillis();

    public static Zoom getInstance() {
        return INSTANCE;
    }

    private Zoom() {
        super("Zoom", "OptiFine-style smooth zooming with mouse wheel scrolling and cinematic drag.", Category.VISUALS, true);
        registerSetting(keybind);
        registerSetting(keyMode);
        registerSetting(targetFov);
        registerSetting(cinematicCamera);
        registerSetting(interpolateCamera);
    }

    @Override
    public void onDisable() {
        this.zooming = false;
        this.scrollZoomOffset = 0.0;
        this.targetScrollOffset = 0.0;
        this.zoomAnimationProgress = 0.0f;
        this.prevZoomAnimationProgress = 0.0f;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null) {
            mc.gameSettings.smoothCamera = this.originalSmoothCamera;
        }
    }

    public void onKeyInput() {
        if (!isEnabled()) {
            return;
        }

        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (key != Keyboard.KEY_NONE && key == this.keybind.getKeyCode()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen != null) {
                return;
            }

            if (this.keyMode.is("Toggle")) {
                if (state) {
                    setZooming(!this.zooming);
                }
            }
        }
    }

    private void setZooming(boolean zoom) {
        if (this.zooming == zoom) {
            return;
        }
        this.zooming = zoom;
        Minecraft mc = Minecraft.getMinecraft();
        if (this.zooming) {
            this.scrollZoomOffset = 0.0;
            this.targetScrollOffset = 0.0;
            if (mc != null && mc.gameSettings != null) {
                this.originalSmoothCamera = mc.gameSettings.smoothCamera;
                if (this.cinematicCamera.isEnabled()) {
                    mc.gameSettings.smoothCamera = true;
                }
            }
        } else {
            this.scrollZoomOffset = 0.0;
            this.targetScrollOffset = 0.0;
            if (mc != null && mc.gameSettings != null) {
                mc.gameSettings.smoothCamera = this.originalSmoothCamera;
            }
        }
    }

    public void handleMouseScroll(int dWheel) {
        if (!isEnabled() || !this.zooming || dWheel == 0) {
            return;
        }

        double step = dWheel > 0 ? -4.0 : 4.0;
        double currentBase = this.targetFov.getValue();
        double minOffset = 5.0 - currentBase;
        double maxOffset = 110.0 - currentBase;

        this.targetScrollOffset = Math.max(minOffset, Math.min(maxOffset, this.targetScrollOffset + step));
        if (!this.interpolateCamera.isEnabled()) {
            this.scrollZoomOffset = this.targetScrollOffset;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }

        if (isEnabled() && this.keyMode.is("Hold")) {
            if (mc.currentScreen == null) {
                int key = this.keybind.getKeyCode();
                boolean isDown = key != Keyboard.KEY_NONE && Keyboard.isKeyDown(key);
                setZooming(isDown);
            } else if (this.zooming) {
                setZooming(false);
            }
        }

        if (this.zooming && mc.gameSettings != null) {
            if (this.cinematicCamera.isEnabled()) {
                mc.gameSettings.smoothCamera = true;
            } else {
                mc.gameSettings.smoothCamera = false;
            }
        }
    }

    public float getFov(float baseFov, float partialTicks) {
        if (!isEnabled()) {
            return baseFov;
        }

        long now = System.currentTimeMillis();
        float deltaSeconds = Math.min(0.1f, (now - this.lastAnimTime) / 1000.0f);
        this.lastAnimTime = now;

        if (this.interpolateCamera.isEnabled()) {
            this.scrollZoomOffset += (this.targetScrollOffset - this.scrollZoomOffset) * Math.min(1.0, deltaSeconds * 18.0);
            float targetProgress = this.zooming ? 1.0f : 0.0f;
            this.zoomAnimationProgress += (targetProgress - this.zoomAnimationProgress) * Math.min(1.0f, deltaSeconds * 20.0f);
            if (Math.abs(this.zoomAnimationProgress - targetProgress) < 0.001f) {
                this.zoomAnimationProgress = targetProgress;
            }
        } else {
            this.scrollZoomOffset = this.targetScrollOffset;
            this.zoomAnimationProgress = this.zooming ? 1.0f : 0.0f;
        }

        if (this.zoomAnimationProgress <= 0.0f) {
            return baseFov;
        }

        double desiredFov = Math.max(2.0, Math.min(110.0, this.targetFov.getValue() + this.scrollZoomOffset));
        float effectiveFov = (float) (baseFov + (desiredFov - (double) baseFov) * (double) this.zoomAnimationProgress);

        return effectiveFov;
    }
}
