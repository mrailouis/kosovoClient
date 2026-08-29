package com.mrailouis.kosovoclient.render;

import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class LWJGL2FunctionProvider implements FunctionProvider {

    @Getter
    private static final LWJGL2FunctionProvider instance = new LWJGL2FunctionProvider();

    private final Method getFunctionAddressMethod;

    @SneakyThrows
    private LWJGL2FunctionProvider() {
        Class<?> glContextClass = Class.forName("org.lwjgl.opengl.GLContext");
        getFunctionAddressMethod = glContextClass.getDeclaredMethod("getFunctionAddress", String.class);
        getFunctionAddressMethod.setAccessible(true);
    }

    @Override
    @SneakyThrows
    public long getFunctionAddress(CharSequence functionName) {
        return (long) getFunctionAddressMethod.invoke(null, functionName.toString());
    }

    @Override
    @SneakyThrows
    public long getFunctionAddress(ByteBuffer functionName) {
        String name = MemoryUtil.memASCII(functionName);
        return (long) getFunctionAddressMethod.invoke(null, name);
    }
}
