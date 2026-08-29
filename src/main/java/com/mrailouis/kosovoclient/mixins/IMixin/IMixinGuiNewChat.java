package com.mrailouis.kosovoclient.mixins.IMixin;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiNewChat.class)
public interface IMixinGuiNewChat {

    @Accessor("drawnChatLines")
    List<ChatLine> getDrawnChatLines();

    @Accessor("chatLines")
    List<ChatLine> getChatLines();

    @Accessor("scrollPos")
    int getScrollPos();

    @Invoker("setChatLine")
    void invokeSetChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly);
}
