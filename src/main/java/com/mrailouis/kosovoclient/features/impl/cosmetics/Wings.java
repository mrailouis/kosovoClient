package com.mrailouis.kosovoclient.features.impl.cosmetics;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ColorSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.features.impl.cosmetics.wings.ModelAngelWings;
import com.mrailouis.kosovoclient.features.impl.cosmetics.wings.ModelDragonWings;
import com.mrailouis.kosovoclient.features.impl.cosmetics.wings.ModelVoxelWings;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

@Getter
public class Wings extends Module {
    private static final String[] STYLES = {"Dragon", "Minecraft 3D", "Angel", "Demon", "Fairy"};
    private static final String[] TEXTURES = {"Default", "Dragon", "Prismarine", "Obsidian", "Fire", "Portal", "End Crystal", "Elytra"};
    private static final Wings INSTANCE = new Wings();

    private final ModeSetting style = new ModeSetting("Style", "Wing model geometry.", "Dragon", STYLES);
    private final ModeSetting texture = new ModeSetting("Texture", "Minecraft texture mapped to wings.", "Default", TEXTURES);
    private final NumberSetting scale = new NumberSetting("Scale", "Overall wing size multiplier.", 1.0, 0.5, 2.5, 0.1);
    private final ColorSetting color = new ColorSetting("Color", "Custom wing tint color.", 0xFFFFFFFF);
    private final BooleanSetting customColor = new BooleanSetting("Custom Color", "Enable color tinting on wings.", false);
    private final NumberSetting flapSpeed = new NumberSetting("Flap Speed", "Wing flapping animation speed.", 1.0, 0.2, 3.0, 0.1);
    private final BooleanSetting flapMovingOnly = new BooleanSetting("Only Flap Moving", "Only flap wings when walking or flying.", false);

    private final ModelDragonWings dragonWings = new ModelDragonWings();
    private final ModelVoxelWings voxelWings = new ModelVoxelWings();
    private final ModelAngelWings angelWings = new ModelAngelWings();

    private static final ResourceLocation LOC_DRAGON = new ResourceLocation("textures/entity/enderdragon/dragon.png");
    private static final ResourceLocation LOC_PRISMARINE = new ResourceLocation("textures/blocks/prismarine_rough.png");
    private static final ResourceLocation LOC_OBSIDIAN = new ResourceLocation("textures/blocks/obsidian.png");
    private static final ResourceLocation LOC_FIRE = new ResourceLocation("textures/blocks/fire_layer_0.png");
    private static final ResourceLocation LOC_PORTAL = new ResourceLocation("textures/blocks/portal.png");
    private static final ResourceLocation LOC_END_CRYSTAL = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
    private static final ResourceLocation LOC_ELYTRA = new ResourceLocation("textures/entity/elytra.png");

    public static Wings getInstance() {
        return INSTANCE;
    }

    private Wings() {
        super("Wings", "Cosmetic 3D wings with articulated flapping animation.", Category.COSMETICS, true);
        registerSetting(this.style);
        registerSetting(this.texture);
        registerSetting(this.scale);
        registerSetting(this.color);
        registerSetting(this.customColor);
        registerSetting(this.flapSpeed);
        registerSetting(this.flapMovingOnly);
    }

    public ResourceLocation getActiveTexture() {
        String tex = this.texture.getValue();
        if ("Prismarine".equalsIgnoreCase(tex)) {
            return LOC_PRISMARINE;
        } else if ("Obsidian".equalsIgnoreCase(tex)) {
            return LOC_OBSIDIAN;
        } else if ("Fire".equalsIgnoreCase(tex)) {
            return LOC_FIRE;
        } else if ("Portal".equalsIgnoreCase(tex)) {
            return LOC_PORTAL;
        } else if ("End Crystal".equalsIgnoreCase(tex)) {
            return LOC_END_CRYSTAL;
        } else if ("Elytra".equalsIgnoreCase(tex)) {
            return LOC_ELYTRA;
        } else if ("Dragon".equalsIgnoreCase(tex)) {
            return LOC_DRAGON;
        }

        String st = this.style.getValue();
        if ("Minecraft 3D".equalsIgnoreCase(st)) {
            return LOC_PRISMARINE;
        } else if ("Demon".equalsIgnoreCase(st)) {
            return LOC_OBSIDIAN;
        } else if ("Fairy".equalsIgnoreCase(st)) {
            return LOC_END_CRYSTAL;
        } else if ("Angel".equalsIgnoreCase(st)) {
            return LOC_ELYTRA;
        }
        return LOC_DRAGON;
    }

    public void renderWings(EntityPlayer player, float partialTicks) {
        if (!isEnabled() || player == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(getActiveTexture());

        GlStateManager.pushMatrix();

        if (this.customColor.isEnabled()) {
            int c = this.color.getColor();
            float a = ((c >> 24) & 0xFF) / 255.0f;
            float r = ((c >> 16) & 0xFF) / 255.0f;
            float g = ((c >> 8) & 0xFF) / 255.0f;
            float b = (c & 0xFF) / 255.0f;
            GlStateManager.color(r, g, b, a);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        float sc = this.scale.getValue().floatValue();
        float sp = this.flapSpeed.getValue().floatValue();
        boolean movingOnly = this.flapMovingOnly.isEnabled();

        String st = this.style.getValue();
        if ("Minecraft 3D".equalsIgnoreCase(st)) {
            this.voxelWings.renderWings(player, partialTicks, sc, sp, movingOnly);
        } else if ("Angel".equalsIgnoreCase(st) || "Demon".equalsIgnoreCase(st) || "Fairy".equalsIgnoreCase(st)) {
            this.angelWings.renderWings(player, partialTicks, sc, sp, movingOnly);
        } else {
            this.dragonWings.renderWings(player, partialTicks, sc, sp, movingOnly);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }
}
