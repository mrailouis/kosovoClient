package com.mrailouis.kosovoclient.features.impl.visuals;

import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.mixins.IMixin.IMixinSimpleReloadableResourceManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Getter
public class MotionBlur extends Module {
    private static final MotionBlur INSTANCE = new MotionBlur();

    private final NumberSetting blurAmount = new NumberSetting("Blur Amount", "Motion blur intensity level.", 3.0, 1.0, 7.0, 1.0);

    private double lastBlurAmount = -1.0;
    private Map<String, FallbackResourceManager> domainResourceManagers = null;

    public static MotionBlur getInstance() {
        return INSTANCE;
    }

    private MotionBlur() {
        super("Motion Blur", "Smooth shader-based camera motion blur.", Category.VISUALS, false);
        registerSetting(blurAmount);
    }

    @Override
    public void onEnable() {
        this.lastBlurAmount = -1.0;
        reloadShader();
    }

    @Override
    public void onDisable() {
        this.lastBlurAmount = -1.0;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.entityRenderer != null) {
            mc.entityRenderer.stopUseShader();
        }
    }

    public void reloadShader() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.entityRenderer == null || mc.theWorld == null || !OpenGlHelper.shadersSupported) {
            return;
        }

        ensureResourceManager(mc);

        if (mc.entityRenderer.isShaderActive()) {
            mc.entityRenderer.stopUseShader();
        }

        mc.entityRenderer.loadShader(new ResourceLocation("motionblur", "motionblur"));
        this.lastBlurAmount = this.blurAmount.getValue();
    }

    private void ensureResourceManager(Minecraft mc) {
        if (this.domainResourceManagers == null && mc.getResourceManager() instanceof IMixinSimpleReloadableResourceManager) {
            this.domainResourceManagers = ((IMixinSimpleReloadableResourceManager) mc.getResourceManager()).getDomainResourceManagers();
        }

        if (this.domainResourceManagers != null && !this.domainResourceManagers.containsKey("motionblur")) {
            IMetadataSerializer serializer = ((IMixinSimpleReloadableResourceManager) mc.getResourceManager()).getMetadataSerializer();
            this.domainResourceManagers.put("motionblur", new MotionBlurResourceManager(serializer));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.entityRenderer == null || !OpenGlHelper.shadersSupported) {
            return;
        }

        if (isEnabled()) {
            ensureResourceManager(mc);

            double currentAmount = this.blurAmount.getValue();
            if (!mc.entityRenderer.isShaderActive() || this.lastBlurAmount != currentAmount) {
                reloadShader();
            }
        }
    }

    private class MotionBlurResourceManager extends FallbackResourceManager implements IResourceManager {
        public MotionBlurResourceManager(IMetadataSerializer serializer) {
            super(serializer);
        }

        @Override
        public Set<String> getResourceDomains() {
            return null;
        }

        @Override
        public IResource getResource(ResourceLocation location) {
            return new MotionBlurResource();
        }

        @Override
        public List<IResource> getAllResources(ResourceLocation location) {
            return null;
        }
    }

    private class MotionBlurResource implements IResource {
        private static final String JSON =
                "{\"targets\":[\"swap\",\"previous\"],\"passes\":[{\"name\":\"phosphor\",\"intarget\":\"minecraft:main\",\"outtarget\":\"swap\",\"auxtargets\":[{\"name\":\"PrevSampler\",\"id\":\"previous\"}],\"uniforms\":[{\"name\":\"Phosphor\",\"values\":[%.2f, %.2f, %.2f]}]},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"previous\"},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"minecraft:main\"}]}";

        @Override
        public ResourceLocation getResourceLocation() {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            double amount = 0.7 + MotionBlur.this.blurAmount.getValue() / 100.0 * 3.0 - 0.01;
            return IOUtils.toInputStream(
                    String.format(Locale.ENGLISH, JSON, amount, amount, amount),
                    Charset.defaultCharset()
            );
        }

        @Override
        public boolean hasMetadata() {
            return false;
        }

        @Override
        public <T extends IMetadataSection> T getMetadata(String sectionName) {
            return null;
        }

        @Override
        public String getResourcePackName() {
            return null;
        }
    }
}
