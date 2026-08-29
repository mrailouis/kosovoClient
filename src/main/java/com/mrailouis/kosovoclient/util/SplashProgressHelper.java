package com.mrailouis.kosovoclient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SplashProgressHelper {

    public static void applyConfig(File mcDir) {
        if (mcDir == null) {
            return;
        }

        File configFile = new File(mcDir, "config/splash.properties");
        Properties properties = new Properties();

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                properties.load(in);
            } catch (Exception ignored) {
            }
        }

        properties.setProperty("background", "0xFF0000");
        properties.setProperty("barBackground", "0xFF0000");
        properties.setProperty("barBorder", "0xFF0000");
        properties.setProperty("bar", "0xFF0000");
        properties.setProperty("font", "0xFF0000");
        properties.setProperty("logoTexture", "textures/gui/title/mojang.png");
        properties.setProperty("forgeTexture", "fml:textures/gui/forge.gif");
        properties.setProperty("enabled", "true");

        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (OutputStream out = new FileOutputStream(configFile)) {
            properties.store(out, "Splash screen properties");
        } catch (Exception ignored) {
        }
    }
}
