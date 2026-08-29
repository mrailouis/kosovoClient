package com.mrailouis.kosovoclient.features.impl.chat;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.mixins.IMixin.IMixinGuiNewChat;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

@Getter
public class CopyChat extends Module {
    private static final CopyChat INSTANCE = new CopyChat();

    private final ModeSetting modifier = new ModeSetting("Modifier", "Key modifier required to copy chat message.", "Shift", new String[]{"Shift", "Control", "Alt", "None"});
    private final ModeSetting button = new ModeSetting("Click", "Mouse click button to trigger copy.", "Right", new String[]{"Right", "Left", "Middle"});
    private final BooleanSetting stripFormatting = new BooleanSetting("Strip Formatting", "Remove color and formatting codes from copied text.", true);
    private final BooleanSetting copyFullMessage = new BooleanSetting("Copy Full Message", "Copy entire message even if split across lines.", true);
    private final BooleanSetting notification = new BooleanSetting("Notification", "Show feedback notification when copied.", true);

    public static CopyChat getInstance() {
        return INSTANCE;
    }

    private CopyChat() {
        super("Copy Chat", "Click chat messages with a modifier key to copy them to clipboard.", Category.CHAT, true);
        registerSetting(this.modifier);
        registerSetting(this.button);
        registerSetting(this.stripFormatting);
        registerSetting(this.copyFullMessage);
        registerSetting(this.notification);
    }

    public boolean handleChatClick(int mouseX, int mouseY, int mouseButton) {
        if (!isEnabled()) {
            return false;
        }

        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean ctrl = GuiScreen.isCtrlKeyDown();
        boolean alt = GuiScreen.isAltKeyDown();

        String mod = this.modifier.getValue();
        boolean modMatches = false;
        if ("Shift".equalsIgnoreCase(mod)) {
            modMatches = shift;
        } else if ("Control".equalsIgnoreCase(mod)) {
            modMatches = ctrl;
        } else if ("Alt".equalsIgnoreCase(mod)) {
            modMatches = alt;
        } else if ("None".equalsIgnoreCase(mod)) {
            modMatches = !shift && !ctrl && !alt;
        }

        if (!modMatches) {
            return false;
        }

        String btn = this.button.getValue();
        boolean btnMatches = false;
        if ("Left".equalsIgnoreCase(btn) && mouseButton == 0) {
            btnMatches = true;
        } else if ("Right".equalsIgnoreCase(btn) && mouseButton == 1) {
            btnMatches = true;
        } else if ("Middle".equalsIgnoreCase(btn) && mouseButton == 2) {
            btnMatches = true;
        }

        if (!btnMatches) {
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
        if (!chatGui.getChatOpen()) {
            return false;
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();
        float chatScale = chatGui.getChatScale();
        int j = Mouse.getX() / scaleFactor - 3;
        int k = Mouse.getY() / scaleFactor - 27;
        j = MathHelper.floor_float((float) j / chatScale);
        k = MathHelper.floor_float((float) k / chatScale);

        if (j < 0 || k < 0) {
            return false;
        }

        IMixinGuiNewChat mixinChat = (IMixinGuiNewChat) chatGui;
        List<ChatLine> drawnLines = mixinChat.getDrawnChatLines();
        int l = Math.min(chatGui.getLineCount(), drawnLines.size());

        if (j <= MathHelper.floor_float((float) chatGui.getChatWidth() / chatScale) && k < mc.fontRendererObj.FONT_HEIGHT * l + l) {
            int m = k / mc.fontRendererObj.FONT_HEIGHT + mixinChat.getScrollPos();
            if (m >= 0 && m < drawnLines.size()) {
                ChatLine clickedLine = drawnLines.get(m);
                if (clickedLine != null) {
                    String textToCopy = null;
                    if (this.copyFullMessage.isEnabled()) {
                        List<ChatLine> fullLines = mixinChat.getChatLines();
                        for (ChatLine parent : fullLines) {
                            if (parent.getUpdatedCounter() == clickedLine.getUpdatedCounter()
                                    && (parent.getChatLineID() == clickedLine.getChatLineID() || clickedLine.getChatLineID() == 0)) {
                                textToCopy = this.stripFormatting.isEnabled()
                                        ? parent.getChatComponent().getUnformattedText()
                                        : parent.getChatComponent().getFormattedText();
                                break;
                            }
                        }
                    }

                    if (textToCopy == null) {
                        textToCopy = this.stripFormatting.isEnabled()
                                ? clickedLine.getChatComponent().getUnformattedText()
                                : clickedLine.getChatComponent().getFormattedText();
                    }

                    if (textToCopy != null && !textToCopy.isEmpty()) {
                        if (this.stripFormatting.isEnabled()) {
                            textToCopy = EnumChatFormatting.getTextWithoutFormattingCodes(textToCopy);
                        }
                        GuiScreen.setClipboardString(textToCopy);
                        if (this.notification.isEnabled() && mc.thePlayer != null) {
                            mc.thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fCopied message to clipboard!"));
                        }
                        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
