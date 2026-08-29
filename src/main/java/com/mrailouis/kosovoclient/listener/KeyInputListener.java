package com.mrailouis.kosovoclient.listener;

import com.mrailouis.kosovoclient.features.impl.visuals.Zoom;
import com.mrailouis.kosovoclient.gui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyInputListener {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_RSHIFT) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ClickGuiScreen());
            }
        }
        Zoom.getInstance().onKeyInput();
    }
}
