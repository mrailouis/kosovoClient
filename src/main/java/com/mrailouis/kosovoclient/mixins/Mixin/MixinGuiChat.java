package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.chat.CopyChat;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiChat.class)
public abstract class MixinGuiChat {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onChatMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (CopyChat.getInstance().handleChatClick(mouseX, mouseY, mouseButton)) {
            ci.cancel();
        }
    }
}
