package com.mrailouis.kosovoclient.features.impl.sounds;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FileUtils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

@Getter
public class CustomWinSound extends Module {
    private static final String[] SOUND_PRESETS = {
            "Level Up", "Fireworks", "Fireworks Blast", "Dragon Death", "Wither Death",
            "Thunder", "Orb", "Pling", "Anvil", "Custom File"
    };

    private static final CustomWinSound INSTANCE = new CustomWinSound();

    private final ModeSetting sound = new ModeSetting("Sound", "Sound effect to play on victory.", "Level Up", SOUND_PRESETS);
    private final NumberSetting pitch = new NumberSetting("Pitch", "Sound pitch frequency multiplier.", 1.0, 0.5, 2.0, 0.1);
    private final NumberSetting volume = new NumberSetting("Volume", "Playback volume level.", 1.0, 0.1, 2.0, 0.1);
    private final BooleanSetting uploadSound = new BooleanSetting("Upload Sound", "Select a custom .wav audio file.", false, () -> promptUpload());
    private final BooleanSetting openFolder = new BooleanSetting("Open Folder", "Open the win sounds folder.", false, () -> openFolder());

    private final File soundsDir;
    private long lastWinSoundTime = 0L;

    public static CustomWinSound getInstance() {
        return INSTANCE;
    }

    private CustomWinSound() {
        super("Custom Win Sound", "Plays a custom sound whenever VICTORY appears in a title or chat.", Category.SOUNDS, true);
        registerSetting(this.sound);
        registerSetting(this.pitch);
        registerSetting(this.volume);
        registerSetting(this.uploadSound);
        registerSetting(this.openFolder);

        Minecraft mc = Minecraft.getMinecraft();
        File baseDir = mc.mcDataDir != null ? mc.mcDataDir : new File(".");
        this.soundsDir = new File(baseDir, "kosovoclient/sounds/wins");
        if (!this.soundsDir.exists()) {
            this.soundsDir.mkdirs();
        }
    }

    public void checkTitle(String titleText) {
        if (!isEnabled() || titleText == null) {
            return;
        }
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(titleText).toUpperCase();
        if (stripped.contains("VICTORY") || stripped.contains("WINNER") || stripped.contains("YOU WON") || stripped.contains("CHAMPION")) {
            triggerWinSound();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2 || !isEnabled()) {
            return;
        }

        String raw = event.message.getUnformattedText();
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(raw);
        if (stripped == null || stripped.isEmpty()) {
            return;
        }

        String upper = stripped.toUpperCase().trim();
        if (upper.startsWith("VICTORY!") || upper.contains("VICTORY!") || upper.startsWith("YOU WON!") || upper.contains("1ST PLACE!")) {
            triggerWinSound();
        }
    }

    public void triggerWinSound() {
        long now = System.currentTimeMillis();
        if (now - this.lastWinSoundTime < 4000L) {
            return;
        }
        this.lastWinSoundTime = now;

        String selected = this.sound.getValue();
        float p = this.pitch.getValue().floatValue();
        float v = this.volume.getValue().floatValue();

        if ("Custom File".equalsIgnoreCase(selected)) {
            File[] customFiles = this.soundsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (customFiles != null && customFiles.length > 0) {
                SoundPlayer.playCustomWav(customFiles[0], v);
            } else {
                SoundPlayer.playMinecraftSound("Level Up", p, v);
            }
        } else {
            SoundPlayer.playMinecraftSound(selected, p, v);
        }
    }

    private void promptUpload() {
        new Thread(() -> {
            try {
                File selectedFile = null;
                try {
                    FileDialog dialog = new FileDialog((Frame) null, "Select Win Sound (.wav)", FileDialog.LOAD);
                    dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".wav"));
                    dialog.setVisible(true);
                    String file = dialog.getFile();
                    String dir = dialog.getDirectory();
                    if (file != null && dir != null) {
                        selectedFile = new File(dir, file);
                    }
                } catch (Throwable fallback) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Select Win Sound (.wav)");
                    chooser.setFileFilter(new FileNameExtensionFilter("WAV Audio (*.wav)", "wav"));
                    int result = chooser.showOpenDialog(null);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFile = chooser.getSelectedFile();
                    }
                }

                if (selectedFile != null && selectedFile.exists()) {
                    final File fileToCopy = selectedFile;
                    File target = new File(this.soundsDir, "custom.wav");
                    FileUtils.copyFile(fileToCopy, target);
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        this.sound.setValue("Custom File");
                        if (Minecraft.getMinecraft().thePlayer != null) {
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fImported win sound: §e" + fileToCopy.getName()));
                        }
                    });
                }
            } catch (Exception e) {
                if (Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§c[Kosovo] §fFailed to import win sound: " + e.getMessage()));
                }
            }
        }, "KosovoWinSoundUploader").start();
    }

    private void openFolder() {
        new Thread(() -> {
            try {
                if (!this.soundsDir.exists()) {
                    this.soundsDir.mkdirs();
                }
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(this.soundsDir);
                }
            } catch (Exception ignored) {
            }
        }, "KosovoWinSoundFolderOpener").start();
    }
}
