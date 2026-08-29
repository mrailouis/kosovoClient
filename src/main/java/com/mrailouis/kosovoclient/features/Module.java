package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private boolean expanded;
    private float expandProgress;
    private float toggleProgress;
    private float hoverProgress;
    private final List<Setting<?>> settings = new ArrayList<Setting<?>>();

    public Module(String name, String description, Category category, boolean defaultEnabled) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = defaultEnabled;
        this.toggleProgress = defaultEnabled ? 1.0f : 0.0f;
    }

    public void registerSetting(Setting<?> setting) {
        this.settings.add(setting);
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (this.enabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
