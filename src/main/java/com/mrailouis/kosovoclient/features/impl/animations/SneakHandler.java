package com.mrailouis.kosovoclient.features.impl.animations;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class SneakHandler {
    private static final float START_HEIGHT = 1.62f;
    private static final float END_HEIGHT = 1.54f;

    @Getter
    private static final SneakHandler instance = new SneakHandler();

    private float eyeHeight = START_HEIGHT;
    private float lastEyeHeight = START_HEIGHT;

    public float getEyeHeight(float partialTicks) {
        OldAnimations mod = OldAnimations.getInstance();
        if (!mod.isEnabled() || !mod.getSmoothSneaking().isEnabled()) {
            return this.eyeHeight;
        }

        return this.lastEyeHeight + (this.eyeHeight - this.lastEyeHeight) * partialTicks;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            this.lastEyeHeight = this.eyeHeight;

            final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            if (player == null) {
                this.eyeHeight = START_HEIGHT;
                return;
            }

            OldAnimations mod = OldAnimations.getInstance();
            if (player.isSneaking()) {
                this.eyeHeight = END_HEIGHT;
            } else if (!mod.isEnabled() || !mod.getLongerUnsneak().isEnabled()) {
                this.eyeHeight = START_HEIGHT;
            } else if (this.eyeHeight < START_HEIGHT) {
                float delta = START_HEIGHT - this.eyeHeight;
                delta *= 0.4f;
                this.eyeHeight = START_HEIGHT - delta;
            }
        }
    }
}
