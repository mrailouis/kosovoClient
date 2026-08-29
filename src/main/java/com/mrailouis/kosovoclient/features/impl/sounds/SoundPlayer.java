package com.mrailouis.kosovoclient.features.impl.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundPlayer {

    public static void playMinecraftSound(String soundName, float pitch, float volume) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        String actualName = soundName;
        if ("Orb".equalsIgnoreCase(soundName) || "Level Up".equalsIgnoreCase(soundName)) {
            actualName = "random.levelup";
        } else if ("Ding".equalsIgnoreCase(soundName) || "Exp".equalsIgnoreCase(soundName)) {
            actualName = "random.orb";
        } else if ("Anvil".equalsIgnoreCase(soundName)) {
            actualName = "random.anvil_land";
        } else if ("Thunder".equalsIgnoreCase(soundName)) {
            actualName = "ambient.weather.thunder";
        } else if ("Fireworks".equalsIgnoreCase(soundName)) {
            actualName = "fireworks.launch";
        } else if ("Fireworks Blast".equalsIgnoreCase(soundName)) {
            actualName = "fireworks.blast";
        } else if ("Dragon Death".equalsIgnoreCase(soundName) || "Dragon".equalsIgnoreCase(soundName)) {
            actualName = "mob.enderdragon.end";
        } else if ("Wither Death".equalsIgnoreCase(soundName) || "Wither".equalsIgnoreCase(soundName)) {
            actualName = "mob.wither.death";
        } else if ("Blaze Death".equalsIgnoreCase(soundName)) {
            actualName = "mob.blaze.death";
        } else if ("Wolf Death".equalsIgnoreCase(soundName)) {
            actualName = "mob.wolf.death";
        } else if ("Golem Death".equalsIgnoreCase(soundName)) {
            actualName = "mob.irongolem.death";
        } else if ("Bow Hit".equalsIgnoreCase(soundName) || "Pop".equalsIgnoreCase(soundName)) {
            actualName = "random.pop";
        } else if ("Explode".equalsIgnoreCase(soundName)) {
            actualName = "random.explode";
        } else if ("Pling".equalsIgnoreCase(soundName)) {
            actualName = "note.pling";
        } else if ("Bass".equalsIgnoreCase(soundName)) {
            actualName = "note.bass";
        } else if ("Harp".equalsIgnoreCase(soundName)) {
            actualName = "note.harp";
        }

        try {
            PositionedSoundRecord record = new PositionedSoundRecord(new ResourceLocation(actualName), volume, pitch, (float) mc.thePlayer.posX, (float) mc.thePlayer.posY, (float) mc.thePlayer.posZ);
            mc.getSoundHandler().playSound(record);
        } catch (Throwable ignored) {
        }
    }

    public static void playCustomWav(File wavFile, float volume) {
        if (wavFile == null || !wavFile.exists()) {
            return;
        }
        new Thread(() -> {
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
                AudioFormat baseFormat = stream.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );
                AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, stream);
                Clip clip = AudioSystem.getClip();
                clip.open(din);
                try {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(Math.max(0.0001f, volume)) * 20.0);
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                } catch (Throwable ignored) {
                }
                clip.start();
            } catch (Throwable ignored) {
            }
        }, "KosovoSoundPlayer").start();
    }
}
