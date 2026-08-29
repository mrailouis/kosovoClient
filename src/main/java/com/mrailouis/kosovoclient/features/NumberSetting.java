package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double increment;
    private boolean dragging;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double increment) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public float getNormalized() {
        return (float) ((this.value - this.min) / (this.max - this.min));
    }

    public void setNormalized(float ratio) {
        double clamped = Math.max(0.0, Math.min(1.0, ratio));
        double raw = this.min + (this.max - this.min) * clamped;
        double steps = Math.round((raw - this.min) / this.increment);
        this.value = Math.max(this.min, Math.min(this.max, this.min + steps * this.increment));
    }
}
