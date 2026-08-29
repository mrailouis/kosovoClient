package com.mrailouis.kosovoclient.features.impl.hud;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import lombok.Getter;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class CPS extends HudModule {
    private static final CPS INSTANCE = new CPS();

    private final BooleanSetting showLeft = new BooleanSetting("Show Left", "Display left click CPS.", true);
    private final BooleanSetting showRight = new BooleanSetting("Show Right", "Display right click CPS.", true);
    private final BooleanSetting showText = new BooleanSetting("Show Text", "Display button labels and CPS suffix.", true);
    private final ModeSetting orientation = new ModeSetting("Orientation", "Layout orientation when showing multiple buttons.", "Horizontal", new String[]{"Horizontal", "Vertical"});

    private final List<Long> leftClicks = new CopyOnWriteArrayList<Long>();
    private final List<Long> rightClicks = new CopyOnWriteArrayList<Long>();

    public static CPS getInstance() {
        return INSTANCE;
    }

    private CPS() {
        super("CPS", "Displays your clicks per second.", 10.0f, 10.0f);
        registerSetting(this.showLeft);
        registerSetting(this.showRight);
        registerSetting(this.showText);
        registerSetting(this.orientation);
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!event.buttonstate) {
            return;
        }
        long now = System.currentTimeMillis();
        if (event.button == 0) {
            this.leftClicks.add(now);
        } else if (event.button == 1) {
            this.rightClicks.add(now);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        this.leftClicks.removeIf(time -> now - time > 1000L);
        this.rightClicks.removeIf(time -> now - time > 1000L);
    }

    public int getLeftCPS() {
        long now = System.currentTimeMillis();
        this.leftClicks.removeIf(time -> now - time > 1000L);
        return this.leftClicks.size();
    }

    public int getRightCPS() {
        long now = System.currentTimeMillis();
        this.rightClicks.removeIf(time -> now - time > 1000L);
        return this.rightClicks.size();
    }

    @Override
    public List<String> getLines(boolean example) {
        int left = example ? 12 : getLeftCPS();
        int right = example ? 10 : getRightCPS();

        boolean leftEnabled = this.showLeft.isEnabled();
        boolean rightEnabled = this.showRight.isEnabled();
        boolean text = this.showText.isEnabled();
        boolean vertical = this.orientation.is("Vertical");

        if (leftEnabled && rightEnabled) {
            if (vertical) {
                List<String> list = new ArrayList<String>();
                list.add(text ? ("L: " + left + " CPS") : String.valueOf(left));
                list.add(text ? ("R: " + right + " CPS") : String.valueOf(right));
                return list;
            } else {
                return Collections.singletonList(text ? ("L: " + left + " | R: " + right + " CPS") : (left + " | " + right));
            }
        } else if (leftEnabled) {
            return Collections.singletonList(text ? (left + " CPS") : String.valueOf(left));
        } else if (rightEnabled) {
            return Collections.singletonList(text ? (right + " CPS") : String.valueOf(right));
        } else {
            return Collections.singletonList(text ? (left + " CPS") : String.valueOf(left));
        }
    }
}
