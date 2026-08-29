package com.mrailouis.kosovoclient.features.impl.player.autogg;

import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.regex.Pattern;

@Getter
public class Server {
    private final String name;
    private final String kind;
    private final String data;
    private final String messagePrefix;
    private final Trigger[] triggers;

    public Server(String name, String kind, String data, String messagePrefix, Trigger[] triggers) {
        this.name = name;
        this.kind = kind;
        this.data = data;
        this.messagePrefix = messagePrefix;
        this.triggers = triggers;
    }

    public boolean detect() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return false;
        }

        if ("SERVER_BRANDING".equalsIgnoreCase(this.kind)) {
            String brand = mc.thePlayer.getClientBrand();
            if (brand != null) {
                Pattern p = PatternHandler.getInstance().getOrRegisterPattern(this.data);
                if (p != null && p.matcher(brand).matches()) {
                    return true;
                }
            }
        } else if ("SERVER_IP".equalsIgnoreCase(this.kind)) {
            if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
                Pattern p = PatternHandler.getInstance().getOrRegisterPattern(this.data);
                if (p != null && p.matcher(mc.getCurrentServerData().serverIP).matches()) {
                    return true;
                }
            }
        }

        if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null) {
            String ip = mc.getCurrentServerData().serverIP.toLowerCase().trim();
            if (ip.contains(":")) {
                ip = ip.split(":")[0];
            }
            if (ip.equals("hypixel.net") || ip.endsWith(".hypixel.net")) {
                return true;
            }
        }

        return false;
    }
}
