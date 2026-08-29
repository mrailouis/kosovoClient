package com.mrailouis.kosovoclient.features.impl.chat;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import lombok.Getter;

@Getter
public class InfiniteChat extends Module {
    private static final InfiniteChat INSTANCE = new InfiniteChat();

    private final BooleanSetting unlimited = new BooleanSetting("Unlimited", "Store unlimited chat history.", true);
    private final NumberSetting maxLines = new NumberSetting("Max Lines", "Custom line limit if unlimited is disabled.", 10000.0, 100.0, 100000.0, 500.0);

    public static InfiniteChat getInstance() {
        return INSTANCE;
    }

    private InfiniteChat() {
        super("Infinite Chat", "Removes the 100-line chat history limit allowing infinite scrolling.", Category.CHAT, true);
        registerSetting(this.unlimited);
        registerSetting(this.maxLines);
    }

    public int getMaxLinesLimit() {
        if (this.unlimited.isEnabled()) {
            return Integer.MAX_VALUE;
        }
        return this.maxLines.getValue().intValue();
    }
}
