package com.mrailouis.kosovoclient.features;

import com.mrailouis.kosovoclient.features.impl.animations.AnimationHandler;
import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import com.mrailouis.kosovoclient.features.impl.animations.SneakHandler;
import com.mrailouis.kosovoclient.features.impl.chat.CopyChat;
import com.mrailouis.kosovoclient.features.impl.chat.InfiniteChat;
import com.mrailouis.kosovoclient.features.impl.chat.SpamFilter;
import com.mrailouis.kosovoclient.features.impl.cosmetics.Capes;
import com.mrailouis.kosovoclient.features.impl.cosmetics.Wings;
import com.mrailouis.kosovoclient.features.impl.hud.CPS;
import com.mrailouis.kosovoclient.features.impl.hud.Clock;
import com.mrailouis.kosovoclient.features.impl.hud.FPS;
import com.mrailouis.kosovoclient.features.impl.hud.HudRenderer;
import com.mrailouis.kosovoclient.features.impl.hud.Ping;
import com.mrailouis.kosovoclient.features.impl.player.AutoGG;
import com.mrailouis.kosovoclient.features.impl.player.AutoTip;
import com.mrailouis.kosovoclient.features.impl.sounds.CustomKillSound;
import com.mrailouis.kosovoclient.features.impl.sounds.CustomWinSound;
import com.mrailouis.kosovoclient.features.impl.visuals.BlockOverlay;
import com.mrailouis.kosovoclient.features.impl.visuals.CustomCrosshair;
import com.mrailouis.kosovoclient.features.impl.visuals.CustomNametags;
import com.mrailouis.kosovoclient.features.impl.visuals.Fullbright;
import com.mrailouis.kosovoclient.features.impl.visuals.ItemPhysics;
import com.mrailouis.kosovoclient.features.impl.visuals.MotionBlur;
import com.mrailouis.kosovoclient.features.impl.visuals.Particles;
import com.mrailouis.kosovoclient.features.impl.visuals.Zoom;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    @Getter
    private static final ModuleManager instance = new ModuleManager();

    @Getter
    private final List<Module> modules = new ArrayList<Module>();

    public void init() {
        register(CPS.getInstance());
        register(FPS.getInstance());
        register(Ping.getInstance());
        register(Clock.getInstance());
        register(OldAnimations.getInstance());
        register(Fullbright.getInstance());
        register(Zoom.getInstance());
        register(MotionBlur.getInstance());
        register(BlockOverlay.getInstance());
        register(CustomCrosshair.getInstance());
        register(CustomNametags.getInstance());
        register(ItemPhysics.getInstance());
        register(Particles.getInstance());
        register(CopyChat.getInstance());
        register(InfiniteChat.getInstance());
        register(SpamFilter.getInstance());
        register(AutoTip.getInstance());
        register(AutoGG.getInstance());
        register(Capes.getInstance());
        register(Wings.getInstance());
        register(CustomWinSound.getInstance());
        register(CustomKillSound.getInstance());

        MinecraftForge.EVENT_BUS.register(CPS.getInstance());
        MinecraftForge.EVENT_BUS.register(HudRenderer.getInstance());
        MinecraftForge.EVENT_BUS.register(AnimationHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(SneakHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(Fullbright.getInstance());
        MinecraftForge.EVENT_BUS.register(Zoom.getInstance());
        MinecraftForge.EVENT_BUS.register(MotionBlur.getInstance());
        MinecraftForge.EVENT_BUS.register(BlockOverlay.getInstance());
        MinecraftForge.EVENT_BUS.register(CustomCrosshair.getInstance());
        MinecraftForge.EVENT_BUS.register(CustomNametags.getInstance());
        MinecraftForge.EVENT_BUS.register(Particles.getInstance());
        MinecraftForge.EVENT_BUS.register(AutoTip.getInstance());
        MinecraftForge.EVENT_BUS.register(AutoGG.getInstance());
        MinecraftForge.EVENT_BUS.register(CustomWinSound.getInstance());
        MinecraftForge.EVENT_BUS.register(CustomKillSound.getInstance());
    }

    public void register(Module module) {
        this.modules.add(module);
    }

    public List<Module> getModulesByCategory(Category category) {
        return this.modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Module> getModulesBySearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String lower = query.trim().toLowerCase();
        return this.modules.stream()
                .filter(m -> m.getName().toLowerCase().contains(lower) || m.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
