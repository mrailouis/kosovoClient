package com.mrailouis.kosovoclient.features.impl.player.autogg;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TriggerType {
    NORMAL(0),
    CASUAL(1),
    ANTI_GG(2),
    ANTI_KARMA(3);

    private final int type;

    public static TriggerType getByType(int t) {
        for (TriggerType type : values()) {
            if (type.type == t) {
                return type;
            }
        }
        return TriggerType.NORMAL;
    }
}
