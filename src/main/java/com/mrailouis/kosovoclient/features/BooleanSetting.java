package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BooleanSetting extends Setting<Boolean> {
    private float toggleProgress;
    private Runnable onToggleAction;

    public BooleanSetting(String name, String description, boolean defaultValue) {
        super(name, description, defaultValue);
        this.toggleProgress = defaultValue ? 1.0f : 0.0f;
    }

    public BooleanSetting(String name, String description, boolean defaultValue, Runnable onToggleAction) {
        super(name, description, defaultValue);
        this.toggleProgress = defaultValue ? 1.0f : 0.0f;
        this.onToggleAction = onToggleAction;
    }

    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.value);
    }

    public void toggle() {
        this.value = !isEnabled();
        if (this.onToggleAction != null) {
            this.onToggleAction.run();
        }
    }
}
