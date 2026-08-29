package com.mrailouis.kosovoclient.proxy;

import com.mrailouis.kosovoclient.features.ModuleManager;
import com.mrailouis.kosovoclient.listener.KeyInputListener;
import com.mrailouis.kosovoclient.util.LoadingProgressMonitor;
import com.mrailouis.kosovoclient.util.NativeLoader;
import com.mrailouis.kosovoclient.util.SplashProgressHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        LoadingProgressMonitor.initialize();
        SplashProgressHelper.applyConfig(event.getModConfigurationDirectory().getParentFile());
        NativeLoader.load();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ModuleManager.getInstance().init();
        MinecraftForge.EVENT_BUS.register(new KeyInputListener());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
}
