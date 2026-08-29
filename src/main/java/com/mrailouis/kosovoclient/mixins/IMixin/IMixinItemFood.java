package com.mrailouis.kosovoclient.mixins.IMixin;

import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFood.class)
public interface IMixinItemFood {
    @Accessor("alwaysEdible")
    boolean isAlwaysEdible();
}
