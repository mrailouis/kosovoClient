package com.mrailouis.kosovoclient.features.impl.chat;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.mixins.IMixin.IMixinGuiNewChat;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@Getter
public class SpamFilter extends Module {
    private static final SpamFilter INSTANCE = new SpamFilter();

    private final NumberSetting threshold = new NumberSetting("Threshold", "Max seconds between duplicate messages to compact.", 10.0, 1.0, 60.0, 1.0);
    private final ModeSetting format = new ModeSetting("Format", "Style of compact counter indicator.", "[xN]", new String[]{"[xN]", "(xN)", "[N]", "(N)"});
    private final BooleanSetting showCount = new BooleanSetting("Show Count", "Display the count indicator next to duplicate messages.", true);

    private String lastMessageText;
    private long lastMessageTime;
    private int duplicateCount;
    private int currentLineId = 10000;

    public static SpamFilter getInstance() {
        return INSTANCE;
    }

    private SpamFilter() {
        super("Spam Filter", "Compacts identical chat messages within a time threshold.", Category.CHAT, true);
        registerSetting(this.threshold);
        registerSetting(this.format);
        registerSetting(this.showCount);
    }

    public void handleChatMessage(GuiNewChat guiNewChat, IChatComponent chatComponent) {
        String unformatted = chatComponent.getUnformattedText();
        long now = System.currentTimeMillis();
        long thresholdMs = (long) (this.threshold.getValue() * 1000.0);

        if (this.lastMessageText != null
                && this.lastMessageText.equals(unformatted)
                && (now - this.lastMessageTime) <= thresholdMs) {
            this.duplicateCount++;
            this.lastMessageTime = now;

            IChatComponent compactComponent = chatComponent.createCopy();
            if (this.showCount.isEnabled()) {
                compactComponent.appendSibling(new ChatComponentText(getFormattedCount(this.duplicateCount)));
            }

            ((IMixinGuiNewChat) guiNewChat).invokeSetChatLine(
                    compactComponent,
                    this.currentLineId,
                    Minecraft.getMinecraft().ingameGUI.getUpdateCounter(),
                    false
            );
        } else {
            this.lastMessageText = unformatted;
            this.lastMessageTime = now;
            this.duplicateCount = 1;
            this.currentLineId = getNextLineId();

            ((IMixinGuiNewChat) guiNewChat).invokeSetChatLine(
                    chatComponent,
                    this.currentLineId,
                    Minecraft.getMinecraft().ingameGUI.getUpdateCounter(),
                    false
            );
        }
    }

    private int getNextLineId() {
        this.currentLineId++;
        if (this.currentLineId > 500000) {
            this.currentLineId = 10000;
        }
        return this.currentLineId;
    }

    private String getFormattedCount(int count) {
        String fmt = this.format.getValue();
        if ("(xN)".equalsIgnoreCase(fmt)) {
            return " §8(§6x" + count + "§8)";
        } else if ("[N]".equalsIgnoreCase(fmt)) {
            return " §8[§6" + count + "§8]";
        } else if ("(N)".equalsIgnoreCase(fmt)) {
            return " §8(§6" + count + "§8)";
        }
        return " §8[§6x" + count + "§8]";
    }

    public void reset() {
        this.lastMessageText = null;
        this.lastMessageTime = 0L;
        this.duplicateCount = 0;
    }
}
