package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import lombok.Getter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Getter
public class Clock extends HudModule {
    private static final Clock INSTANCE = new Clock();

    private final ModeSetting format = new ModeSetting("Format", "Clock time format.", "12-Hour", new String[]{"12-Hour", "24-Hour"});
    private final BooleanSetting showSeconds = new BooleanSetting("Show Seconds", "Display seconds in the clock.", false);

    private static final DateTimeFormatter FORMAT_12_SEC = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter FORMAT_12 = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter FORMAT_24_SEC = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_24 = DateTimeFormatter.ofPattern("HH:mm");

    public static Clock getInstance() {
        return INSTANCE;
    }

    private Clock() {
        super("Clock", "Displays the real-world current time.", 10.0f, 76.0f);
        registerSetting(this.format);
        registerSetting(this.showSeconds);
    }

    @Override
    public List<String> getLines(boolean example) {
        DateTimeFormatter formatter;
        if (this.format.is("24-Hour")) {
            formatter = this.showSeconds.isEnabled() ? FORMAT_24_SEC : FORMAT_24;
        } else {
            formatter = this.showSeconds.isEnabled() ? FORMAT_12_SEC : FORMAT_12;
        }

        LocalTime time = example ? LocalTime.of(16, 20, 0) : LocalTime.now();
        return Collections.singletonList(time.format(formatter));
    }
}
