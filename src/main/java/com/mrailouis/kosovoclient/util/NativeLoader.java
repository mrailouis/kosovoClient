package com.mrailouis.kosovoclient.util;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.InputStream;

public class NativeLoader {

    private static boolean loaded = false;

    @SneakyThrows
    public static synchronized void load() {
        if (loaded) {
            return;
        }

        String os;
        String arch;
        String ext;
        String prefix = "lib";

        if (SystemUtils.IS_OS_WINDOWS) {
            os = "windows";
            ext = ".dll";
            prefix = "";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            os = "macos";
            ext = ".dylib";
        } else {
            os = "linux";
            ext = ".so";
        }

        String osArch = System.getProperty("os.arch").toLowerCase();
        if (osArch.contains("aarch64") || osArch.contains("arm64")) {
            arch = "arm64";
        } else {
            arch = "x64";
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "kosovoclient_natives_" + System.currentTimeMillis());
        tempDir.deleteOnExit();

        String[] libraries = new String[]{"lwjgl", "lwjgl_nanovg"};

        for (String lib : libraries) {
            String fileName = prefix + lib + ext;
            String subDir = lib.equals("lwjgl_nanovg") ? "org/lwjgl/nanovg/" : "org/lwjgl/";
            String pathInJar = os + "/" + arch + "/" + subDir + fileName;

            InputStream is = NativeLoader.class.getClassLoader().getResourceAsStream(pathInJar);
            if (is == null) {
                is = NativeLoader.class.getClassLoader().getResourceAsStream(os + "/" + arch + "/" + fileName);
            }

            if (is != null) {
                File rootFile = new File(tempDir, fileName);
                File nestedFile = new File(tempDir, subDir + fileName);

                FileUtils.copyInputStreamToFile(is, rootFile);
                FileUtils.copyFile(rootFile, nestedFile);

                rootFile.deleteOnExit();
                nestedFile.deleteOnExit();
            }
        }

        if (tempDir.exists() && tempDir.list() != null && tempDir.list().length > 0) {
            System.setProperty("org.lwjgl.librarypath", tempDir.getAbsolutePath());
        }

        loaded = true;
    }
}
