package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

import java.awt.Color;

@Getter
@Setter
public class ColorSetting extends Setting<Integer> {
    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;
    private float alpha = 1.0f;

    private float targetHue = 0.0f;
    private float targetSaturation = 1.0f;
    private float targetBrightness = 1.0f;
    private float targetAlpha = 1.0f;

    private boolean expanded = false;
    private float expandProgress = 0.0f;

    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public ColorSetting(String name, String description, int defaultColor) {
        super(name, description, defaultColor);
        setColor(defaultColor);
    }

    public void setColor(int hexColor) {
        this.value = hexColor;
        int a = (hexColor >> 24) & 0xFF;
        int r = (hexColor >> 16) & 0xFF;
        int g = (hexColor >> 8) & 0xFF;
        int b = hexColor & 0xFF;
        if ((hexColor & 0xFF000000) == 0) {
            a = 255;
            this.value = (0xFF << 24) | (hexColor & 0x00FFFFFF);
        }

        this.alpha = a / 255.0f;
        this.targetAlpha = this.alpha;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        this.hue = hsb[0];
        this.targetHue = this.hue;
        this.saturation = hsb[1];
        this.targetSaturation = this.saturation;
        this.brightness = hsb[2];
        this.targetBrightness = this.brightness;
    }

    public void updateFromHSBA() {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 0x00FFFFFF;
        int a = Math.max(0, Math.min(255, Math.round(this.alpha * 255.0f)));
        this.value = (a << 24) | rgb;
    }

    public void animate(float deltaSeconds) {
        float factor = Math.min(1.0f, deltaSeconds * 28.0f);
        this.hue += (this.targetHue - this.hue) * factor;
        this.saturation += (this.targetSaturation - this.saturation) * factor;
        this.brightness += (this.targetBrightness - this.brightness) * factor;
        this.alpha += (this.targetAlpha - this.alpha) * factor;
        updateFromHSBA();
    }

    public int getColor() {
        return this.value != null ? this.value : 0xFFFFFFFF;
    }

    public float getRed() {
        return ((getColor() >> 16) & 0xFF) / 255.0f;
    }

    public float getGreen() {
        return ((getColor() >> 8) & 0xFF) / 255.0f;
    }

    public float getBlue() {
        return (getColor() & 0xFF) / 255.0f;
    }

    public float getAlpha() {
        return ((getColor() >> 24) & 0xFF) / 255.0f;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }
}
