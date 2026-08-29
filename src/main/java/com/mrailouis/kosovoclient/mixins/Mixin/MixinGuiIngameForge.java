package com.mrailouis.kosovoclient.mixins.Mixin;

import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import net.minecraftforge.client.GuiIngameForge;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GuiIngameForge.class, remap = false)
public abstract class MixinGuiIngameForge {

    @ModifyVariable(method = "renderHealth", at = @At(value = "LOAD", opcode = Opcodes.ILOAD, ordinal = 1), index = 5)
    private boolean cancelHealthFlash(boolean original) {
        OldAnimations mod = OldAnimations.getInstance();
        return original && !(mod.isEnabled() && mod.getHealthBarFlashRemoval().isEnabled());
    }
}
