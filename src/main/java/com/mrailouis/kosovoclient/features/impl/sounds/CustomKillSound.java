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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class CustomKillSound extends Module {
    private static final String[] SOUND_PRESETS = {
            "Ding", "Orb", "Pling", "Anvil", "Thunder", "Fireworks", "Fireworks Blast",
            "Dragon Death", "Wither Death", "Blaze Death", "Golem Death", "Wolf Death",
            "Bow Hit", "Explode", "Bass", "Harp", "Custom File"
    };

    private static final CustomKillSound INSTANCE = new CustomKillSound();

    private final ModeSetting sound = new ModeSetting("Sound", "Kill sound effect to play.", "Ding", SOUND_PRESETS);
    private final NumberSetting pitch = new NumberSetting("Pitch", "Sound pitch frequency multiplier.", 1.0, 0.5, 2.0, 0.1);
    private final NumberSetting volume = new NumberSetting("Volume", "Playback volume level.", 1.0, 0.1, 2.0, 0.1);
    private final BooleanSetting uploadSound = new BooleanSetting("Upload Sound", "Select a custom .wav audio file.", false, () -> promptUpload());
    private final BooleanSetting openFolder = new BooleanSetting("Open Folder", "Open the kill sounds folder.", false, () -> openFolder());

    private final File soundsDir;
    private long lastKillSoundTime = 0L;

    private static final Pattern[] KILL_PATTERNS = new Pattern[]{
            Pattern.compile("^(\\w+) was (?:slain|shot|killed|thrown|pushed|blown up|struck|burnt|finished|annihilated) by (\\w+)"),
            Pattern.compile("^(\\w+) was killed by (\\w+)"),
            Pattern.compile("^(\\w+) died to (\\w+)"),
            Pattern.compile("^(\\w+) was knocked into the void by (\\w+)"),
            Pattern.compile("^(\\w+) was thrown into the void by (\\w+)"),
            Pattern.compile("^(\\w+) got rekt by (\\w+)"),
            Pattern.compile("^(\\w+) was turned to dust by (\\w+)"),
            Pattern.compile("^(\\w+) had their head blown off by (\\w+)"),
            Pattern.compile("^(\\w+) was pummeled by (\\w+)"),
            Pattern.compile("^(\\w+) was struck down by (\\w+)"),
            Pattern.compile("^(\\w+) was brutally crushed by (\\w+)"),
            Pattern.compile("^(\\w+) was deleted by (\\w+)"),
            Pattern.compile("^(\\w+) was sniped by (\\w+)"),
            Pattern.compile("^(\\w+) was squashed by (\\w+)"),
            Pattern.compile("^(\\w+) lost a duel against (\\w+)"),
            Pattern.compile("^KILL! You killed (\\w+)"),
            Pattern.compile("^You killed (\\w+)"),
            Pattern.compile("^(\\w+) (?:was|got) killed! \\+(\\d+) Coins")
    };

    public static CustomKillSound getInstance() {
        return INSTANCE;
    }

    private CustomKillSound() {
        super("Custom Kill Sound", "Plays a custom sound whenever you kill a player.", Category.SOUNDS, true);
        registerSetting(this.sound);
        registerSetting(this.pitch);
        registerSetting(this.volume);
        registerSetting(this.uploadSound);
        registerSetting(this.openFolder);

        Minecraft mc = Minecraft.getMinecraft();
        File baseDir = mc.mcDataDir != null ? mc.mcDataDir : new File(".");
        this.soundsDir = new File(baseDir, "kosovoclient/sounds/kills");
        if (!this.soundsDir.exists()) {
            this.soundsDir.mkdirs();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2 || !isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }

        String raw = event.message.getUnformattedText();
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(raw);
        if (stripped == null || stripped.isEmpty()) {
            return;
        }

        String username = mc.thePlayer.getName();
        if (username == null) {
            return;
        }

        for (Pattern pattern : KILL_PATTERNS) {
            Matcher matcher = pattern.matcher(stripped.trim());
            if (matcher.find()) {
                if (matcher.groupCount() >= 2) {
                    String victim = matcher.group(1);
                    String killer = matcher.group(2);
                    if (killer != null && killer.equalsIgnoreCase(username) && !victim.equalsIgnoreCase(username)) {
                        triggerKillSound();
                        return;
                    }
                } else if (matcher.groupCount() >= 1) {
                    if (stripped.contains("You killed") || stripped.contains("KILL!")) {
                        triggerKillSound();
                        return;
                    }
                }
            }
        }
    }

    public void triggerKillSound() {
        long now = System.currentTimeMillis();
        if (now - this.lastKillSoundTime < 300L) {
            return;
        }
        this.lastKillSoundTime = now;

        String selected = this.sound.getValue();
        float p = this.pitch.getValue().floatValue();
        float v = this.volume.getValue().floatValue();

        if ("Custom File".equalsIgnoreCase(selected)) {
            File[] customFiles = this.soundsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));
            if (customFiles != null && customFiles.length > 0) {
                SoundPlayer.playCustomWav(customFiles[0], v);
            } else {
                SoundPlayer.playMinecraftSound("Ding", p, v);
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
                    FileDialog dialog = new FileDialog((Frame) null, "Select Kill Sound (.wav)", FileDialog.LOAD);
                    dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".wav"));
                    dialog.setVisible(true);
                    String file = dialog.getFile();
                    String dir = dialog.getDirectory();
                    if (file != null && dir != null) {
                        selectedFile = new File(dir, file);
                    }
                } catch (Throwable fallback) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Select Kill Sound (.wav)");
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
                            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§a[Kosovo] §fImported kill sound: §e" + fileToCopy.getName()));
                        }
                    });
                }
            } catch (Exception e) {
                if (Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§c[Kosovo] §fFailed to import kill sound: " + e.getMessage()));
                }
            }
        }, "KosovoKillSoundUploader").start();
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
        }, "KosovoKillSoundFolderOpener").start();
    }
}
