package com.mrailouis.kosovoclient.render;

import com.mrailouis.kosovoclient.util.NativeLoader;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.io.IOUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class NanoVGManager {

    @Getter
    private static final NanoVGManager instance = new NanoVGManager();

    public static final String FONT_INTER = "Inter";
    public static final String FONT_INTER_BOLD = "InterBold";
    public static final String FONT_SPACE_GROTESK = "SpaceGrotesk";

    private long vg = 0;
    private NVGColor color;
    private NVGColor color1;
    private NVGColor color2;
    private NVGPaint paint;
    private final Map<String, ByteBuffer> fontBuffers = new HashMap<String, ByteBuffer>();
    private final Map<String, Integer> imageCache = new HashMap<String, Integer>();
    private final Map<String, ByteBuffer> imageBuffers = new HashMap<String, ByteBuffer>();

    private int svImage = 0;
    private float lastSVHue = -1.0f;
    private final ByteBuffer svBuffer = ByteBuffer.allocateDirect(64 * 64 * 4).order(ByteOrder.nativeOrder());

    private int hueImage = 0;
    private final ByteBuffer hueBuffer = ByteBuffer.allocateDirect(360 * 1 * 4).order(ByteOrder.nativeOrder());

    private int previousProgram;
    private int previousArrayBuffer;
    private int previousElementBuffer;
    private int previousActiveTexture;

    public void init() {
        if (vg == 0) {
            NativeLoader.load();
            vg = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS);
            color = NVGColor.create();
            color1 = NVGColor.create();
            color2 = NVGColor.create();
            paint = NVGPaint.create();
            loadFont(FONT_INTER, "assets/kosovoclient/fonts/Inter-Regular.ttf");
            loadFont(FONT_INTER_BOLD, "assets/kosovoclient/fonts/Inter-Bold.ttf");
            loadFont(FONT_SPACE_GROTESK, "assets/kosovoclient/fonts/SpaceGrotesk-Regular.ttf");
        }
    }

    @SneakyThrows
    private void loadFont(String name, String resourcePath) {
        InputStream is = NanoVGManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            byte[] bytes = IOUtils.toByteArray(is);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes);
            buffer.flip();
            fontBuffers.put(name, buffer);
            NanoVG.nvgCreateFontMem(vg, name, buffer, false);
        }
    }

    @SneakyThrows
    public int getImage(String resourcePath) {
        init();
        if (imageCache.containsKey(resourcePath)) {
            return imageCache.get(resourcePath);
        }

        InputStream is = NanoVGManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            byte[] bytes = IOUtils.toByteArray(is);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            buffer.put(bytes);
            buffer.flip();
            imageBuffers.put(resourcePath, buffer);
            int img = NanoVG.nvgCreateImageMem(vg, NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, buffer);
            imageCache.put(resourcePath, img);
            return img;
        }

        return -1;
    }

    public void beginFrame(float width, float height, float pixelRatio) {
        init();
        previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        previousElementBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
        NanoVG.nvgBeginFrame(vg, width, height, pixelRatio);
    }

    public void endFrame() {
        NanoVG.nvgEndFrame(vg);
        GL11.glPopClientAttrib();
        GL11.glPopAttrib();

        GL20.glUseProgram(previousProgram);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, previousElementBuffer);
        GL13.glActiveTexture(previousActiveTexture);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void drawRect(float x, float y, float width, float height, int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, x, y, width, height);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    public void drawCircle(float cx, float cy, float radius, int hexColor) {
        if (radius <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy, radius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    public void drawCircleOutline(float cx, float cy, float radius, float strokeWidth, int hexColor) {
        if (radius <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy, radius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgStroke(vg);
    }

    public void drawRoundedRect(float x, float y, float width, float height, float radius, int hexColor) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        float maxRadius = Math.min(width, height) / 2.0f;
        float actualRadius = Math.max(0.0f, Math.min(radius, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, actualRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    public void drawRoundedRectVarying(float x, float y, float width, float height, float radTopLeft, float radTopRight, float radBottomRight, float radBottomLeft, int hexColor) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        float maxRadius = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radTopLeft, maxRadius));
        float rtr = Math.max(0.0f, Math.min(radTopRight, maxRadius));
        float rbr = Math.max(0.0f, Math.min(radBottomRight, maxRadius));
        float rbl = Math.max(0.0f, Math.min(radBottomLeft, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, rtl, rtr, rbr, rbl);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    public void drawLine(float x1, float y1, float x2, float y2, float strokeWidth, int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, x1, y1);
        NanoVG.nvgLineTo(vg, x2, y2);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgStroke(vg);
    }

    public void drawRoundedRectOutline(float x, float y, float width, float height, float radius, float strokeWidth, int hexColor) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        float maxRadius = Math.min(width, height) / 2.0f;
        float actualRadius = Math.max(0.0f, Math.min(radius, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, actualRadius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgStroke(vg);
    }

    public void drawRoundedRectVaryingOutline(float x, float y, float width, float height, float radTopLeft, float radTopRight, float radBottomRight, float radBottomLeft, float strokeWidth, int hexColor) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        float maxRadius = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radTopLeft, maxRadius));
        float rtr = Math.max(0.0f, Math.min(radTopRight, maxRadius));
        float rbr = Math.max(0.0f, Math.min(radBottomRight, maxRadius));
        float rbl = Math.max(0.0f, Math.min(radBottomLeft, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, rtl, rtr, rbr, rbl);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgStroke(vg);
    }

    public void drawGlassPanel(float x, float y, float width, float height, float radius, int fillColor, int strokeColor) {
        drawRoundedRect(x, y, width, height, radius, fillColor);

        float r = ((strokeColor >> 16) & 0xFF) / 255.0f;
        float g = ((strokeColor >> 8) & 0xFF) / 255.0f;
        float b = (strokeColor & 0xFF) / 255.0f;
        float a = ((strokeColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (strokeColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, 1.0f);
        NanoVG.nvgStroke(vg);
    }

    /**
     * Plain, unshadowed text - matches native Windows 7 (Segoe UI) rendering. Do not add a
     * drop-shadow/outline pass here; that is what made earlier iterations read as a Minecraft HUD.
     */
    public void drawText(String text, float x, float y, String fontName, float fontSize, int hexColor, int alignment) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgFontFace(vg, fontName);
        NanoVG.nvgFontSize(vg, fontSize);
        NanoVG.nvgTextAlign(vg, alignment);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgText(vg, x, y, text);
    }

    public float getTextWidth(String text, String fontName, float fontSize) {
        NanoVG.nvgFontFace(vg, fontName);
        NanoVG.nvgFontSize(vg, fontSize);
        float[] bounds = new float[4];
        return NanoVG.nvgTextBounds(vg, 0.0f, 0.0f, text, bounds);
    }

    public void drawImage(int image, float x, float y, float width, float height, float alpha) {
        if (image < 0) {
            return;
        }
        NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0f, image, alpha, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, x, y, width, height);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    public void intersectScissor(float x, float y, float width, float height) {
        NanoVG.nvgIntersectScissor(vg, x, y, width, height);
    }

    public void scissor(float x, float y, float width, float height) {
        NanoVG.nvgScissor(vg, x, y, width, height);
    }

    public void resetScissor() {
        NanoVG.nvgResetScissor(vg);
    }

    public void drawPillToggle(float x, float y, float width, float height, float progress, int activeColor, int inactiveColor, int knobColor) {
        float radius = height / 2.0f;
        int bgColor = interpolateColor(inactiveColor, activeColor, progress);

        drawRoundedRect(x, y, width, height, radius, bgColor);
        drawRoundedRectOutline(x, y, width, height, radius, 0.75f, 0x22FFFFFF);

        float knobPadding = 2.0f;
        float knobRadius = radius - knobPadding;
        float minKnobX = x + knobPadding + knobRadius;
        float maxKnobX = x + width - knobPadding - knobRadius;
        float knobX = minKnobX + (maxKnobX - minKnobX) * progress;
        float knobY = y + height / 2.0f;

        float r = ((knobColor >> 16) & 0xFF) / 255.0f;
        float g = ((knobColor >> 8) & 0xFF) / 255.0f;
        float b = (knobColor & 0xFF) / 255.0f;
        float a = ((knobColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (knobColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(0.0f);
        color.g(0.0f);
        color.b(0.0f);
        color.a(a * 0.35f);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, knobX, knobY + 1.0f, knobRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, knobX, knobY, knobRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    public void drawChevron(float cx, float cy, float size, float progress, int hexColor, float strokeWidth) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgSave(vg);
        NanoVG.nvgTranslate(vg, cx, cy);
        NanoVG.nvgRotate(vg, (float) Math.toRadians(progress * 90.0));

        float s = size * 0.45f;
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, -s * 0.6f, -s);
        NanoVG.nvgLineTo(vg, s * 0.4f, 0.0f);
        NanoVG.nvgLineTo(vg, -s * 0.6f, s);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgLineCap(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgLineJoin(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgStroke(vg);

        NanoVG.nvgRestore(vg);
    }

    public void drawSlider(float x, float y, float width, float height, float progress, int trackColor, int activeTrackColor, int thumbColor) {
        float radius = height / 2.0f;
        drawRoundedRect(x, y, width, height, radius, trackColor);

        float activeWidth = Math.max(height, width * Math.max(0.0f, Math.min(1.0f, progress)));
        drawRoundedRect(x, y, activeWidth, height, radius, activeTrackColor);

        float thumbX = x + width * Math.max(0.0f, Math.min(1.0f, progress));
        float thumbY = y + height / 2.0f;
        float thumbRadius = height * 0.75f;

        float r = ((thumbColor >> 16) & 0xFF) / 255.0f;
        float g = ((thumbColor >> 8) & 0xFF) / 255.0f;
        float b = (thumbColor & 0xFF) / 255.0f;
        float a = ((thumbColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (thumbColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(0.0f);
        color.g(0.0f);
        color.b(0.0f);
        color.a(a * 0.35f);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, thumbX, thumbY + 1.0f, thumbRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, thumbX, thumbY, thumbRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }

    private int interpolateColor(int color1, int color2, float factor) {
        float f = Math.max(0.0f, Math.min(1.0f, factor));
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * f);
        int r = (int) (r1 + (r2 - r1) * f);
        int g = (int) (g1 + (g2 - g1) * f);
        int b = (int) (b1 + (b2 - b1) * f);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void drawVectorSearchIcon(float x, float y, float size, int hexColor, float strokeWidth) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

        float cx = x + size * 0.40f;
        float cy = y + size * 0.40f;
        float radius = size * 0.28f;

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy, radius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgStroke(vg);

        float handleStartX = cx + radius * 0.7071f;
        float handleStartY = cy + radius * 0.7071f;
        float handleEndX = x + size * 0.88f;
        float handleEndY = y + size * 0.88f;

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, handleStartX, handleStartY);
        NanoVG.nvgLineTo(vg, handleEndX, handleEndY);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgLineCap(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgStroke(vg);
    }

    public void drawSVBox(float x, float y, float width, float height, float radius, float hue) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }

        if (svImage == 0) {
            for (int sy = 0; sy < 64; sy++) {
                float v = 1.0f - (sy / 63.0f);
                for (int sx = 0; sx < 64; sx++) {
                    float s = sx / 63.0f;
                    int rgb = java.awt.Color.HSBtoRGB(hue, s, v);
                    int offset = (sy * 64 + sx) * 4;
                    svBuffer.put(offset, (byte) ((rgb >> 16) & 0xFF));
                    svBuffer.put(offset + 1, (byte) ((rgb >> 8) & 0xFF));
                    svBuffer.put(offset + 2, (byte) (rgb & 0xFF));
                    svBuffer.put(offset + 3, (byte) 0xFF);
                }
            }
            svBuffer.position(0);
            svImage = NanoVG.nvgCreateImageRGBA(vg, 64, 64, NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, svBuffer);
            lastSVHue = hue;
        } else if (Math.abs(hue - lastSVHue) > 0.0001f) {
            for (int sy = 0; sy < 64; sy++) {
                float v = 1.0f - (sy / 63.0f);
                for (int sx = 0; sx < 64; sx++) {
                    float s = sx / 63.0f;
                    int rgb = java.awt.Color.HSBtoRGB(hue, s, v);
                    int offset = (sy * 64 + sx) * 4;
                    svBuffer.put(offset, (byte) ((rgb >> 16) & 0xFF));
                    svBuffer.put(offset + 1, (byte) ((rgb >> 8) & 0xFF));
                    svBuffer.put(offset + 2, (byte) (rgb & 0xFF));
                    svBuffer.put(offset + 3, (byte) 0xFF);
                }
            }
            svBuffer.position(0);
            NanoVG.nvgUpdateImage(vg, svImage, svBuffer);
            lastSVHue = hue;
        }

        NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0f, svImage, 1.0f, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    public void drawHueBar(float x, float y, float width, float height, float radius) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }

        if (hueImage == 0) {
            for (int i = 0; i < 360; i++) {
                float h = i / 359.0f;
                int rgb = java.awt.Color.HSBtoRGB(h, 1.0f, 1.0f);
                int offset = i * 4;
                hueBuffer.put(offset, (byte) ((rgb >> 16) & 0xFF));
                hueBuffer.put(offset + 1, (byte) ((rgb >> 8) & 0xFF));
                hueBuffer.put(offset + 2, (byte) (rgb & 0xFF));
                hueBuffer.put(offset + 3, (byte) 0xFF);
            }
            hueBuffer.position(0);
            hueImage = NanoVG.nvgCreateImageRGBA(vg, 360, 1, NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, hueBuffer);
        }

        NanoVG.nvgImagePattern(vg, x, y, width, height, 0.0f, hueImage, 1.0f, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    public void drawAlphaBar(float x, float y, float width, float height, float radius, int rgbColor) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }

        drawRoundedRect(x, y, width, height, radius, 0x33000000);

        float r = ((rgbColor >> 16) & 0xFF) / 255.0f;
        float g = ((rgbColor >> 8) & 0xFF) / 255.0f;
        float b = (rgbColor & 0xFF) / 255.0f;

        color1.r(r).g(g).b(b).a(0.0f);
        color2.r(r).g(g).b(b).a(1.0f);

        NanoVG.nvgLinearGradient(vg, x - 1.0f, y, x + width + 1.0f, y, color1, color2, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    private void setColorExact(NVGColor target, int hexColor) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >>> 24) & 0xFF) / 255.0f;
        target.r(r);
        target.g(g);
        target.b(b);
        target.a(a);
    }

    public static int withAlpha(int hexColor, float alpha) {
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        return (a << 24) | (hexColor & 0x00FFFFFF);
    }

    /**
     * Fills a rounded rect with a top-to-bottom linear gradient. Colors are exact ARGB
     * (an explicit 0x00 alpha stays fully transparent) - used for Aero glass/gloss effects.
     */
    public void drawVerticalGradientRounded(float x, float y, float width, float height,
                                             float radTopLeft, float radTopRight, float radBottomRight, float radBottomLeft,
                                             int topColorArgb, int bottomColorArgb) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        setColorExact(color1, topColorArgb);
        setColorExact(color2, bottomColorArgb);
        NanoVG.nvgLinearGradient(vg, x, y, x, y + height, color1, color2, paint);

        float maxRadius = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radTopLeft, maxRadius));
        float rtr = Math.max(0.0f, Math.min(radTopRight, maxRadius));
        float rbr = Math.max(0.0f, Math.min(radBottomRight, maxRadius));
        float rbl = Math.max(0.0f, Math.min(radBottomLeft, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, rtl, rtr, rbr, rbl);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    public void drawVerticalGradientRounded(float x, float y, float width, float height, float radius, int topColorArgb, int bottomColorArgb) {
        drawVerticalGradientRounded(x, y, width, height, radius, radius, radius, radius, topColorArgb, bottomColorArgb);
    }

    /**
     * Soft outer glow/drop-shadow behind a rounded rect, e.g. the Aero window glow.
     * Uses the classic NanoVG box-gradient-with-hole technique.
     */
    public void drawOuterGlow(float x, float y, float width, float height, float radius, float feather, int glowColorArgb) {
        setColorExact(color1, glowColorArgb);
        setColorExact(color2, withAlpha(glowColorArgb, 0.0f));
        NanoVG.nvgBoxGradient(vg, x, y, width, height, radius, feather, color1, color2, paint);

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, x - feather * 2.0f, y - feather * 2.0f, width + feather * 4.0f, height + feather * 4.0f);
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius);
        NanoVG.nvgPathWinding(vg, NanoVG.NVG_HOLE);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    /**
     * The glassy "shine" strip across the top portion of an Aero surface (title bar, button).
     */
    public void drawGlossHighlight(float x, float y, float width, float height, float radTopLeft, float radTopRight, float peakAlpha) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        setColorExact(color1, withAlpha(0xFFFFFFFF, peakAlpha));
        setColorExact(color2, withAlpha(0xFFFFFFFF, 0.0f));
        NanoVG.nvgLinearGradient(vg, x, y, x, y + height, color1, color2, paint);

        float maxRadius = Math.min(width, height) / 2.0f;
        float rtl = Math.max(0.0f, Math.min(radTopLeft, maxRadius));
        float rtr = Math.max(0.0f, Math.min(radTopRight, maxRadius));

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRoundedRectVarying(vg, x, y, width, height, rtl, rtr, 0.0f, 0.0f);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    /**
     * A classic Windows 7 chrome button: vertical gradient fill, glossy top-half shine and a
     * thin outline. Used for Edit HUDs / mode / keybind / done buttons.
     */
    public void drawAeroButton(float x, float y, float width, float height, float radius,
                                int topColorArgb, int bottomColorArgb, int borderColorArgb, float glossAlpha) {
        drawVerticalGradientRounded(x, y, width, height, radius, topColorArgb, bottomColorArgb);
        if (glossAlpha > 0.0f) {
            drawGlossHighlight(x + 1.0f, y + 1.0f, width - 2.0f, (height - 2.0f) * 0.5f, Math.max(0.0f, radius - 1.0f), Math.max(0.0f, radius - 1.0f), glossAlpha);
        }
        drawRoundedRectOutline(x, y, width, height, radius, 1.0f, borderColorArgb);
    }

    /**
     * Windows 7 Aero title bar: blue vertical gradient with a glassy shine band on top.
     */
    public void drawAeroTitleBar(float x, float y, float width, float height, float radTopLeft, float radTopRight,
                                  int topColorArgb, int bottomColorArgb) {
        drawAeroTitleBar(x, y, width, height, radTopLeft, radTopRight, topColorArgb, bottomColorArgb, bottomColorArgb);
    }

    /**
     * Three-layer Aero glass: a pale translucent top, a mid-tone belly and a stronger blue base,
     * with a glassy shine band and a 1px bright highlight just under the top edge. The base fill
     * uses a translucent top color so the world behind the GUI faintly shows through the glass.
     */
    public void drawAeroTitleBar(float x, float y, float width, float height, float radTopLeft, float radTopRight,
                                  int topColorArgb, int midColorArgb, int bottomColorArgb) {
        drawVerticalGradientRounded(x, y, width, height, radTopLeft, radTopRight, 0.0f, 0.0f, topColorArgb, bottomColorArgb);

        float midY = y + height * 0.32f;
        setColorExact(color1, midColorArgb);
        setColorExact(color2, bottomColorArgb);
        NanoVG.nvgLinearGradient(vg, x, midY, x, y + height, color1, color2, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, x, midY, width, (y + height) - midY);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);

        drawGlossHighlight(x, y, width, height * 0.50f, radTopLeft, radTopRight, 0.38f);
        drawLine(x + 1.0f, y + 1.0f, x + width - 1.0f, y + 1.0f, 1.0f, withAlpha(0xFFFFFFFF, 0.55f));
    }

    public void setGlobalAlpha(float alpha) {
        NanoVG.nvgGlobalAlpha(vg, Math.max(0.0f, Math.min(1.0f, alpha)));
    }

    /**
     * A Windows 7 style square checkbox: light bevel fill, grey border, blue tick that
     * fades in with `progress` (drive this with the same enable/toggle progress used elsewhere).
     */
    public void drawWin7Checkbox(float x, float y, float size, float progress, boolean hovered) {
        int topC = hovered ? 0xFFFFFFFF : 0xFFFCFCFC;
        int botC = hovered ? 0xFFE9F4FE : 0xFFE9E9E9;
        int borderC = hovered ? 0xFF7EB4EA : 0xFF8C9BA8;
        drawVerticalGradientRounded(x, y, size, size, 2.0f, topC, botC);
        drawRoundedRectOutline(x, y, size, size, 2.0f, 1.0f, borderC);
        if (progress > 0.01f) {
            drawCheckmark(x + size / 2.0f, y + size / 2.0f + size * 0.02f, size, 0xFF1E6FC7, Math.max(1.0f, size * 0.16f), progress);
        }
    }

    public void drawCheckmark(float cx, float cy, float size, int hexColor, float strokeWidth, float alpha) {
        if (alpha <= 0.001f) {
            return;
        }
        setColorExact(color, withAlpha(hexColor, alpha));
        float s = size * 0.5f;
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, cx - s * 0.85f, cy + s * 0.05f);
        NanoVG.nvgLineTo(vg, cx - s * 0.20f, cy + s * 0.65f);
        NanoVG.nvgLineTo(vg, cx + s * 0.95f, cy - s * 0.55f);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, strokeWidth);
        NanoVG.nvgLineCap(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgLineJoin(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgStroke(vg);
    }

    public static final int TITLE_CONTROL_MINIMIZE = 0;
    public static final int TITLE_CONTROL_MAXIMIZE = 1;
    public static final int TITLE_CONTROL_CLOSE = 2;

    /**
     * One of the three Windows 7 title bar buttons. Flat rectangles flush against each other
     * (native Windows 7 buttons are not individually rounded pills) drawn as vector glyphs, not
     * text, so font glyph coverage never matters.
     */
    public void drawTitleBarControl(float x, float y, float w, float h, int kind, boolean hovered) {
        boolean isCloseButton = kind == TITLE_CONTROL_CLOSE;
        int topC, botC, borderC, glyphColor;
        if (isCloseButton && hovered) {
            topC = 0xFFE8796D;
            botC = 0xFFC63027;
            borderC = 0xFF8E1E19;
            glyphColor = 0xFFFFFFFF;
        } else if (hovered) {
            topC = 0xFFDCF0FE;
            botC = 0xFFAEDBF7;
            borderC = 0xFF5B7894;
            glyphColor = 0xFF15539E;
        } else {
            topC = withAlpha(0xFFFFFFFF, 0.16f);
            botC = withAlpha(0xFFFFFFFF, 0.02f);
            borderC = withAlpha(0xFFFFFFFF, 0.0f);
            glyphColor = 0xFFFFFFFF;
        }
        drawVerticalGradientRounded(x, y, w, h, 0.0f, topC, botC);
        if ((borderC >>> 24) > 0) {
            drawRoundedRectOutline(x, y, w, h, 0.0f, 1.0f, borderC);
        }

        float cx = x + w / 2.0f;
        float cy = y + h / 2.0f;
        float s = Math.min(w, h) * 0.30f;
        setColorExact(color, glyphColor);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgLineCap(vg, NanoVG.NVG_BUTT);
        NanoVG.nvgLineJoin(vg, NanoVG.NVG_MITER);

        if (kind == TITLE_CONTROL_MINIMIZE) {
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgMoveTo(vg, cx - s, cy + s * 0.7f);
            NanoVG.nvgLineTo(vg, cx + s, cy + s * 0.7f);
            NanoVG.nvgStrokeWidth(vg, 1.2f);
            NanoVG.nvgStroke(vg);
        } else if (kind == TITLE_CONTROL_MAXIMIZE) {
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, cx - s, cy - s, s * 2.0f, s * 2.0f);
            NanoVG.nvgStrokeWidth(vg, 1.2f);
            NanoVG.nvgStroke(vg);
        } else {
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgMoveTo(vg, cx - s, cy - s);
            NanoVG.nvgLineTo(vg, cx + s, cy + s);
            NanoVG.nvgMoveTo(vg, cx + s, cy - s);
            NanoVG.nvgLineTo(vg, cx - s, cy + s);
            NanoVG.nvgStrokeWidth(vg, 1.3f);
            NanoVG.nvgStroke(vg);
        }
    }

    /**
     * The round glossy blue back/forward arrow button from the Windows 7 Explorer toolbar.
     */
    public void drawExplorerNavButton(float cx, float cy, float radius, boolean pointsLeft, boolean enabled, boolean hovered) {
        int topC = enabled ? (hovered ? 0xFF8FC6F2 : 0xFF6DB2EC) : 0xFFE1E6EB;
        int botC = enabled ? (hovered ? 0xFF2E7BC9 : 0xFF175BA6) : 0xFFC7CED6;
        int borderC = enabled ? 0xFF0F4E92 : 0xFFA7B6C6;

        setColorExact(color1, topC);
        setColorExact(color2, botC);
        NanoVG.nvgLinearGradient(vg, cx, cy - radius, cx, cy + radius, color1, color2, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy, radius);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);

        setColorExact(color1, withAlpha(0xFFFFFFFF, 0.55f));
        setColorExact(color2, withAlpha(0xFFFFFFFF, 0.0f));
        NanoVG.nvgLinearGradient(vg, cx, cy - radius, cx, cy, color1, color2, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy - radius * 0.15f, radius * 0.85f);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);

        setColorExact(color, borderC);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, cx, cy, radius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, 1.0f);
        NanoVG.nvgStroke(vg);

        int arrowColor = enabled ? 0xFFFFFFFF : 0xFF9AA7B4;
        setColorExact(color, arrowColor);
        float s = radius * 0.5f;
        NanoVG.nvgBeginPath(vg);
        if (pointsLeft) {
            NanoVG.nvgMoveTo(vg, cx + s * 0.35f, cy - s);
            NanoVG.nvgLineTo(vg, cx - s * 0.5f, cy);
            NanoVG.nvgLineTo(vg, cx + s * 0.35f, cy + s);
        } else {
            NanoVG.nvgMoveTo(vg, cx - s * 0.35f, cy - s);
            NanoVG.nvgLineTo(vg, cx + s * 0.5f, cy);
            NanoVG.nvgLineTo(vg, cx - s * 0.35f, cy + s);
        }
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, 1.5f);
        NanoVG.nvgLineCap(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgLineJoin(vg, NanoVG.NVG_ROUND);
        NanoVG.nvgStroke(vg);
    }

    /**
     * A Windows 7 trackbar (slider): thin silver groove, blue fill and a rounded silver/white knob.
     */
    public void drawAeroSlider(float x, float y, float width, float height, float progress, int grooveColor, int fillColor, int knobBorderColor) {
        float radius = height / 2.0f;
        drawRoundedRect(x, y, width, height, radius, grooveColor);
        drawRoundedRectOutline(x, y, width, height, radius, 0.75f, 0x33000000);

        float activeWidth = Math.max(height, width * Math.max(0.0f, Math.min(1.0f, progress)));
        drawVerticalGradientRounded(x, y, activeWidth, height, radius, withAlpha(fillColor, 0.95f), withAlpha(fillColor, 1.0f));

        float thumbX = x + width * Math.max(0.0f, Math.min(1.0f, progress));
        float thumbY = y + height / 2.0f;
        float thumbRadius = height * 0.95f;

        color.r(0.0f).g(0.0f).b(0.0f).a(0.30f);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, thumbX, thumbY + 1.0f, thumbRadius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);

        setColorExact(color1, 0xFFFFFFFF);
        setColorExact(color2, 0xFFD8D8D8);
        NanoVG.nvgLinearGradient(vg, thumbX, thumbY - thumbRadius, thumbX, thumbY + thumbRadius, color1, color2, paint);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, thumbX, thumbY, thumbRadius);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);

        setColorExact(color, knobBorderColor);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, thumbX, thumbY, thumbRadius);
        NanoVG.nvgStrokeColor(vg, color);
        NanoVG.nvgStrokeWidth(vg, 1.0f);
        NanoVG.nvgStroke(vg);
    }

    public void drawPickerKnob(float x, float y, float radius, int hexColor) {
        color.r(0.0f).g(0.0f).b(0.0f).a(0.4f);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, x, y + 1.0f, radius + 1.0f);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);

        color.r(1.0f).g(1.0f).b(1.0f).a(1.0f);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, x, y, radius);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);

        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }
        color.r(r).g(g).b(b).a(a);
        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgCircle(vg, x, y, radius - 1.5f);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgFill(vg);
    }
}
