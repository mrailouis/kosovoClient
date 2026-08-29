package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Getter
public class Fullbright extends Module {
    private static final Fullbright INSTANCE = new Fullbright();

    private final ModeSetting mode = new ModeSetting("Mode", "Brightness method to use.", "Gamma", new String[]{"Gamma", "Night Vision"});
    private float previousGamma = 1.0f;
    private String lastMode = "Gamma";

    public static Fullbright getInstance() {
        return INSTANCE;
    }

    private Fullbright() {
        super("Fullbright", "Flashbang simulator", Category.VISUALS, false);
        registerSetting(mode);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null) {
            if (mc.gameSettings.gammaSetting <= 1.0f) {
                this.previousGamma = mc.gameSettings.gammaSetting;
            }
            if (this.mode.is("Gamma")) {
                mc.gameSettings.gammaSetting = 100.0f;
            }
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = this.previousGamma;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.gameSettings == null) {
            return;
        }

        if (isEnabled()) {
            if (this.mode.is("Gamma")) {
                if (!"Gamma".equalsIgnoreCase(this.lastMode)) {
                    this.lastMode = "Gamma";
                }
                if (mc.gameSettings.gammaSetting < 100.0f) {
                    mc.gameSettings.gammaSetting = 100.0f;
                }
            } else if (this.mode.is("Night Vision")) {
                if ("Gamma".equalsIgnoreCase(this.lastMode)) {
                    this.lastMode = "Night Vision";
                    mc.gameSettings.gammaSetting = this.previousGamma;
                }
            }
        }
    }
}
