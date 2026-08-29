package com.mrailouis.kosovoclient.features;

import com.mrailouis.kosovoclient.features.impl.animations.AnimationHandler;
import com.mrailouis.kosovoclient.features.impl.animations.OldAnimations;
import com.mrailouis.kosovoclient.features.impl.animations.SneakHandler;
import com.mrailouis.kosovoclient.features.impl.visuals.BlockOverlay;
import com.mrailouis.kosovoclient.features.impl.visuals.Fullbright;
import com.mrailouis.kosovoclient.features.impl.visuals.MotionBlur;
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
        register(OldAnimations.getInstance());
        register(Fullbright.getInstance());
        register(Zoom.getInstance());
        register(MotionBlur.getInstance());
        register(BlockOverlay.getInstance());

        MinecraftForge.EVENT_BUS.register(AnimationHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(SneakHandler.getInstance());
        MinecraftForge.EVENT_BUS.register(Fullbright.getInstance());
        MinecraftForge.EVENT_BUS.register(Zoom.getInstance());
        MinecraftForge.EVENT_BUS.register(MotionBlur.getInstance());
        MinecraftForge.EVENT_BUS.register(BlockOverlay.getInstance());
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
