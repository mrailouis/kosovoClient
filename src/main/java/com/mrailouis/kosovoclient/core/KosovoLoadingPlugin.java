package com.mrailouis.kosovoclient.core;

import com.mrailouis.kosovoclient.util.SplashProgressHelper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("KosovoClientCore")
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE)
public class KosovoLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        File mcLocation = (File) data.get("mcLocation");
        if (mcLocation != null) {
            SplashProgressHelper.applyConfig(mcLocation);
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
