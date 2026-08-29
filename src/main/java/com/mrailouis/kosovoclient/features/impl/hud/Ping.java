package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Collections;
import java.util.List;

@Getter
public class Ping extends HudModule {
    private static final Ping INSTANCE = new Ping();

    private final BooleanSetting showText = new BooleanSetting("Show Text", "Display 'Ping:' label or only number.", true);

    public static Ping getInstance() {
        return INSTANCE;
    }

    private Ping() {
        super("Ping", "Displays your network latency.", 10.0f, 54.0f);
        registerSetting(this.showText);
    }

    public int getPing() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.getNetHandler() != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info != null) {
                return Math.max(0, info.getResponseTime());
            }
        }
        return 0;
    }

    @Override
    public List<String> getLines(boolean example) {
        int ping = example ? 32 : getPing();
        if (this.showText.isEnabled()) {
            return Collections.singletonList("Ping: " + ping + " ms");
        }
        return Collections.singletonList(ping + " ms");
    }
}
