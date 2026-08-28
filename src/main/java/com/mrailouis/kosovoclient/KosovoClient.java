package com.mrailouis.kosovoclient;

import com.mrailouis.kosovoclient.proxy.CommonProxy;
import lombok.Getter;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = KosovoClient.MOD_ID, useMetadata = true)
public class KosovoClient {

    public static final String MOD_ID = "kosovoclient";

    @Getter
    @Mod.Instance(MOD_ID)
    private static KosovoClient instance;

    @SidedProxy(
            clientSide = "com.mrailouis.kosovoclient.proxy.ClientProxy",
            serverSide = "com.mrailouis.kosovoclient.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
