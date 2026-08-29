package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Collections;
import java.util.List;

@Getter
public class FPS extends HudModule {
    private static final FPS INSTANCE = new FPS();

    private final BooleanSetting showText = new BooleanSetting("Show Text", "Display 'FPS' label or only number.", true);

    public static FPS getInstance() {
        return INSTANCE;
    }

    private FPS() {
        super("FPS", "Displays your current frames per second.", 10.0f, 32.0f);
        registerSetting(this.showText);
    }

    @Override
    public List<String> getLines(boolean example) {
        int fps = example ? 144 : Minecraft.getDebugFPS();
        if (this.showText.isEnabled()) {
            return Collections.singletonList(fps + " FPS");
        }
        return Collections.singletonList(String.valueOf(fps));
    }
}
