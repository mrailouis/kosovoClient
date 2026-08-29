package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeSetting extends Setting<String> {
    private String[] modes;
    private int index;

    public ModeSetting(String name, String description, String defaultMode, String[] modes) {
        super(name, description, defaultMode);
        this.modes = modes == null ? new String[0] : modes;
        for (int i = 0; i < this.modes.length; i++) {
            if (this.modes[i].equalsIgnoreCase(defaultMode)) {
                this.index = i;
                break;
            }
        }
    }

    public void setModes(String[] newModes) {
        this.modes = newModes == null ? new String[0] : newModes;
        this.index = 0;
        for (int i = 0; i < this.modes.length; i++) {
            if (this.modes[i].equalsIgnoreCase(this.value)) {
                this.index = i;
                return;
            }
        }
        if (this.modes.length > 0) {
            this.value = this.modes[0];
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
