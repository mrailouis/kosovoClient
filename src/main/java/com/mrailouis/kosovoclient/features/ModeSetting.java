package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeSetting extends Setting<String> {
    private final String[] modes;
    private int index;

    public ModeSetting(String name, String description, String defaultMode, String[] modes) {
        super(name, description, defaultMode);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(defaultMode)) {
                this.index = i;
                break;
            }
        }
    }

    public void cycle() {
        if (modes.length == 0) {
            return;
        }
        this.index = (this.index + 1) % modes.length;
        this.value = modes[this.index];
    }

    public void cyclePrevious() {
        if (modes.length == 0) {
            return;
        }
        this.index = (this.index - 1 + modes.length) % modes.length;
        this.value = modes[this.index];
    }

    public boolean is(String mode) {
        return this.value != null && this.value.equalsIgnoreCase(mode);
    }
}
