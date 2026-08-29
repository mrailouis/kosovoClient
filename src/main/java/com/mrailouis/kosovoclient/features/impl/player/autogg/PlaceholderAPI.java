package com.mrailouis.kosovoclient.features.impl.player.autogg;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderAPI {
    private static final PlaceholderAPI INSTANCE = new PlaceholderAPI();
    private static final String PLACEHOLDER = "${%s}";
    private final Map<String, String> placeholders = new HashMap<String, String>();

    public static PlaceholderAPI getInstance() {
        return INSTANCE;
    }

    public void registerPlaceholder(String key, String value) {
        this.placeholders.put(key, value);
    }

    public String process(String string) {
        if (string == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : this.placeholders.entrySet()) {
            String placeholder = String.format(PLACEHOLDER, entry.getKey());
            if (string.contains(placeholder)) {
                string = string.replace(placeholder, entry.getValue());
            }
        }
        return string;
    }
}
