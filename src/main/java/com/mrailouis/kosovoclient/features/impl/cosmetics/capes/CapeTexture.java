package com.mrailouis.kosovoclient.features.impl.cosmetics.capes;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

@Getter
@Setter
public class CapeTexture {
    private final String name;
    private final ResourceLocation[] frameLocations;
    private final int[] frameDelays;
    private final int textureWidth;
    private final int textureHeight;
    private final boolean animated;
    private int currentFrame;
    private long lastFrameTime;

    public CapeTexture(String name, ResourceLocation singleLocation, int width, int height) {
        this.name = name;
        this.frameLocations = new ResourceLocation[]{singleLocation};
        this.frameDelays = new int[]{100};
        this.textureWidth = width;
        this.textureHeight = height;
        this.animated = false;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public CapeTexture(String name, ResourceLocation[] frameLocations, int[] frameDelays, int width, int height) {
        this.name = name;
        this.frameLocations = frameLocations;
        this.frameDelays = frameDelays;
        this.textureWidth = width;
        this.textureHeight = height;
        this.animated = frameLocations != null && frameLocations.length > 1;
        this.currentFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public ResourceLocation getActiveLocation() {
        if (!this.animated || this.frameLocations == null || this.frameLocations.length == 0) {
            return this.frameLocations != null && this.frameLocations.length > 0 ? this.frameLocations[0] : null;
        }
        updateAnimation();
        return this.frameLocations[this.currentFrame];
    }

    private void updateAnimation() {
        if (this.frameLocations.length <= 1) {
            return;
        }
        long now = System.currentTimeMillis();
        int delay = (this.frameDelays != null && this.currentFrame < this.frameDelays.length) ? this.frameDelays[this.currentFrame] : 100;
        if (delay <= 0) {
            delay = 100;
        }
        if (now - this.lastFrameTime >= delay) {
            this.currentFrame = (this.currentFrame + 1) % this.frameLocations.length;
            this.lastFrameTime = now;
        }
    }

    public float[] getUVBounds() {
        float tw = this.textureWidth > 0 ? (float) this.textureWidth : 64.0f;
        float th = this.textureHeight > 0 ? (float) this.textureHeight : 32.0f;

        float ratio = tw / th;
        if (Math.abs(ratio - (46.0f / 22.0f)) < 0.05f || (tw == 92.0f && th == 44.0f) || (tw == 46.0f && th == 22.0f)) {
            float scaleX = tw / 46.0f;
            float scaleY = th / 22.0f;
            return new float[]{
                    0.0f,
                    scaleX / tw,
                    (1.0f + 10.0f) * scaleX / tw,
                    (1.0f + 10.0f + 1.0f) * scaleX / tw,
                    (1.0f + 10.0f + 1.0f + 10.0f) * scaleX / tw,
                    0.0f,
                    scaleY / th,
                    (1.0f + 16.0f) * scaleY / th
            };
        }

        if (Math.abs(ratio - 2.0f) < 0.05f || (tw == 64.0f && th == 32.0f)) {
            float scaleX = tw / 64.0f;
            float scaleY = th / 32.0f;
            return new float[]{
                    0.0f,
                    scaleX / tw,
                    11.0f * scaleX / tw,
                    12.0f * scaleX / tw,
                    22.0f * scaleX / tw,
                    0.0f,
                    scaleY / th,
                    17.0f * scaleY / th
            };
        }

        return new float[]{
                0.0f,
                0.045f,
                0.50f,
                0.545f,
                1.0f,
                0.0f,
                0.06f,
                1.0f
        };
    }
}
