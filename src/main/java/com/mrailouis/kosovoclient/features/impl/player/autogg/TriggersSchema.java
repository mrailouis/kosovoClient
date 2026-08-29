package com.mrailouis.kosovoclient.features.impl.player.autogg;

import lombok.Getter;

@Getter
public class TriggersSchema {
    private final Server[] servers;

    public TriggersSchema(Server[] servers) {
        this.servers = servers;
    }
}
