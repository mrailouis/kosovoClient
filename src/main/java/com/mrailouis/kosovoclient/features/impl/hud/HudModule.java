package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class HudModule extends Module {
    private float x;
    private float y;
    private float scale = 1.0f;
    private float width;
    private float height;

    private final ModeSetting font = new ModeSetting("Font", "Font style to render text with.", "Custom", new String[]{"Custom", "Minecraft"});
    private final BooleanSetting background = new BooleanSetting("Draw Background", "Draw blurred background panel.", true);

    public HudModule(String name, String description, float defaultX, float defaultY) {
        super(name, description, Category.HUD, true);
        this.x = defaultX;
        this.y = defaultY;
        registerSetting(this.font);
        registerSetting(this.background);
    }

    public abstract List<String> getLines(boolean example);
}
