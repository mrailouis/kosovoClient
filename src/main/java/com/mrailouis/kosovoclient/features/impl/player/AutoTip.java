package com.mrailouis.kosovoclient.features.impl.player;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Getter
public class AutoTip extends Module {
    private static final AutoTip INSTANCE = new AutoTip();

    private final NumberSetting intervalMinutes = new NumberSetting("Interval (Mins)", "Time in minutes between sending /tip all.", 5.0, 1.0, 60.0, 1.0);
    private final BooleanSetting feedback = new BooleanSetting("Feedback", "Display client notification message when tipping.", true);

    private long lastTipTime = 0L;

    public static AutoTip getInstance() {
        return INSTANCE;
    }

    private AutoTip() {
        super("Auto Tip", "Automatically executes /tip all on Hypixel on a configurable timer.", Category.PLAYER, true);
        registerSetting(this.intervalMinutes);
        registerSetting(this.feedback);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (!isHypixel(mc)) {
            return;
        }

        long now = System.currentTimeMillis();
        long intervalMs = (long) (this.intervalMinutes.getValue() * 60.0 * 1000.0);

        if (this.lastTipTime == 0L) {
            this.lastTipTime = now;
            return;
        }

        if (now - this.lastTipTime >= intervalMs) {
            this.lastTipTime = now;
            mc.thePlayer.sendChatMessage("/tip all");
            if (this.feedback.isEnabled()) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fAutomatically sent /tip all!"));
            }
        }
    }

    private boolean isHypixel(Minecraft mc) {
        if (mc.isSingleplayer()) {
            return false;
        }

        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null || serverData.serverIP == null) {
            return false;
        }

        String ip = serverData.serverIP.toLowerCase().trim();
        if (ip.contains(":")) {
            ip = ip.split(":")[0];
        }

        return ip.equals("hypixel.net") || ip.endsWith(".hypixel.net");
    }
}
