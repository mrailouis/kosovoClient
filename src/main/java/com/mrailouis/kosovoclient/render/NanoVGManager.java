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

    public void drawText(String text, float x, float y, String fontName, float fontSize, int hexColor, int alignment) {
        float r = ((hexColor >> 16) & 0xFF) / 255.0f;
        float g = ((hexColor >> 8) & 0xFF) / 255.0f;
        float b = (hexColor & 0xFF) / 255.0f;
        float a = ((hexColor >> 24) & 0xFF) / 255.0f;
        if (a == 0.0f && (hexColor & 0xFF000000) == 0) {
            a = 1.0f;
        }

        float shadowOffset = Math.max(1.0f, Math.round(fontSize * 0.035f));
        float shadowAlpha = a * 0.35f;

        color.r(0.0f);
        color.g(0.0f);
        color.b(0.0f);
        color.a(shadowAlpha);

        NanoVG.nvgFontFace(vg, fontName);
        NanoVG.nvgFontSize(vg, fontSize);
        NanoVG.nvgTextAlign(vg, alignment);
        NanoVG.nvgFillColor(vg, color);
        NanoVG.nvgText(vg, x + shadowOffset, y + shadowOffset, text);

        color.r(r);
        color.g(g);
        color.b(b);
        color.a(a);

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
