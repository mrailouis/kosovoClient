package com.mrailouis.kosovoclient.features.impl.cosmetics.capes;

import com.mrailouis.kosovoclient.features.impl.cosmetics.Capes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;

public class LayerCustomCape implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer playerRenderer;

    public LayerCustomCape(RenderPlayer playerRenderer) {
        this.playerRenderer = playerRenderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        if (!Capes.getInstance().isEnabled()) {
            return;
        }

        if (entitylivingbaseIn != Minecraft.getMinecraft().thePlayer) {
            return;
        }

        if (entitylivingbaseIn.isInvisible()) {
            return;
        }

        GlStateManager.pushMatrix();
        if (entitylivingbaseIn.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }

        this.playerRenderer.getMainModel().bipedBody.postRender(0.0625F);

        ItemStack chest = entitylivingbaseIn.getCurrentArmor(2);
        if (chest != null) {
            GlStateManager.translate(0.0F, 0.0F, 0.05F);
        }

        Capes.getInstance().renderCape(entitylivingbaseIn, partialTicks);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
