package com.mrailouis.kosovoclient.features;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    HUD("HUD"),
    VISUALS("Visuals"),
    ANIMATIONS("Animations"),
    PLAYER("Player"),
    CHAT("Chat"),
    COSMETICS("Cosmetics"),
    SOUNDS("Sounds");

    private final String displayName;
}
