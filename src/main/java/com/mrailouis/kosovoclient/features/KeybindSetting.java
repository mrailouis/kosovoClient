package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;

@Getter
@Setter
public class KeybindSetting extends Setting<Integer> {
    private boolean listening;

    public KeybindSetting(String name, String description, int defaultKeyCode) {
        super(name, description, defaultKeyCode);
    }

    public int getKeyCode() {
        return this.value != null ? this.value : Keyboard.KEY_NONE;
    }

    public void setKeyCode(int keyCode) {
        this.value = keyCode;
    }

    public String getKeyName() {
        int code = getKeyCode();
        if (code == Keyboard.KEY_NONE) {
            return "None";
        }
        String name = Keyboard.getKeyName(code);
        return name != null ? name : "Unknown";
    }

    public boolean isPressed() {
        int code = getKeyCode();
        return code != Keyboard.KEY_NONE && Keyboard.isKeyDown(code);
    }
}
