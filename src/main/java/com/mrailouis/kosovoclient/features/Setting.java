package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Setting<T> {
    protected final String name;
    protected final String description;
    protected T value;
    protected float hoverProgress;

    public Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }
}
