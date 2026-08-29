package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.chat.InfiniteChat;
import com.mrailouis.kosovoclient.features.impl.chat.SpamFilter;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {

    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"), cancellable = true)
    private void onPrintChatMessage(IChatComponent chatComponent, int chatLineId, CallbackInfo ci) {
        if (SpamFilter.getInstance().isEnabled() && chatLineId == 0) {
            ci.cancel();
            SpamFilter.getInstance().handleChatMessage((GuiNewChat) (Object) this, chatComponent);
        }
    }

    @ModifyConstant(method = "setChatLine", constant = @Constant(intValue = 100))
    private int modifyMaxChatLines(int original) {
        if (InfiniteChat.getInstance().isEnabled()) {
            return InfiniteChat.getInstance().getMaxLinesLimit();
        }
        return original;
    }

    @Inject(method = "clearChatMessages", at = @At("HEAD"))
    private void onClearChat(CallbackInfo ci) {
        SpamFilter.getInstance().reset();
    }
}
