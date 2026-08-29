package com.mrailouis.kosovoclient.render;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class KawaseBlur {

    @Getter
    private static final KawaseBlur instance = new KawaseBlur();

    private int downsampleProgram = -1;
    private int upsampleProgram = -1;
    private int maskUpsampleProgram = -1;

    private Framebuffer down1;
    private Framebuffer down2;
    private Framebuffer down3;
    private Framebuffer down4;
    private Framebuffer up3;
    private Framebuffer up2;
    private Framebuffer up1;

    private int lastWidth = -1;
    private int lastHeight = -1;

    private static final String VERTEX_SHADER =
            "#version 120\n" +
            "void main() {\n" +
            "    gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
            "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n" +
            "}\n";

    private static final String DOWNSAMPLE_SHADER =
            "#version 120\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform vec2 uHalfPixel;\n" +
            "uniform float uOffset;\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    vec4 sum = texture2D(uTexture, uv) * 4.0;\n" +
            "    sum += texture2D(uTexture, uv - uHalfPixel * uOffset);\n" +
            "    sum += texture2D(uTexture, uv + uHalfPixel * uOffset);\n" +
            "    sum += texture2D(uTexture, uv + vec2(uHalfPixel.x, -uHalfPixel.y) * uOffset);\n" +
            "    sum += texture2D(uTexture, uv - vec2(uHalfPixel.x, -uHalfPixel.y) * uOffset);\n" +
            "    gl_FragColor = sum / 8.0;\n" +
            "}\n";

    private static final String UPSAMPLE_SHADER =
            "#version 120\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform vec2 uHalfPixel;\n" +
            "uniform float uOffset;\n" +
            "void main() {\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    vec2 d = uHalfPixel * uOffset;\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x * 2.0, 0.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x, d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(0.0, d.y * 2.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x, d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x * 2.0, 0.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x, -d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(0.0, -d.y * 2.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x, -d.y)) * 2.0;\n" +
            "    gl_FragColor = sum / 12.0;\n" +
            "}\n";

    private static final String MASK_UPSAMPLE_SHADER =
            "#version 120\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform vec2 uHalfPixel;\n" +
            "uniform float uOffset;\n" +
            "uniform vec2 uRectPos;\n" +
            "uniform vec2 uRectSize;\n" +
            "uniform float uRadius;\n" +
            "float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {\n" +
            "    vec2 q = abs(centerPos) - size + vec2(radius);\n" +
            "    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;\n" +
            "}\n" +
            "void main() {\n" +
            "    vec2 fragCoord = gl_FragCoord.xy;\n" +
            "    vec2 rectCenter = uRectPos + uRectSize * 0.5;\n" +
            "    float dist = roundedBoxSDF(fragCoord - rectCenter, uRectSize * 0.5, uRadius);\n" +
            "    if (dist > 0.0) {\n" +
            "        discard;\n" +
            "    }\n" +
            "    float alpha = clamp(0.5 - dist, 0.0, 1.0);\n" +
            "    vec2 uv = gl_TexCoord[0].st;\n" +
            "    vec2 d = uHalfPixel * uOffset;\n" +
            "    vec4 sum = vec4(0.0);\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x * 2.0, 0.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x, d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(0.0, d.y * 2.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x, d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x * 2.0, 0.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(d.x, -d.y)) * 2.0;\n" +
            "    sum += texture2D(uTexture, uv + vec2(0.0, -d.y * 2.0));\n" +
            "    sum += texture2D(uTexture, uv + vec2(-d.x, -d.y)) * 2.0;\n" +
            "    vec4 color = sum / 12.0;\n" +
            "    gl_FragColor = vec4(color.rgb, color.a * alpha);\n" +
            "}\n";

    private void initShaders() {
        if (downsampleProgram != -1) {
            return;
        }

        downsampleProgram = createProgram(VERTEX_SHADER, DOWNSAMPLE_SHADER);
        upsampleProgram = createProgram(VERTEX_SHADER, UPSAMPLE_SHADER);
        maskUpsampleProgram = createProgram(VERTEX_SHADER, MASK_UPSAMPLE_SHADER);
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSource);
        GL20.glCompileShader(vertexShader);

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSource);
        GL20.glCompileShader(fragmentShader);

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return program;
    }

    private void updateFramebuffers(int width, int height) {
        if (lastWidth != width || lastHeight != height || down1 == null) {
            if (down1 != null) {
                down1.deleteFramebuffer();
                down2.deleteFramebuffer();
                down3.deleteFramebuffer();
                down4.deleteFramebuffer();
                up3.deleteFramebuffer();
                up2.deleteFramebuffer();
                up1.deleteFramebuffer();
            }

            down1 = new Framebuffer(Math.max(1, width / 2), Math.max(1, height / 2), false);
            down1.setFramebufferFilter(GL11.GL_LINEAR);

            down2 = new Framebuffer(Math.max(1, width / 4), Math.max(1, height / 4), false);
            down2.setFramebufferFilter(GL11.GL_LINEAR);

            down3 = new Framebuffer(Math.max(1, width / 8), Math.max(1, height / 8), false);
            down3.setFramebufferFilter(GL11.GL_LINEAR);

            down4 = new Framebuffer(Math.max(1, width / 16), Math.max(1, height / 16), false);
            down4.setFramebufferFilter(GL11.GL_LINEAR);

            up3 = new Framebuffer(Math.max(1, width / 8), Math.max(1, height / 8), false);
            up3.setFramebufferFilter(GL11.GL_LINEAR);

            up2 = new Framebuffer(Math.max(1, width / 4), Math.max(1, height / 4), false);
            up2.setFramebufferFilter(GL11.GL_LINEAR);

            up1 = new Framebuffer(Math.max(1, width / 2), Math.max(1, height / 2), false);
            up1.setFramebufferFilter(GL11.GL_LINEAR);

            lastWidth = width;
            lastHeight = height;
        }
    }

    public void renderBlur(float x, float y, float width, float height, float radius) {
        if (!OpenGlHelper.isFramebufferEnabled() || width <= 0.0f || height <= 0.0f) {
            return;
        }

        initShaders();

        Minecraft mc = Minecraft.getMinecraft();
        int displayWidth = mc.displayWidth;
        int displayHeight = mc.displayHeight;

        updateFramebuffers(displayWidth, displayHeight);

        Framebuffer mcFramebuffer = mc.getFramebuffer();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        down1.bindFramebuffer(false);
        GL20.glUseProgram(downsampleProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(downsampleProgram, "uTexture"), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(downsampleProgram, "uHalfPixel"), 1.0f / displayWidth, 1.0f / displayHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(downsampleProgram, "uOffset"), 1.5f);
        mcFramebuffer.bindFramebufferTexture();
        drawQuad(down1.framebufferWidth, down1.framebufferHeight);

        down2.bindFramebuffer(false);
        GL20.glUniform2f(GL20.glGetUniformLocation(downsampleProgram, "uHalfPixel"), 1.0f / down1.framebufferWidth, 1.0f / down1.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(downsampleProgram, "uOffset"), 2.0f);
        down1.bindFramebufferTexture();
        drawQuad(down2.framebufferWidth, down2.framebufferHeight);

        down3.bindFramebuffer(false);
        GL20.glUniform2f(GL20.glGetUniformLocation(downsampleProgram, "uHalfPixel"), 1.0f / down2.framebufferWidth, 1.0f / down2.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(downsampleProgram, "uOffset"), 2.5f);
        down2.bindFramebufferTexture();
        drawQuad(down3.framebufferWidth, down3.framebufferHeight);

        down4.bindFramebuffer(false);
        GL20.glUniform2f(GL20.glGetUniformLocation(downsampleProgram, "uHalfPixel"), 1.0f / down3.framebufferWidth, 1.0f / down3.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(downsampleProgram, "uOffset"), 3.0f);
        down3.bindFramebufferTexture();
        drawQuad(down4.framebufferWidth, down4.framebufferHeight);

        up3.bindFramebuffer(false);
        GL20.glUseProgram(upsampleProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(upsampleProgram, "uTexture"), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(upsampleProgram, "uHalfPixel"), 1.0f / down4.framebufferWidth, 1.0f / down4.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(upsampleProgram, "uOffset"), 3.0f);
        down4.bindFramebufferTexture();
        drawQuad(up3.framebufferWidth, up3.framebufferHeight);

        up2.bindFramebuffer(false);
        GL20.glUniform2f(GL20.glGetUniformLocation(upsampleProgram, "uHalfPixel"), 1.0f / up3.framebufferWidth, 1.0f / up3.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(upsampleProgram, "uOffset"), 2.5f);
        up3.bindFramebufferTexture();
        drawQuad(up2.framebufferWidth, up2.framebufferHeight);

        up1.bindFramebuffer(false);
        GL20.glUniform2f(GL20.glGetUniformLocation(upsampleProgram, "uHalfPixel"), 1.0f / up2.framebufferWidth, 1.0f / up2.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(upsampleProgram, "uOffset"), 2.0f);
        up2.bindFramebufferTexture();
        drawQuad(up1.framebufferWidth, up1.framebufferHeight);

        mcFramebuffer.bindFramebuffer(false);

        ScaledResolution sr = new ScaledResolution(mc);
        float scaleFactor = sr.getScaleFactor();
        float physicalX = x * scaleFactor;
        float physicalY = (sr.getScaledHeight() - (y + height)) * scaleFactor;
        float physicalWidth = width * scaleFactor;
        float physicalHeight = height * scaleFactor;
        float physicalRadius = radius * scaleFactor;

        GL20.glUseProgram(maskUpsampleProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(maskUpsampleProgram, "uTexture"), 0);
        GL20.glUniform2f(GL20.glGetUniformLocation(maskUpsampleProgram, "uHalfPixel"), 1.0f / up1.framebufferWidth, 1.0f / up1.framebufferHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(maskUpsampleProgram, "uOffset"), 1.5f);
        GL20.glUniform2f(GL20.glGetUniformLocation(maskUpsampleProgram, "uRectPos"), physicalX, physicalY);
        GL20.glUniform2f(GL20.glGetUniformLocation(maskUpsampleProgram, "uRectSize"), physicalWidth, physicalHeight);
        GL20.glUniform1f(GL20.glGetUniformLocation(maskUpsampleProgram, "uRadius"), physicalRadius);

        up1.bindFramebufferTexture();
        drawQuad(displayWidth, displayHeight);

        GL20.glUseProgram(0);
        mcFramebuffer.bindFramebufferTexture();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    private void drawQuad(int width, int height) {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.viewport(0, 0, width, height);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, height, 0.0D).tex(0.0D, 0.0D).endVertex();
        worldrenderer.pos(width, height, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(width, 0.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        tessellator.draw();
    }
}
