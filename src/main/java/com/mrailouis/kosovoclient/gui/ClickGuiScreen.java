package com.mrailouis.kosovoclient.gui;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ColorSetting;
import com.mrailouis.kosovoclient.features.KeybindSetting;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.ModuleManager;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.features.Setting;
import com.mrailouis.kosovoclient.render.KawaseBlur;
import com.mrailouis.kosovoclient.render.NanoVGManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {

    private static final int PANEL_FILL_COLOR = 0xBF000000;
    private static final int PANEL_BORDER_COLOR = 0x22FFFFFF;
    private static final int TOP_BAR_COLOR = 0x55000000;
    private static final int SIDEBAR_COLOR = 0x2C000000;
    private static final int DIVIDER_COLOR = 0x33E0E0E0;
    private static final int KOSOVO_COLOR = 0xFFFF3333;
    private static final int CLIENT_COLOR = 0xFFFFFFFF;
    private static final int TOP_BAR_CATEGORY_COLOR = 0xFFA0A0A0;
    private static final int CATEGORY_RED = 0xFFFF3333;
    private static final int CATEGORY_HIGHLIGHT_COLOR = 0x26FF3333;
    private static final int SEARCH_TEXT_COLOR = 0xFFFFFFFF;
    private static final int SEARCH_PLACEHOLDER_COLOR = 0x66FFFFFF;
    private static final int SEARCH_SELECTION_COLOR = 0x55FF3333;
    private static final int SEARCH_CURSOR_COLOR = 0xFFFF3333;
    private static final int SEARCH_IDLE_COLOR = 0xFFA0A0A0;
    private static final int SEARCH_ACTIVE_COLOR = 0xFFFF3333;
    private static final int EDIT_HUDS_IDLE_COLOR = 0xFFA0A0A0;
    private static final int EDIT_HUDS_HOVER_COLOR = 0xFFFF3333;
    private static final int EDIT_HUDS_HOVER_BG = 0x1AFF3333;
    private static final float CORNER_RADIUS = 6.0f;
    private static final float ANIMATION_DURATION_MS = 250.0f;

    private static final String[] CATEGORY_NAMES = {
            "HUD", "Visuals", "Animations", "Player", "Chat", "Cosmetics", "Sounds"
    };

    private static final String[] CATEGORY_ICONS = {
            "assets/kosovoclient/icons/hud.png",
            "assets/kosovoclient/icons/visuals.png",
            "assets/kosovoclient/icons/animations.png",
            "assets/kosovoclient/icons/player.png",
            "assets/kosovoclient/icons/chat.png",
            "assets/kosovoclient/icons/cosmetics.png",
            "assets/kosovoclient/icons/sounds.png"
    };

    private long openTime;
    private long lastAnimTime;
    private int selectedCategory = 0;
    private float animatedCategoryIndex = 0.0f;

    private String searchQuery = "";
    private int cursorPosition = 0;
    private int selectionEnd = 0;
    private boolean searchFocused = false;
    private float searchFocusProgress = 0.0f;
    private float editHudsHoverProgress = 0.0f;
    private float animatedCursorX = 0.0f;
    private long lastTypeOrBlinkTime = 0;

    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float scrollVelocity = 0.0f;
    private NumberSetting draggingSlider = null;
    private ColorSetting draggingColorSetting = null;
    private int draggingColorComponent = 0;
    private float draggingColorPickerY = 0.0f;

    private float getSettingTotalHeight(Setting<?> s, float baseRowHeight, float cardWidth, float scale) {
        float h = baseRowHeight;
        if (s instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) s;
            float pickerWidth = cardWidth - 40.0f;
            float svHeight = pickerWidth * 0.40f;
            float barHeight = 8.0f * scale;
            float gap = 6.0f * scale;
            float fullPickerH = svHeight + gap + barHeight + gap + barHeight + gap;
            h += fullPickerH * cs.getExpandProgress();
        }
        return h;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        openTime = System.currentTimeMillis();
        lastAnimTime = System.currentTimeMillis();
        lastTypeOrBlinkTime = System.currentTimeMillis();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        draggingSlider = null;
        draggingColorSetting = null;
        draggingColorComponent = 0;
        for (Module m : ModuleManager.getInstance().getModules()) {
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof KeybindSetting) {
                    ((KeybindSetting) s).setListening(false);
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dw = Mouse.getEventDWheel();
        if (dw != 0) {
            float impulse = (dw > 0 ? -420.0f : 420.0f);
            scrollVelocity += impulse;
            targetScrollOffset += (dw > 0 ? -48.0f : 48.0f);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long currentTime = System.currentTimeMillis();
        float deltaSeconds = (currentTime - lastAnimTime) / 1000.0f;
        if (deltaSeconds <= 0.0f || deltaSeconds > 0.1f) {
            deltaSeconds = 0.016f;
        }
        lastAnimTime = currentTime;

        float targetCat = (float) selectedCategory;
        animatedCategoryIndex += (targetCat - animatedCategoryIndex) * Math.min(1.0f, deltaSeconds * 12.0f);

        float targetFocus = searchFocused ? 1.0f : 0.0f;
        searchFocusProgress += (targetFocus - searchFocusProgress) * Math.min(1.0f, deltaSeconds * 14.0f);

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();
        float pixelRatio = (float) mc.displayWidth / screenWidth;

        float elapsed = (float) (currentTime - openTime);
        float progress = Math.min(1.0f, Math.max(0.0f, elapsed / ANIMATION_DURATION_MS));
        float scale = 1.0f - (float) Math.pow(1.0f - progress, 3.0);

        if (scale > 0.001f) {
            float targetWidth = screenWidth * 0.52f;
            float topBarHeight = targetWidth * (9.0f / 16.0f) / 7.0f;
            float itemHeight = topBarHeight * 0.60f;
            float categoriesContentHeight = (topBarHeight * 0.18f) + (CATEGORY_NAMES.length * itemHeight) + (itemHeight * 0.35f);
            float editHudsButtonHeight = itemHeight * 0.82f;
            float editHudsSpacing = itemHeight * 0.45f;
            float targetHeight = topBarHeight + categoriesContentHeight + editHudsSpacing + editHudsButtonHeight + editHudsSpacing;

            float currentWidth = targetWidth * scale;
            float currentHeight = targetHeight * scale;
            float currentRadius = CORNER_RADIUS * scale;
            float currentX = (screenWidth - currentWidth) / 2.0f;
            float currentY = (screenHeight - currentHeight) / 2.0f;
            float curTopBarHeight = topBarHeight * scale;
            float sidebarWidth = currentWidth * 0.22f;
            float sidebarHeight = currentHeight - curTopBarHeight;
            float padX = sidebarWidth * 0.12f;

            KawaseBlur.getInstance().renderBlur(currentX, currentY, currentWidth, currentHeight, currentRadius);

            NanoVGManager nvg = NanoVGManager.getInstance();
            nvg.beginFrame(screenWidth, screenHeight, pixelRatio);
            nvg.drawGlassPanel(currentX, currentY, currentWidth, currentHeight, currentRadius, PANEL_FILL_COLOR, PANEL_BORDER_COLOR);
            nvg.drawRoundedRectVarying(currentX, currentY, currentWidth, curTopBarHeight, currentRadius, currentRadius, 0.0f, 0.0f, TOP_BAR_COLOR);
            nvg.drawRoundedRectVarying(currentX, currentY + curTopBarHeight, sidebarWidth, sidebarHeight, 0.0f, 0.0f, 0.0f, currentRadius, SIDEBAR_COLOR);
            nvg.drawLine(currentX, currentY + curTopBarHeight, currentX + currentWidth, currentY + curTopBarHeight, 1.0f, DIVIDER_COLOR);
            nvg.drawLine(currentX + sidebarWidth, currentY + curTopBarHeight, currentX + sidebarWidth, currentY + currentHeight, 1.0f, DIVIDER_COLOR);

            float availableTitleWidth = (sidebarWidth - padX * 2.0f);
            float baseTitleFontSize = curTopBarHeight * 0.40f;
            float measuredBaseWidth = nvg.getTextWidth("kosovoclient", NanoVGManager.FONT_INTER_BOLD, baseTitleFontSize);
            float titleFontSize = (measuredBaseWidth > 0.0f && measuredBaseWidth > availableTitleWidth)
                    ? (baseTitleFontSize * (availableTitleWidth / measuredBaseWidth))
                    : baseTitleFontSize;

            if (titleFontSize >= 1.0f) {
                float textY = currentY + (curTopBarHeight / 2.0f);
                float kosovoX = currentX + padX;
                nvg.drawText("kosovo", kosovoX, textY, NanoVGManager.FONT_INTER_BOLD, titleFontSize, KOSOVO_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
                float kosovoWidth = nvg.getTextWidth("kosovo", NanoVGManager.FONT_INTER_BOLD, titleFontSize);
                nvg.drawText("client", kosovoX + kosovoWidth, textY, NanoVGManager.FONT_INTER_BOLD, titleFontSize, CLIENT_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

                float dividerCenterX = currentX + sidebarWidth;
                float categoryFontSize = titleFontSize * 0.72f;
                nvg.drawText("|", dividerCenterX, textY, NanoVGManager.FONT_INTER, categoryFontSize, TOP_BAR_CATEGORY_COLOR, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

                float categoryTextX = dividerCenterX + padX;
                String headerCategoryName = searchQuery.isEmpty() ? CATEGORY_NAMES[selectedCategory] : "Search Results";
                nvg.drawText(headerCategoryName, categoryTextX, textY, NanoVGManager.FONT_INTER, categoryFontSize, TOP_BAR_CATEGORY_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            }

            float searchBarHeight = curTopBarHeight * 0.44f;
            float searchBarWidth = currentWidth * 0.14f;
            float searchBarX = currentX + currentWidth - padX - searchBarWidth;
            float searchBarY = currentY + (curTopBarHeight - searchBarHeight) / 2.0f;
            float searchBarRadius = 5.0f * scale;

            int currentSearchColor = interpolateColor(SEARCH_IDLE_COLOR, SEARCH_ACTIVE_COLOR, searchFocusProgress);
            nvg.drawRoundedRectOutline(searchBarX, searchBarY, searchBarWidth, searchBarHeight, searchBarRadius, 0.75f, currentSearchColor);

            float searchIconSize = searchBarHeight * 0.48f;
            float searchIconX = searchBarX + (searchBarHeight * 0.28f);
            float searchIconY = searchBarY + (searchBarHeight - searchIconSize) / 2.0f;
            nvg.drawVectorSearchIcon(searchIconX, searchIconY, searchIconSize, currentSearchColor, 1.2f);

            float searchFontSize = searchBarHeight * 0.44f;
            float searchTextStartX = searchIconX + searchIconSize + (searchBarHeight * 0.20f);
            float searchCenterY = searchBarY + (searchBarHeight / 2.0f);

            if (cursorPosition > searchQuery.length()) {
                cursorPosition = searchQuery.length();
            }
            if (selectionEnd > searchQuery.length()) {
                selectionEnd = searchQuery.length();
            }

            float targetCursorOffset = 0.0f;
            if (searchFocused && cursorPosition > 0) {
                String upToCursor = searchQuery.substring(0, cursorPosition);
                targetCursorOffset = nvg.getTextWidth(upToCursor, NanoVGManager.FONT_INTER, searchFontSize);
            }
            animatedCursorX += (targetCursorOffset - animatedCursorX) * Math.min(1.0f, deltaSeconds * 22.0f);

            if (searchFocused && cursorPosition != selectionEnd) {
                int selStart = Math.min(cursorPosition, selectionEnd);
                int selEnd = Math.max(cursorPosition, selectionEnd);
                String beforeSel = searchQuery.substring(0, selStart);
                String inSel = searchQuery.substring(selStart, selEnd);
                float selStartX = searchTextStartX + nvg.getTextWidth(beforeSel, NanoVGManager.FONT_INTER, searchFontSize);
                float selWidth = nvg.getTextWidth(inSel, NanoVGManager.FONT_INTER, searchFontSize);
                float selPadding = searchBarHeight * 0.10f;
                nvg.drawRoundedRect(selStartX, searchBarY + selPadding, selWidth, searchBarHeight - selPadding * 2.0f, 2.0f, SEARCH_SELECTION_COLOR);
            }

            if (searchQuery.isEmpty()) {
                nvg.drawText("Search...", searchTextStartX, searchCenterY, NanoVGManager.FONT_INTER, searchFontSize, SEARCH_PLACEHOLDER_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            } else {
                nvg.drawText(searchQuery, searchTextStartX, searchCenterY, NanoVGManager.FONT_INTER, searchFontSize, SEARCH_TEXT_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            }

            if (searchFocused) {
                long timeSinceLastAction = currentTime - lastTypeOrBlinkTime;
                boolean cursorVisible = (timeSinceLastAction % 1000) < 500;
                if (cursorVisible) {
                    float curX = searchTextStartX + animatedCursorX;
                    float cursorHalfHeight = searchBarHeight * 0.24f;
                    nvg.drawLine(curX, searchCenterY - cursorHalfHeight, curX, searchCenterY + cursorHalfHeight, 1.2f, SEARCH_CURSOR_COLOR);
                }
            }

            float curItemHeight = itemHeight * scale;
            float categoriesStartY = currentY + curTopBarHeight + (curTopBarHeight * 0.18f);

            float highlightY = categoriesStartY + (animatedCategoryIndex * curItemHeight);
            float highlightPaddingX = sidebarWidth * 0.08f;
            float highlightWidth = sidebarWidth - (highlightPaddingX * 2.0f);
            float highlightHeight = curItemHeight * 0.88f;
            float highlightRadius = 6.0f * scale;

            nvg.drawRoundedRect(currentX + highlightPaddingX, highlightY + (curItemHeight - highlightHeight) / 2.0f, highlightWidth, highlightHeight, highlightRadius, CATEGORY_HIGHLIGHT_COLOR);

            float catIconSize = curItemHeight * 0.40f;
            float catIconX = currentX + (sidebarWidth * 0.12f);
            float catTextX = catIconX + catIconSize + (sidebarWidth * 0.08f);
            float catFontSize = curItemHeight * 0.38f;

            for (int i = 0; i < CATEGORY_NAMES.length; i++) {
                float itemY = categoriesStartY + (i * curItemHeight);
                float iconY = itemY + (curItemHeight - catIconSize) / 2.0f;
                float centerY = itemY + (curItemHeight / 2.0f);

                int image = nvg.getImage(CATEGORY_ICONS[i]);
                nvg.drawImage(image, catIconX, iconY, catIconSize, catIconSize, 1.0f);
                nvg.drawText(CATEGORY_NAMES[i], catTextX, centerY, NanoVGManager.FONT_INTER, catFontSize, CATEGORY_RED, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            }

            float categoriesBottomY = categoriesStartY + (CATEGORY_NAMES.length * curItemHeight) + (curItemHeight * 0.35f);
            float dividerGap = sidebarWidth * 0.08f;
            nvg.drawLine(currentX + dividerGap, categoriesBottomY, currentX + sidebarWidth - dividerGap, categoriesBottomY, 1.0f, DIVIDER_COLOR);

            float curEditHudsSpacing = editHudsSpacing * scale;
            float curEditHudsHeight = editHudsButtonHeight * scale;
            float editHudsButtonWidth = sidebarWidth - (dividerGap * 2.0f);
            float editHudsButtonHeightCalculated = curEditHudsHeight;
            float editHudsButtonX = currentX + dividerGap;
            float editHudsButtonY = categoriesBottomY + curEditHudsSpacing;
            float editHudsRadius = 5.0f * scale;

            boolean editHudsHovered = mouseX >= editHudsButtonX && mouseX <= editHudsButtonX + editHudsButtonWidth &&
                    mouseY >= editHudsButtonY && mouseY <= editHudsButtonY + editHudsButtonHeightCalculated;

            float targetEditHudsHover = editHudsHovered ? 1.0f : 0.0f;
            editHudsHoverProgress += (targetEditHudsHover - editHudsHoverProgress) * Math.min(1.0f, deltaSeconds * 14.0f);

            int currentEditHudsColor = interpolateColor(EDIT_HUDS_IDLE_COLOR, EDIT_HUDS_HOVER_COLOR, editHudsHoverProgress);
            int currentEditHudsBg = interpolateColor(0x00000000, EDIT_HUDS_HOVER_BG, editHudsHoverProgress);

            if ((currentEditHudsBg & 0xFF000000) != 0) {
                nvg.drawRoundedRect(editHudsButtonX, editHudsButtonY, editHudsButtonWidth, editHudsButtonHeightCalculated, editHudsRadius, currentEditHudsBg);
            }
            nvg.drawRoundedRectOutline(editHudsButtonX, editHudsButtonY, editHudsButtonWidth, editHudsButtonHeightCalculated, editHudsRadius, 0.75f, currentEditHudsColor);

            float editHudsFontSize = editHudsButtonHeightCalculated * 0.44f;
            nvg.drawText("Edit HUDs", editHudsButtonX + (editHudsButtonWidth / 2.0f), editHudsButtonY + (editHudsButtonHeightCalculated / 2.0f), NanoVGManager.FONT_INTER, editHudsFontSize, currentEditHudsColor, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

            float contentX = currentX + sidebarWidth;
            float contentY = currentY + curTopBarHeight;
            float contentWidth = currentWidth - sidebarWidth;
            float contentHeight = currentHeight - curTopBarHeight;

            List<Module> visibleModules = searchQuery.isEmpty()
                    ? ModuleManager.getInstance().getModulesByCategory(Category.values()[selectedCategory])
                    : ModuleManager.getInstance().getModulesBySearch(searchQuery);

            float cardGap = curItemHeight * 0.28f;
            float cardWidth = contentWidth - (padX * 2.0f);
            float headerHeight = curItemHeight * 1.12f;
            float cardRadius = 6.0f * scale;
            float settingRowHeight = curItemHeight * 0.68f;

            float totalContentHeight = padX;
            for (Module mod : visibleModules) {
                float targetExpand = mod.isExpanded() ? 1.0f : 0.0f;
                float curExpand = mod.getExpandProgress();
                curExpand += (targetExpand - curExpand) * Math.min(1.0f, deltaSeconds * 14.0f);
                mod.setExpandProgress(curExpand);

                float targetToggle = mod.isEnabled() ? 1.0f : 0.0f;
                float curToggle = mod.getToggleProgress();
                curToggle += (targetToggle - curToggle) * Math.min(1.0f, deltaSeconds * 14.0f);
                mod.setToggleProgress(curToggle);

                for (Setting<?> s : mod.getSettings()) {
                    if (s instanceof BooleanSetting) {
                        BooleanSetting bs = (BooleanSetting) s;
                        float targetBS = bs.isEnabled() ? 1.0f : 0.0f;
                        float curBS = bs.getToggleProgress();
                        curBS += (targetBS - curBS) * Math.min(1.0f, deltaSeconds * 14.0f);
                        bs.setToggleProgress(curBS);
                    } else if (s instanceof ColorSetting) {
                        ColorSetting cs = (ColorSetting) s;
                        float targetCS = cs.isExpanded() ? 1.0f : 0.0f;
                        float curCS = cs.getExpandProgress();
                        curCS += (targetCS - curCS) * Math.min(1.0f, deltaSeconds * 14.0f);
                        cs.setExpandProgress(curCS);
                        cs.animate(deltaSeconds);
                    }
                }

                float settingsCountHeight = settingRowHeight * 0.20f;
                for (Setting<?> s : mod.getSettings()) {
                    settingsCountHeight += getSettingTotalHeight(s, settingRowHeight, cardWidth, scale);
                }
                totalContentHeight += headerHeight + (settingsCountHeight * curExpand) + cardGap;
            }
            totalContentHeight += padX;

            float maxScroll = Math.max(0.0f, totalContentHeight - contentHeight);
            targetScrollOffset = Math.max(0.0f, Math.min(maxScroll, targetScrollOffset));

            scrollOffset += scrollVelocity * deltaSeconds;
            scrollVelocity *= (float) Math.pow(0.04, deltaSeconds);
            if (Math.abs(scrollVelocity) < 1.0f) {
                scrollVelocity = 0.0f;
            }

            scrollOffset += (targetScrollOffset - scrollOffset) * Math.min(1.0f, deltaSeconds * 16.0f);
            scrollOffset = Math.max(0.0f, Math.min(maxScroll, scrollOffset));

            nvg.scissor(contentX, contentY, contentWidth, contentHeight);

            float currentCardY = contentY + padX - scrollOffset;
            for (Module mod : visibleModules) {
                float cardX = contentX + padX;
                float cardY = currentCardY;

                float curExpand = mod.getExpandProgress();
                float settingsTotalHeight = settingRowHeight * 0.20f;
                for (Setting<?> s : mod.getSettings()) {
                    settingsTotalHeight += getSettingTotalHeight(s, settingRowHeight, cardWidth, scale);
                }
                float totalCardHeight = headerHeight + (settingsTotalHeight * curExpand);

                boolean cardHovered = mouseX >= cardX && mouseX <= cardX + cardWidth &&
                        mouseY >= cardY && mouseY <= cardY + totalCardHeight &&
                        mouseY >= contentY && mouseY <= contentY + contentHeight;

                float targetHover = cardHovered ? 1.0f : 0.0f;
                float curHover = mod.getHoverProgress();
                curHover += (targetHover - curHover) * Math.min(1.0f, deltaSeconds * 14.0f);
                mod.setHoverProgress(curHover);

                int cardBg = interpolateColor(0x28000000, 0x44000000, mod.getHoverProgress());
                nvg.drawRoundedRect(cardX, cardY, cardWidth, totalCardHeight, cardRadius, cardBg);

                int idleOutline = interpolateColor(0xFF888888, CATEGORY_RED, mod.getHoverProgress());
                int activeOutline = CATEGORY_RED;
                int currentOutline = interpolateColor(idleOutline, activeOutline, mod.getToggleProgress());
                nvg.drawRoundedRectOutline(cardX, cardY, cardWidth, totalCardHeight, cardRadius, 0.75f, currentOutline);

                float modTitleSize = headerHeight * 0.36f;
                float modDescSize = headerHeight * 0.25f;
                nvg.drawText(mod.getName(), cardX + 16.0f, cardY + (headerHeight * 0.36f), NanoVGManager.FONT_INTER_BOLD, modTitleSize, 0xFFFFFFFF, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
                nvg.drawText(mod.getDescription(), cardX + 16.0f, cardY + (headerHeight * 0.70f), NanoVGManager.FONT_INTER, modDescSize, 0x99FFFFFF, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

                float chevronX = cardX + cardWidth - (headerHeight * 0.40f);
                float chevronY = cardY + (headerHeight / 2.0f);
                nvg.drawChevron(chevronX, chevronY, headerHeight * 0.28f, mod.getExpandProgress(), 0xFFA0A0A0, 1.4f);

                float toggleWidth = headerHeight * 0.82f;
                float toggleHeight = headerHeight * 0.44f;
                float toggleX = chevronX - (headerHeight * 0.35f) - toggleWidth;
                float toggleY = cardY + (headerHeight - toggleHeight) / 2.0f;
                nvg.drawPillToggle(toggleX, toggleY, toggleWidth, toggleHeight, mod.getToggleProgress(), CATEGORY_RED, 0x55333333, 0xFFFFFFFF);

                if (curExpand > 0.002f) {
                    float settingsY = cardY + headerHeight;
                    float visibleSettingsHeight = settingsTotalHeight * curExpand;

                    nvg.intersectScissor(cardX, settingsY, cardWidth, visibleSettingsHeight);
                    nvg.drawLine(cardX + 12.0f, settingsY, cardX + cardWidth - 12.0f, settingsY, 0.75f, 0x1AFFFFFF);

                    float startSettingY = settingsY + (settingRowHeight * 0.10f);
                    float currentSettingRowY = startSettingY;
                    for (int sIdx = 0; sIdx < mod.getSettings().size(); sIdx++) {
                        Setting<?> s = mod.getSettings().get(sIdx);
                        float rowY = currentSettingRowY;
                        float thisSettingHeight = getSettingTotalHeight(s, settingRowHeight, cardWidth, scale);

                        if (sIdx > 0) {
                            nvg.drawLine(cardX + 16.0f, rowY, cardX + cardWidth - 16.0f, rowY, 0.5f, 0x10FFFFFF);
                        }

                        float sTitleSize = settingRowHeight * 0.44f;
                        nvg.drawText(s.getName(), cardX + 20.0f, rowY + (settingRowHeight / 2.0f), NanoVGManager.FONT_INTER, sTitleSize, 0xFFEEEEEE, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

                        if (s instanceof BooleanSetting) {
                            BooleanSetting bs = (BooleanSetting) s;
                            float sToggleWidth = settingRowHeight * 0.88f;
                            float sToggleHeight = settingRowHeight * 0.48f;
                            float sToggleX = cardX + cardWidth - 20.0f - sToggleWidth;
                            float sToggleY = rowY + (settingRowHeight - sToggleHeight) / 2.0f;
                            nvg.drawPillToggle(sToggleX, sToggleY, sToggleWidth, sToggleHeight, bs.getToggleProgress(), CATEGORY_RED, 0x55333333, 0xFFFFFFFF);
                        } else if (s instanceof NumberSetting) {
                            NumberSetting ns = (NumberSetting) s;
                            float sliderWidth = cardWidth * 0.32f;
                            float sliderHeight = 5.0f * scale;
                            float sliderX = cardX + cardWidth - 20.0f - sliderWidth;
                            float sliderY = rowY + (settingRowHeight - sliderHeight) / 2.0f;
                            nvg.drawSlider(sliderX, sliderY, sliderWidth, sliderHeight, ns.getNormalized(), 0x55333333, CATEGORY_RED, 0xFFFFFFFF);

                            String valStr = String.format("%.1f", ns.getValue());
                            float valWidth = nvg.getTextWidth(valStr, NanoVGManager.FONT_INTER, sTitleSize);
                            nvg.drawText(valStr, sliderX - valWidth - 8.0f, rowY + (settingRowHeight / 2.0f), NanoVGManager.FONT_INTER, sTitleSize, 0xFFA0A0A0, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
                        } else if (s instanceof ModeSetting) {
                            ModeSetting ms = (ModeSetting) s;
                            String modeText = ms.getValue();
                            float modeTextWidth = nvg.getTextWidth(modeText, NanoVGManager.FONT_INTER, sTitleSize);
                            float modeButtonWidth = Math.max(cardWidth * 0.30f, modeTextWidth + (settingRowHeight * 1.10f));
                            float modeButtonHeight = settingRowHeight * 0.58f;
                            float modeButtonX = cardX + cardWidth - 20.0f - modeButtonWidth;
                            float modeButtonY = rowY + (settingRowHeight - modeButtonHeight) / 2.0f;
                            float modeRadius = 4.0f * scale;

                            boolean modeHovered = mouseX >= modeButtonX && mouseX <= modeButtonX + modeButtonWidth &&
                                    mouseY >= modeButtonY && mouseY <= modeButtonY + modeButtonHeight &&
                                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;

                            float targetModeHover = modeHovered ? 1.0f : 0.0f;
                            float curH = ms.getHoverProgress();
                            curH += (targetModeHover - curH) * Math.min(1.0f, deltaSeconds * 14.0f);
                            ms.setHoverProgress(curH);

                            int modeBg = interpolateColor(0x28000000, 0x55000000, ms.getHoverProgress());
                            int modeBorder = interpolateColor(0x33FFFFFF, CATEGORY_RED, ms.getHoverProgress());
                            nvg.drawRoundedRect(modeButtonX, modeButtonY, modeButtonWidth, modeButtonHeight, modeRadius, modeBg);
                            nvg.drawRoundedRectOutline(modeButtonX, modeButtonY, modeButtonWidth, modeButtonHeight, modeRadius, 0.75f, modeBorder);

                            nvg.drawText(modeText, modeButtonX + (modeButtonWidth / 2.0f), rowY + (settingRowHeight / 2.0f), NanoVGManager.FONT_INTER, sTitleSize, 0xFFFFFFFF, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
                        } else if (s instanceof KeybindSetting) {
                            KeybindSetting ks = (KeybindSetting) s;
                            String keyText = ks.isListening() ? "..." : "[" + ks.getKeyName() + "]";
                            float keyTextWidth = nvg.getTextWidth(keyText, NanoVGManager.FONT_INTER, sTitleSize);
                            float keyButtonWidth = Math.max(cardWidth * 0.25f, keyTextWidth + (settingRowHeight * 0.80f));
                            float keyButtonHeight = settingRowHeight * 0.58f;
                            float keyButtonX = cardX + cardWidth - 20.0f - keyButtonWidth;
                            float keyButtonY = rowY + (settingRowHeight - keyButtonHeight) / 2.0f;
                            float keyRadius = 4.0f * scale;

                            boolean keyHovered = mouseX >= keyButtonX && mouseX <= keyButtonX + keyButtonWidth &&
                                    mouseY >= keyButtonY && mouseY <= keyButtonY + keyButtonHeight &&
                                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;

                            float targetKeyHover = keyHovered || ks.isListening() ? 1.0f : 0.0f;
                            float curH = ks.getHoverProgress();
                            curH += (targetKeyHover - curH) * Math.min(1.0f, deltaSeconds * 14.0f);
                            ks.setHoverProgress(curH);

                            int keyBg = interpolateColor(0x28000000, 0x55000000, ks.getHoverProgress());
                            int keyBorder = interpolateColor(0x33FFFFFF, CATEGORY_RED, ks.getHoverProgress());
                            nvg.drawRoundedRect(keyButtonX, keyButtonY, keyButtonWidth, keyButtonHeight, keyRadius, keyBg);
                            nvg.drawRoundedRectOutline(keyButtonX, keyButtonY, keyButtonWidth, keyButtonHeight, keyRadius, 0.75f, keyBorder);

                            int textColor = ks.isListening() ? CATEGORY_RED : 0xFFFFFFFF;
                            nvg.drawText(keyText, keyButtonX + (keyButtonWidth / 2.0f), rowY + (settingRowHeight / 2.0f), NanoVGManager.FONT_INTER, sTitleSize, textColor, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
                        } else if (s instanceof ColorSetting) {
                            ColorSetting cs = (ColorSetting) s;
                            float previewWidth = settingRowHeight * 0.76f;
                            float previewHeight = settingRowHeight * 0.48f;
                            float previewX = cardX + cardWidth - 20.0f - previewWidth;
                            float previewY = rowY + (settingRowHeight - previewHeight) / 2.0f;
                            float previewRadius = 4.0f * scale;

                            boolean prevHovered = mouseX >= previewX && mouseX <= previewX + previewWidth &&
                                    mouseY >= previewY && mouseY <= previewY + previewHeight &&
                                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;

                            float targetPrevHover = prevHovered || cs.isExpanded() ? 1.0f : 0.0f;
                            float curH = cs.getHoverProgress();
                            curH += (targetPrevHover - curH) * Math.min(1.0f, deltaSeconds * 14.0f);
                            cs.setHoverProgress(curH);

                            int borderCol = interpolateColor(0x33FFFFFF, CATEGORY_RED, cs.getHoverProgress());
                            nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, previewRadius, 0xFF141414);
                            nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, previewRadius, cs.getColor());
                            nvg.drawRoundedRectOutline(previewX, previewY, previewWidth, previewHeight, previewRadius, 0.75f, borderCol);

                            if (cs.getExpandProgress() > 0.01f) {
                                float pickerX = cardX + 20.0f;
                                float pickerY = rowY + settingRowHeight;
                                float pickerWidth = cardWidth - 40.0f;
                                float svHeight = pickerWidth * 0.40f;
                                float barHeight = 8.0f * scale;
                                float gap = 6.0f * scale;
                                float fullPickerH = svHeight + gap + barHeight + gap + barHeight + gap;

                                nvg.intersectScissor(pickerX - 8.0f, pickerY, pickerWidth + 16.0f, fullPickerH * cs.getExpandProgress());

                                float curSVY = pickerY;
                                float curHueY = curSVY + svHeight + gap;
                                float curAlphaY = curHueY + barHeight + gap;

                                nvg.drawSVBox(pickerX, curSVY, pickerWidth, svHeight, 4.0f * scale, cs.getHue());
                                float svKnobX = pickerX + (cs.getSaturation() * pickerWidth);
                                float svKnobY = curSVY + ((1.0f - cs.getBrightness()) * svHeight);
                                nvg.drawPickerKnob(svKnobX, svKnobY, 4.0f * scale, cs.getColor());

                                nvg.drawHueBar(pickerX, curHueY, pickerWidth, barHeight, 3.0f * scale);
                                float hueKnobX = pickerX + (cs.getHue() * pickerWidth);
                                nvg.drawPickerKnob(hueKnobX, curHueY + barHeight / 2.0f, 4.0f * scale, java.awt.Color.HSBtoRGB(cs.getHue(), 1.0f, 1.0f));

                                nvg.drawAlphaBar(pickerX, curAlphaY, pickerWidth, barHeight, 3.0f * scale, cs.getColor() & 0x00FFFFFF);
                                float alphaKnobX = pickerX + (cs.getAlpha() * pickerWidth);
                                nvg.drawPickerKnob(alphaKnobX, curAlphaY + barHeight / 2.0f, 4.0f * scale, cs.getColor());

                                nvg.scissor(contentX, contentY, contentWidth, contentHeight);
                                nvg.intersectScissor(cardX, settingsY, cardWidth, visibleSettingsHeight);
                            }
                        }

                        currentSettingRowY += thisSettingHeight;
                    }
                    nvg.scissor(contentX, contentY, contentWidth, contentHeight);
                }

                currentCardY += totalCardHeight + cardGap;
            }

            nvg.resetScissor();
            nvg.endFrame();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int interpolateColor(int c1, int c2, float fraction) {
        float f = Math.max(0.0f, Math.min(1.0f, fraction));
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * f);
        int r = (int) (r1 + (r2 - r1) * f);
        int g = (int) (g1 + (g2 - g1) * f);
        int b = (int) (b1 + (b2 - b1) * f);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();

        float targetWidth = screenWidth * 0.52f;
        float topBarHeight = targetWidth * (9.0f / 16.0f) / 7.0f;
        float itemHeight = topBarHeight * 0.60f;
        float categoriesContentHeight = (topBarHeight * 0.18f) + (CATEGORY_NAMES.length * itemHeight) + (itemHeight * 0.35f);
        float editHudsButtonHeight = itemHeight * 0.82f;
        float editHudsSpacing = itemHeight * 0.45f;
        float targetHeight = topBarHeight + categoriesContentHeight + editHudsSpacing + editHudsButtonHeight + editHudsSpacing;

        float currentX = (screenWidth - targetWidth) / 2.0f;
        float currentY = (screenHeight - targetHeight) / 2.0f;
        float sidebarWidth = targetWidth * 0.22f;
        float padX = sidebarWidth * 0.12f;

        float searchBarHeight = topBarHeight * 0.44f;
        float searchBarWidth = targetWidth * 0.14f;
        float searchBarX = currentX + targetWidth - padX - searchBarWidth;
        float searchBarY = currentY + (topBarHeight - searchBarHeight) / 2.0f;

        if (mouseButton == 0) {
            if (mouseX >= searchBarX && mouseX <= searchBarX + searchBarWidth &&
                    mouseY >= searchBarY && mouseY <= searchBarY + searchBarHeight) {
                searchFocused = true;
                cursorPosition = searchQuery.length();
                selectionEnd = cursorPosition;
                lastTypeOrBlinkTime = System.currentTimeMillis();
                return;
            } else {
                searchFocused = false;
            }

            float categoriesStartY = currentY + topBarHeight + (topBarHeight * 0.18f);
            float categoriesBottomY = categoriesStartY + (CATEGORY_NAMES.length * itemHeight);

            if (mouseX >= currentX && mouseX <= currentX + sidebarWidth &&
                    mouseY >= categoriesStartY && mouseY <= categoriesBottomY) {
                int clickedIndex = (int) ((mouseY - categoriesStartY) / itemHeight);
                if (clickedIndex >= 0 && clickedIndex < CATEGORY_NAMES.length) {
                    selectedCategory = clickedIndex;
                    targetScrollOffset = 0.0f;
                    scrollOffset = 0.0f;
                    scrollVelocity = 0.0f;
                    return;
                }
            }
        }

        float contentX = currentX + sidebarWidth;
        float contentY = currentY + topBarHeight;
        float contentWidth = targetWidth - sidebarWidth;
        float contentHeight = targetHeight - topBarHeight;

        if (mouseX >= contentX && mouseX <= contentX + contentWidth &&
                mouseY >= contentY && mouseY <= contentY + contentHeight) {

            List<Module> visibleModules = searchQuery.isEmpty()
                    ? ModuleManager.getInstance().getModulesByCategory(Category.values()[selectedCategory])
                    : ModuleManager.getInstance().getModulesBySearch(searchQuery);

            float cardGap = itemHeight * 0.28f;
            float cardWidth = contentWidth - (padX * 2.0f);
            float headerHeight = itemHeight * 1.12f;
            float settingRowHeight = itemHeight * 0.68f;

            float currentCardY = contentY + padX - scrollOffset;
            for (Module mod : visibleModules) {
                float cardX = contentX + padX;
                float cardY = currentCardY;
                float settingsTotalHeight = settingRowHeight * 0.20f;
                for (Setting<?> s : mod.getSettings()) {
                    settingsTotalHeight += getSettingTotalHeight(s, settingRowHeight, cardWidth, 1.0f);
                }
                float totalCardHeight = headerHeight + (settingsTotalHeight * mod.getExpandProgress());

                if (mouseButton == 0) {
                    if (mouseX >= cardX && mouseX <= cardX + cardWidth &&
                            mouseY >= cardY && mouseY <= cardY + headerHeight) {
                        float chevronX = cardX + cardWidth - (headerHeight * 0.40f);
                        if (mouseX >= chevronX - 10.0f && mouseX <= cardX + cardWidth) {
                            mod.toggleExpanded();
                        } else {
                            mod.toggle();
                        }
                        return;
                    }

                    if (mod.getExpandProgress() > 0.05f &&
                            mouseX >= cardX && mouseX <= cardX + cardWidth &&
                            mouseY >= cardY + headerHeight && mouseY <= cardY + totalCardHeight) {
                        float currentSettingRowY = cardY + headerHeight + (settingRowHeight * 0.10f);
                        for (int sIdx = 0; sIdx < mod.getSettings().size(); sIdx++) {
                            Setting<?> s = mod.getSettings().get(sIdx);
                            float rowY = currentSettingRowY;
                            float thisSettingHeight = getSettingTotalHeight(s, settingRowHeight, cardWidth, 1.0f);

                            if (mouseY >= rowY && mouseY <= rowY + thisSettingHeight) {
                                if (s instanceof BooleanSetting) {
                                    ((BooleanSetting) s).toggle();
                                } else if (s instanceof NumberSetting) {
                                    NumberSetting ns = (NumberSetting) s;
                                    float sliderWidth = cardWidth * 0.32f;
                                    float sliderX = cardX + cardWidth - 20.0f - sliderWidth;
                                    float ratio = (mouseX - sliderX) / sliderWidth;
                                    ns.setNormalized(ratio);
                                    draggingSlider = ns;
                                } else if (s instanceof ModeSetting) {
                                    ((ModeSetting) s).cycle();
                                } else if (s instanceof KeybindSetting) {
                                    KeybindSetting ks = (KeybindSetting) s;
                                    ks.setListening(!ks.isListening());
                                } else if (s instanceof ColorSetting) {
                                    ColorSetting cs = (ColorSetting) s;
                                    float previewWidth = settingRowHeight * 0.76f;
                                    float previewHeight = settingRowHeight * 0.48f;
                                    float previewX = cardX + cardWidth - 20.0f - previewWidth;
                                    float previewY = rowY + (settingRowHeight - previewHeight) / 2.0f;

                                    if (mouseY <= rowY + settingRowHeight) {
                                        cs.toggleExpanded();
                                    } else if (cs.getExpandProgress() > 0.05f) {
                                        float pickerX = cardX + 20.0f;
                                        float pickerY = rowY + settingRowHeight;
                                        float pickerWidth = cardWidth - 40.0f;
                                        float svHeight = pickerWidth * 0.40f;
                                        float barHeight = 8.0f;
                                        float gap = 6.0f;

                                        float curSVY = pickerY;
                                        float curHueY = curSVY + svHeight + gap;
                                        float curAlphaY = curHueY + barHeight + gap;

                                        if (mouseY >= curSVY && mouseY <= curSVY + svHeight && mouseX >= pickerX && mouseX <= pickerX + pickerWidth) {
                                            cs.setTargetSaturation(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                                            cs.setTargetBrightness(Math.max(0.0f, Math.min(1.0f, 1.0f - (mouseY - curSVY) / svHeight)));
                                            cs.setSaturation(cs.getTargetSaturation());
                                            cs.setBrightness(cs.getTargetBrightness());
                                            cs.updateFromHSBA();
                                            draggingColorSetting = cs;
                                            draggingColorComponent = 1;
                                            draggingColorPickerY = curSVY;
                                        } else if (mouseY >= curHueY - 3.0f && mouseY <= curHueY + barHeight + 3.0f && mouseX >= pickerX && mouseX <= pickerX + pickerWidth) {
                                            cs.setTargetHue(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                                            cs.setHue(cs.getTargetHue());
                                            cs.updateFromHSBA();
                                            draggingColorSetting = cs;
                                            draggingColorComponent = 2;
                                            draggingColorPickerY = curSVY;
                                        } else if (mouseY >= curAlphaY - 3.0f && mouseY <= curAlphaY + barHeight + 3.0f && mouseX >= pickerX && mouseX <= pickerX + pickerWidth) {
                                            cs.setTargetAlpha(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                                            cs.setAlpha(cs.getTargetAlpha());
                                            cs.updateFromHSBA();
                                            draggingColorSetting = cs;
                                            draggingColorComponent = 3;
                                            draggingColorPickerY = curSVY;
                                        }
                                    }
                                }
                                return;
                            }
                            currentSettingRowY += thisSettingHeight;
                        }
                    }
                } else if (mouseButton == 1) {
                    if (mouseX >= cardX && mouseX <= cardX + cardWidth &&
                            mouseY >= cardY && mouseY <= cardY + headerHeight) {
                        mod.toggleExpanded();
                        return;
                    }

                    if (mod.getExpandProgress() > 0.05f &&
                            mouseX >= cardX && mouseX <= cardX + cardWidth &&
                            mouseY >= cardY + headerHeight && mouseY <= cardY + totalCardHeight) {
                        float currentSettingRowY = cardY + headerHeight + (settingRowHeight * 0.10f);
                        for (int sIdx = 0; sIdx < mod.getSettings().size(); sIdx++) {
                            Setting<?> s = mod.getSettings().get(sIdx);
                            float rowY = currentSettingRowY;
                            float thisSettingHeight = getSettingTotalHeight(s, settingRowHeight, cardWidth, 1.0f);

                            if (mouseY >= rowY && mouseY <= rowY + thisSettingHeight) {
                                if (s instanceof ModeSetting) {
                                    ((ModeSetting) s).cyclePrevious();
                                    return;
                                } else if (s instanceof KeybindSetting) {
                                    KeybindSetting ks = (KeybindSetting) s;
                                    ks.setKeyCode(Keyboard.KEY_NONE);
                                    ks.setListening(false);
                                    return;
                                } else if (s instanceof ColorSetting) {
                                    ((ColorSetting) s).toggleExpanded();
                                    return;
                                }
                            }
                            currentSettingRowY += thisSettingHeight;
                        }
                    }
                }

                currentCardY += totalCardHeight + cardGap;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingSlider = null;
        draggingColorSetting = null;
        draggingColorComponent = 0;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (clickedMouseButton == 0) {
            if (draggingSlider != null) {
                Minecraft mc = Minecraft.getMinecraft();
                ScaledResolution sr = new ScaledResolution(mc);
                float screenWidth = sr.getScaledWidth();
                float targetWidth = screenWidth * 0.52f;
                float sidebarWidth = targetWidth * 0.22f;
                float padX = sidebarWidth * 0.12f;
                float contentWidth = targetWidth - sidebarWidth;
                float cardWidth = contentWidth - (padX * 2.0f);
                float currentX = (screenWidth - targetWidth) / 2.0f;
                float cardX = currentX + sidebarWidth + padX;
                float sliderWidth = cardWidth * 0.32f;
                float sliderX = cardX + cardWidth - 20.0f - sliderWidth;
                float ratio = (mouseX - sliderX) / sliderWidth;
                draggingSlider.setNormalized(ratio);
            } else if (draggingColorSetting != null) {
                Minecraft mc = Minecraft.getMinecraft();
                ScaledResolution sr = new ScaledResolution(mc);
                float screenWidth = sr.getScaledWidth();
                float targetWidth = screenWidth * 0.52f;
                float sidebarWidth = targetWidth * 0.22f;
                float padX = sidebarWidth * 0.12f;
                float contentWidth = targetWidth - sidebarWidth;
                float cardWidth = contentWidth - (padX * 2.0f);
                float currentX = (screenWidth - targetWidth) / 2.0f;
                float cardX = currentX + sidebarWidth + padX;
                float pickerX = cardX + 20.0f;
                float pickerWidth = cardWidth - 40.0f;
                float svHeight = pickerWidth * 0.40f;

                if (draggingColorComponent == 1) {
                    draggingColorSetting.setTargetSaturation(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                    draggingColorSetting.setTargetBrightness(Math.max(0.0f, Math.min(1.0f, 1.0f - (mouseY - draggingColorPickerY) / svHeight)));
                } else if (draggingColorComponent == 2) {
                    draggingColorSetting.setTargetHue(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                } else if (draggingColorComponent == 3) {
                    draggingColorSetting.setTargetAlpha(Math.max(0.0f, Math.min(1.0f, (mouseX - pickerX) / pickerWidth)));
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        for (Module m : ModuleManager.getInstance().getModules()) {
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof KeybindSetting) {
                    KeybindSetting ks = (KeybindSetting) s;
                    if (ks.isListening()) {
                        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                            ks.setKeyCode(Keyboard.KEY_NONE);
                        } else {
                            ks.setKeyCode(keyCode);
                        }
                        ks.setListening(false);
                        return;
                    }
                }
            }
        }

        if (searchFocused) {
            lastTypeOrBlinkTime = System.currentTimeMillis();

            if (keyCode == Keyboard.KEY_ESCAPE) {
                searchFocused = false;
                return;
            }

            if (isCtrlKeyDown()) {
                if (keyCode == Keyboard.KEY_A) {
                    cursorPosition = 0;
                    selectionEnd = searchQuery.length();
                    return;
                }
                if (keyCode == Keyboard.KEY_C) {
                    if (cursorPosition != selectionEnd) {
                        int selStart = Math.min(cursorPosition, selectionEnd);
                        int selEnd = Math.max(cursorPosition, selectionEnd);
                        setClipboardString(searchQuery.substring(selStart, selEnd));
                    }
                    return;
                }
                if (keyCode == Keyboard.KEY_V) {
                    String clip = getClipboardString();
                    if (clip != null && !clip.isEmpty()) {
                        deleteSelection();
                        searchQuery = searchQuery.substring(0, cursorPosition) + clip + searchQuery.substring(cursorPosition);
                        cursorPosition += clip.length();
                        selectionEnd = cursorPosition;
                    }
                    return;
                }
            }

            if (keyCode == Keyboard.KEY_BACK) {
                if (cursorPosition != selectionEnd) {
                    deleteSelection();
                } else if (cursorPosition > 0 && !searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, cursorPosition - 1) + searchQuery.substring(cursorPosition);
                    cursorPosition--;
                    selectionEnd = cursorPosition;
                }
                return;
            }

            if (keyCode == Keyboard.KEY_DELETE) {
                if (cursorPosition != selectionEnd) {
                    deleteSelection();
                } else if (cursorPosition < searchQuery.length()) {
                    searchQuery = searchQuery.substring(0, cursorPosition) + searchQuery.substring(cursorPosition + 1);
                }
                return;
            }

            if (keyCode == Keyboard.KEY_LEFT) {
                if (isShiftKeyDown()) {
                    if (cursorPosition > 0) {
                        cursorPosition--;
                    }
                } else {
                    if (cursorPosition != selectionEnd) {
                        cursorPosition = Math.min(cursorPosition, selectionEnd);
                        selectionEnd = cursorPosition;
                    } else if (cursorPosition > 0) {
                        cursorPosition--;
                        selectionEnd = cursorPosition;
                    }
                }
                return;
            }

            if (keyCode == Keyboard.KEY_RIGHT) {
                if (isShiftKeyDown()) {
                    if (cursorPosition < searchQuery.length()) {
                        cursorPosition++;
                    }
                } else {
                    if (cursorPosition != selectionEnd) {
                        cursorPosition = Math.max(cursorPosition, selectionEnd);
                        selectionEnd = cursorPosition;
                    } else if (cursorPosition < searchQuery.length()) {
                        cursorPosition++;
                        selectionEnd = cursorPosition;
                    }
                }
                return;
            }

            if (keyCode == Keyboard.KEY_HOME) {
                cursorPosition = 0;
                if (!isShiftKeyDown()) {
                    selectionEnd = 0;
                }
                return;
            }

            if (keyCode == Keyboard.KEY_END) {
                cursorPosition = searchQuery.length();
                if (!isShiftKeyDown()) {
                    selectionEnd = cursorPosition;
                }
                return;
            }

            if (typedChar >= 32 && typedChar != 127) {
                deleteSelection();
                searchQuery = searchQuery.substring(0, cursorPosition) + typedChar + searchQuery.substring(cursorPosition);
                cursorPosition++;
                selectionEnd = cursorPosition;
                return;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.displayGuiScreen(null);
            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        }
    }

    private void deleteSelection() {
        if (cursorPosition != selectionEnd) {
            int selStart = Math.min(cursorPosition, selectionEnd);
            int selEnd = Math.max(cursorPosition, selectionEnd);
            searchQuery = searchQuery.substring(0, selStart) + searchQuery.substring(selEnd);
            cursorPosition = selStart;
            selectionEnd = selStart;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawDefaultBackground() {
    }

    @Override
    public void drawBackground(int tint) {
    }

    @Override
    public void drawWorldBackground(int tint) {
    }
}
