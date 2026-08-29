package com.mrailouis.kosovoclient.mixins.IMixin;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SimpleReloadableResourceManager.class)
public interface IMixinSimpleReloadableResourceManager {
    @Accessor("domainResourceManagers")
    Map<String, FallbackResourceManager> getDomainResourceManagers();

    @Accessor("rmMetadataSerializer")
    IMetadataSerializer getMetadataSerializer();
}
