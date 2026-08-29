package com.mrailouis.kosovoclient.features.impl.player.autogg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PatternHandler {
    private static final PatternHandler INSTANCE = new PatternHandler();
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<String, Pattern>();

    public static PatternHandler getInstance() {
        return INSTANCE;
    }

    public Pattern getOrRegisterPattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        String processedPattern = PlaceholderAPI.getInstance().process(pattern);
        Pattern p = this.patternCache.get(processedPattern);
        if (p == null) {
            try {
                p = Pattern.compile(processedPattern);
                this.patternCache.put(processedPattern, p);
            } catch (Exception e) {
                return null;
            }
        }
        return p;
    }

    public void clearPatterns() {
        this.patternCache.clear();
    }
}
