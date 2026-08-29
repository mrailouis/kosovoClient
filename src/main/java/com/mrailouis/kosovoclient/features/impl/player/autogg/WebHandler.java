package com.mrailouis.kosovoclient.features.impl.player.autogg;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class WebHandler {
    private static final Gson GSON = new Gson();

    public static String fetchString(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            try (InputStream in = connection.getInputStream()) {
                return IOUtils.toString(in, "UTF-8");
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T fetchJson(String urlString, Class<T> clazz) {
        String data = fetchString(urlString);
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(data, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
