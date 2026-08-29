package com.mrailouis.kosovoclient.features.impl.player.autogg;

import lombok.Getter;

@Getter
public class Trigger {
    private final int type;
    private final String pattern;
    private TriggerType triggerType;

    public Trigger(int type, String pattern) {
        this.type = type;
        this.pattern = pattern;
    }

    public TriggerType getType() {
        if (triggerType == null) {
            triggerType = TriggerType.getByType(type);
        }
        return triggerType;
    }
}
