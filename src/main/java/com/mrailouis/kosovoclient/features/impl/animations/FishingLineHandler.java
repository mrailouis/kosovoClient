package com.mrailouis.kosovoclient.features.impl.animations;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public class FishingLineHandler {
    @Getter
    private static final FishingLineHandler instance = new FishingLineHandler();

    public Vec3 getOffset() {
        double fov = Minecraft.getMinecraft().gameSettings.fovSetting;
        double decimalFov = fov / 110.0;
        return new Vec3(((-decimalFov + (decimalFov / 2.5)) - (decimalFov / 8.0)) + 0.16, 0.0, 0.4);
    }
}
