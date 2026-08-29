package com.mrailouis.kosovoclient.features.impl.cosmetics.capes;

import com.mrailouis.kosovoclient.features.impl.cosmetics.Capes;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CapeManager {
    private static final String[] PRESET_RESOURCES = {
            "optifine", "kosovo", "lunar", "galaxy", "fire"
    };

    @Getter
    private static final CapeManager instance = new CapeManager();

    private final File capesDir;
    private final Map<String, CapeTexture> loadedCapes = new ConcurrentHashMap<>();
    private final List<String> availableNames = Collections.synchronizedList(new ArrayList<>());

    private CapeManager() {
        Minecraft mc = Minecraft.getMinecraft();
        File baseDir = mc.mcDataDir != null ? mc.mcDataDir : new File(".");
        this.capesDir = new File(baseDir, "kosovoclient/capes");
        if (!this.capesDir.exists()) {
            this.capesDir.mkdirs();
        }
        extractDefaultPresets();
        reloadCapes();
    }

    private void extractDefaultPresets() {
        if (this.capesDir == null) {
            return;
        }
        for (String preset : PRESET_RESOURCES) {
            try {
                File targetFile = new File(this.capesDir, preset + ".png");
                if (!targetFile.exists()) {
                    InputStream in = CapeManager.class.getResourceAsStream("/assets/kosovoclient/capes/" + preset + ".png");
                    if (in == null) {
                        ClassLoader cl = CapeManager.class.getClassLoader();
                        if (cl != null) {
                            in = cl.getResourceAsStream("assets/kosovoclient/capes/" + preset + ".png");
                        }
                    }
                    if (in != null) {
                        try {
                            FileUtils.copyInputStreamToFile(in, targetFile);
                        } finally {
                            in.close();
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public synchronized void reloadCapes() {
        this.loadedCapes.clear();
        this.availableNames.clear();

        File[] files = this.capesDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".gif");
        });

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
                String displayName = Character.toUpperCase(nameWithoutExt.charAt(0)) + nameWithoutExt.substring(1);

                CapeTexture texture = loadTextureFromFile(displayName, file);
                if (texture != null) {
                    this.loadedCapes.put(displayName.toLowerCase(), texture);
                    this.availableNames.add(displayName);
                }
            }
        }

        if (this.availableNames.isEmpty()) {
            for (String preset : PRESET_RESOURCES) {
                String displayName = Character.toUpperCase(preset.charAt(0)) + preset.substring(1);
                this.availableNames.add(displayName);
            }
        }
    }

    private CapeTexture loadTextureFromFile(String name, File file) {
        try {
            String lower = file.getName().toLowerCase();
            if (lower.endsWith(".gif")) {
                return loadGifCape(name, file);
            } else {
                return loadPngCape(name, file);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private CapeTexture loadPngCape(String name, File file) throws Exception {
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        if (height > width && width > 0) {
            int frameHeight = width / 2;
            int count = height / frameHeight;
            if (count > 1) {
                ResourceLocation[] frames = new ResourceLocation[count];
                int[] delays = new int[count];
                for (int i = 0; i < count; i++) {
                    BufferedImage sub = img.getSubimage(0, i * frameHeight, width, frameHeight);
                    DynamicTexture dyn = new DynamicTexture(sub);
                    ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("cape_" + name.toLowerCase() + "_f" + i, dyn);
                    frames[i] = loc;
                    delays[i] = 100;
                }
                return new CapeTexture(name, frames, delays, width, frameHeight);
            }
        }

        DynamicTexture dyn = new DynamicTexture(img);
        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("cape_" + name.toLowerCase(), dyn);
        return new CapeTexture(name, loc, width, height);
    }

    private CapeTexture loadGifCape(String name, File file) throws Exception {
        ImageInputStream stream = ImageIO.createImageInputStream(file);
        if (stream == null) {
            return null;
        }

        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext()) {
            return null;
        }

        ImageReader reader = readers.next();
        reader.setInput(stream);

        int numImages = reader.getNumImages(true);
        if (numImages <= 0) {
            return null;
        }

        ResourceLocation[] frames = new ResourceLocation[numImages];
        int[] delays = new int[numImages];
        int width = 0;
        int height = 0;

        for (int i = 0; i < numImages; i++) {
            BufferedImage frame = reader.read(i);
            if (i == 0) {
                width = frame.getWidth();
                height = frame.getHeight();
            }
            DynamicTexture dyn = new DynamicTexture(frame);
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("cape_gif_" + name.toLowerCase() + "_f" + i, dyn);
            frames[i] = loc;
            delays[i] = 100;
        }

        reader.dispose();
        stream.close();

        return new CapeTexture(name, frames, delays, width, height);
    }

    public CapeTexture getCapeTexture(String name) {
        if (name == null) {
            return null;
        }
        CapeTexture ct = this.loadedCapes.get(name.toLowerCase());
        if (ct == null) {
            reloadCapes();
            ct = this.loadedCapes.get(name.toLowerCase());
        }
        return ct;
    }

    public String[] getAvailableCapeNames() {
        if (this.availableNames.isEmpty()) {
            reloadCapes();
        }
        return this.availableNames.toArray(new String[0]);
    }

    public void openCapesFolder() {
        new Thread(() -> {
            try {
                if (!this.capesDir.exists()) {
                    this.capesDir.mkdirs();
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(this.capesDir);
                }
            } catch (Exception ignored) {
            }
        }, "KosovoCapeFolderOpener").start();
    }

    public void promptUploadCape() {
        new Thread(() -> {
            try {
                File selectedFile = null;
                try {
                    FileDialog dialog = new FileDialog((Frame) null, "Select Cape Image (.png, .gif)", FileDialog.LOAD);
                    dialog.setFilenameFilter((dir, name) -> {
                        String lower = name.toLowerCase();
                        return lower.endsWith(".png") || lower.endsWith(".gif");
                    });
                    dialog.setVisible(true);
                    String file = dialog.getFile();
                    String dir = dialog.getDirectory();
                    if (file != null && dir != null) {
                        selectedFile = new File(dir, file);
                    }
                } catch (Throwable fallback) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Select Cape Image");
                    chooser.setFileFilter(new FileNameExtensionFilter("Cape Images (*.png, *.gif)", "png", "gif"));
                    int result = chooser.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFile = chooser.getSelectedFile();
                    }
                }

                if (selectedFile != null && selectedFile.exists()) {
                    File target = new File(this.capesDir, selectedFile.getName());
                    FileUtils.copyFile(selectedFile, target);
                    reloadCapes();

                    String nameWithoutExt = selectedFile.getName().substring(0, selectedFile.getName().lastIndexOf('.'));
                    String displayName = Character.toUpperCase(nameWithoutExt.charAt(0)) + nameWithoutExt.substring(1);

                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        Capes.getInstance().getCape().setModes(getAvailableCapeNames());
                        Capes.getInstance().getCape().setValue(displayName);
                        if (Minecraft.getMinecraft().thePlayer != null) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fImported cape: §e" + displayName));
                        }
                    });
                }
            } catch (Exception e) {
                if (Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§c[Kosovo] §fFailed to import cape: " + e.getMessage()));
                }
            }
        }, "KosovoCapeUploader").start();
    }
}
