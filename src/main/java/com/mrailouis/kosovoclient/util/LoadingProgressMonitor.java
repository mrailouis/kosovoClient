package com.mrailouis.kosovoclient.util;

import lombok.Getter;
import net.minecraftforge.fml.common.ProgressManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoadingProgressMonitor {

    private static final List<ProgressManager.ProgressBar> activeBars = new CopyOnWriteArrayList<ProgressManager.ProgressBar>();

    @Getter
    private static volatile String currentTitle = "";

    @Getter
    private static volatile String currentMessage = "";

    @Getter
    private static volatile int currentStep = 0;

    @Getter
    private static volatile int currentSteps = 0;

    @Getter
    private static volatile float progress = 0.0f;

    public static void initialize() {
        try {
            Field barsField = ProgressManager.class.getDeclaredField("bars");
            barsField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(barsField, barsField.getModifiers() & ~Modifier.FINAL);
            barsField.set(null, new InterceptingList());
        } catch (Exception ignored) {
        }
    }

    public static void pollActiveProgress() {
        int size = activeBars.size();
        if (size == 0) {
            return;
        }
        try {
            ProgressManager.ProgressBar bar = activeBars.get(size - 1);
            if (bar != null) {
                currentTitle = bar.getTitle();
                currentMessage = bar.getMessage();
                currentStep = bar.getStep();
                currentSteps = bar.getSteps();
                if (currentSteps > 0) {
                    progress = (float) currentStep / (float) currentSteps;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    public static final class InterceptingList extends CopyOnWriteArrayList<ProgressManager.ProgressBar> {

        @Override
        public boolean add(ProgressManager.ProgressBar bar) {
            if (bar != null) {
                activeBars.add(bar);
                currentTitle = bar.getTitle();
                currentMessage = bar.getMessage();
                currentStep = bar.getStep();
                currentSteps = bar.getSteps();
                if (currentSteps > 0) {
                    progress = (float) currentStep / (float) currentSteps;
                }
            }
            return true;
        }

        @Override
        public boolean remove(Object o) {
            activeBars.remove(o);
            pollActiveProgress();
            return true;
        }

        @Override
        public Iterator<ProgressManager.ProgressBar> iterator() {
            pollActiveProgress();
            return new Iterator<ProgressManager.ProgressBar>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public ProgressManager.ProgressBar next() {
                    return null;
                }
            };
        }
    }
}
