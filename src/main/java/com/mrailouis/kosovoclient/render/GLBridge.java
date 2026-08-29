package com.mrailouis.kosovoclient.render;

import org.lwjgl.system.FunctionProvider;

public final class GLBridge {

    private GLBridge() {
    }

    public static Object getCapabilities() {
        return null;
    }

    public static FunctionProvider getFunctionProvider() {
        return LWJGL2FunctionProvider.getInstance();
    }
}
