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
import com.mrailouis.kosovoclient.render.NanoVGManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.nanovg.NanoVG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Windows 7 Explorer / Control Panel styled click-GUI: Aero glass title bar, an Explorer
 * toolbar (back/forward, breadcrumb, search), a navigation-tree sidebar and a flat list-view
 * content pane. Geometry is fixed-pixel (scaled only by {@link #uiScale(float)}), not stretched
 * to fill the window, to keep the compact desktop-application feel.
 */
public class ClickGuiScreen extends GuiScreen {

    // ---- Windows 7 Aero palette ----
    private static final int WINDOW_SHADOW_COLOR = 0x30000000;
    private static final int WINDOW_BORDER_OUTER = 0xFF5B7894;
    private static final int WINDOW_BORDER_INNER = 0x55FFFFFF;

    // Aero glass: the frame chrome (title bar, toolbar, nav pane) is deliberately translucent so
    // the Minecraft world faintly shows through, like real Aero glass - the content pane stays
    // fully opaque white since that is where the text-heavy module list needs to stay legible.
    private static final int TITLE_BAR_TOP = 0xD9DDF4FF;
    private static final int TITLE_BAR_MID = 0xE38CC8EC;
    private static final int TITLE_BAR_BOTTOM = 0xEE3D86BB;
    private static final int TITLE_BAR_BORDER = 0xFF17436B;

    private static final int TOOLBAR_BG = 0xE6F3F3F3;
    private static final int TOOLBAR_BORDER = 0xFFD3D3D3;

    private static final int NAV_PANE_TOP = 0xF5F1F5FA;
    private static final int NAV_PANE_BOTTOM = 0xF5E8EEF5;
    private static final int CONTENT_BG = 0xFFFFFFFF;
    private static final int DIVIDER_COLOR = 0xFFD8DEE6;

    private static final int TEXT_DARK = 0xFF1E1E1E;
    private static final int TEXT_MUTED = 0xFF666666;
    private static final int ACCENT_BLUE = 0xFF15539E;
    private static final int KOSOVO_RED = 0xFFD83A3F;

    private static final int NAV_SELECTED_TOP = 0xFFF1F8FE;
    private static final int NAV_SELECTED_BOTTOM = 0xFFD9ECFB;
    private static final int NAV_SELECTED_BORDER = 0xFF7DA2CE;
    private static final int NAV_HOVER_TOP = 0xFFF6FBFF;
    private static final int NAV_HOVER_BOTTOM = 0xFFE7F3FC;
    private static final int NAV_HOVER_BORDER = 0xFFC9E0F5;

    private static final int ROW_HOVER_TOP = 0xFFF1F8FE;
    private static final int ROW_HOVER_BOTTOM = 0xFFD9ECFB;
    private static final int ROW_HOVER_BORDER = 0xFFB8D6EF;

    private static final int SEARCH_BG = 0xFFFFFFFF;
    private static final int SEARCH_TEXT_COLOR = 0xFF1E1E1E;
    private static final int SEARCH_PLACEHOLDER_COLOR = 0xFF777777;
    private static final int SEARCH_SELECTION_COLOR = 0x557EB4EA;
    private static final int SEARCH_CURSOR_COLOR = 0xFF15539E;
    private static final int SEARCH_IDLE_BORDER = 0xFFA7B7C9;
    private static final int SEARCH_ACTIVE_BORDER = 0xFF3D94D9;

    private static final int BTN_TOP = 0xFFFFFFFF;
    private static final int BTN_BOTTOM = 0xFFDFE3E6;
    private static final int BTN_BORDER = 0xFF9BA7B0;
    private static final int BTN_HOVER_TOP = 0xFFF0F8FE;
    private static final int BTN_HOVER_BOTTOM = 0xFFD7ECFB;
    private static final int BTN_HOVER_BORDER = 0xFF7EB4EA;

    private static final float ANIMATION_DURATION_MS = 140.0f;

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

    private static final String[] CATEGORY_DESCRIPTIONS = {
            "Manage HUD modules", "Manage Visuals modules", "Manage Animations modules",
            "Manage Player modules", "Manage Chat modules", "Manage Cosmetics modules", "Manage Sounds modules"
    };

    // A category index in this array means "insert a section header labelled below, then this category".
    private static final int[] SECTION_STARTS_AT = {0, 2, 5};
    private static final String[] SECTION_LABELS = {"Favorites", "Libraries", "Other"};

    private long openTime;
    private long lastAnimTime;
    private int selectedCategory = 0;
    private float animatedHighlightY = -1.0f;

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

    private static final class NavRow {
        final boolean header;
        final String label;
        final int categoryIndex;
        float y;
        float height;

        NavRow(boolean header, String label, int categoryIndex) {
            this.header = header;
            this.label = label;
            this.categoryIndex = categoryIndex;
        }
    }

    private static final class Layout {
        float u;
        float winX, winY, winW, winH, radius;
        float titleBarH, toolbarH;
        float bodyY, bodyH;
        float sidebarW;
        float contentX, contentY, contentW, contentH;

        float ctrlW, ctrlH, closeX, closeW, maxX, minX, ctrlY;

        float navBtnSize, backX, fwdX, navBtnY;
        float breadcrumbX, breadcrumbY, breadcrumbW, breadcrumbH;
        float searchX, searchY, searchW, searchH;

        List<NavRow> navRows;
        float navListX, navListY, navListW;

        float editHudsX, editHudsY, editHudsW, editHudsH;

        float contentHeaderH;
        float rowHeight, settingRowHeight, rowPadX, iconSize, checkboxSize, arrowSize;
        float navRowHeight;
    }

    private Layout computeLayout(float screenWidth, float screenHeight) {
        Layout l = new Layout();
        // Kept narrow: at wider Minecraft GUI scales this used to blow every dimension up well
        // past its native Windows 7 size. Everything below is a literal pixel budget for u == 1.
        float u = Math.max(0.8f, Math.min(1.1f, screenHeight / 480.0f));
        l.u = u;

        l.winW = screenWidth * 0.52f;
        l.titleBarH = 31.0f * u;
        l.toolbarH = 36.0f * u;
        l.sidebarW = Math.min(178.0f * u, l.winW * 0.34f);
        l.radius = 6.0f * u;
        l.navRowHeight = 25.0f * u;

        List<NavRow> rows = new ArrayList<NavRow>();
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            for (int k = 0; k < SECTION_STARTS_AT.length; k++) {
                if (SECTION_STARTS_AT[k] == i) {
                    rows.add(new NavRow(true, SECTION_LABELS[k], -1));
                }
            }
            rows.add(new NavRow(false, CATEGORY_NAMES[i], i));
        }

        float rowY = 4.0f * u;
        for (NavRow row : rows) {
            row.height = row.header ? (23.0f * u) : l.navRowHeight;
            row.y = rowY;
            rowY += row.height;
        }
        float navContentHeight = rowY + (4.0f * u);
        l.navRows = rows;

        l.editHudsH = 27.0f * u;
        float editHudsSpacing = 8.0f * u;
        l.bodyH = navContentHeight + editHudsSpacing + l.editHudsH + (8.0f * u);

        l.winH = l.titleBarH + l.toolbarH + l.bodyH;
        l.winX = (screenWidth - l.winW) / 2.0f;
        l.winY = (screenHeight - l.winH) / 2.0f;

        l.bodyY = l.winY + l.titleBarH + l.toolbarH;
        l.contentX = l.winX + l.sidebarW;
        l.contentY = l.bodyY;
        l.contentW = l.winW - l.sidebarW;
        l.contentH = l.bodyH;

        l.navListX = l.winX;
        l.navListY = l.bodyY;
        l.navListW = l.sidebarW;

        l.editHudsW = l.sidebarW - (24.0f * u);
        l.editHudsX = l.winX + (12.0f * u);
        l.editHudsY = l.bodyY + navContentHeight + editHudsSpacing;

        // Title bar buttons form one attached group flush with the very top of the frame,
        // spanning the full title bar height - not floating standalone pills.
        l.ctrlH = l.titleBarH;
        l.ctrlY = l.winY;
        float ctrlW = 45.0f * u;
        float closeW = 48.0f * u;
        float ctrlGap = 0.0f;
        l.ctrlW = ctrlW;
        l.closeW = closeW;
        l.closeX = l.winX + l.winW - closeW;
        l.maxX = l.closeX - ctrlGap - ctrlW;
        l.minX = l.maxX - ctrlGap - ctrlW;

        l.navBtnSize = 28.0f * u;
        float toolbarPad = 8.0f * u;
        l.backX = l.winX + toolbarPad;
        l.fwdX = l.backX + l.navBtnSize + (2.0f * u);
        l.navBtnY = l.winY + l.titleBarH + (l.toolbarH - l.navBtnSize) / 2.0f;

        l.breadcrumbH = 27.0f * u;
        l.searchH = l.breadcrumbH;

        l.searchW = Math.min(175.0f * u, l.winW * 0.32f);
        l.searchX = l.winX + l.winW - toolbarPad - l.searchW;
        l.searchY = l.winY + l.titleBarH + (l.toolbarH - l.searchH) / 2.0f;

        l.breadcrumbX = l.fwdX + l.navBtnSize + (7.0f * u);
        l.breadcrumbY = l.winY + l.titleBarH + (l.toolbarH - l.breadcrumbH) / 2.0f;
        l.breadcrumbW = Math.max(20.0f * u, (l.searchX - (7.0f * u)) - l.breadcrumbX);

        l.contentHeaderH = 58.0f * u;
        l.rowHeight = 51.0f * u;
        l.settingRowHeight = 22.0f * u;
        l.rowPadX = 10.0f * u;
        l.iconSize = 19.0f * u;
        l.checkboxSize = 16.0f * u;
        l.arrowSize = 6.0f * u;

        return l;
    }

    private float getSettingTotalHeight(Setting<?> s, Layout l) {
        float h = l.settingRowHeight;
        if (s instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) s;
            float pickerWidth = (l.contentW - l.rowPadX * 2.0f) - 40.0f;
            float svHeight = pickerWidth * 0.40f;
            float barHeight = 7.0f * l.u;
            float gap = 5.0f * l.u;
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

        float targetFocus = searchFocused ? 1.0f : 0.0f;
        searchFocusProgress += (targetFocus - searchFocusProgress) * Math.min(1.0f, deltaSeconds * 14.0f);

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        float screenWidth = sr.getScaledWidth();
        float screenHeight = sr.getScaledHeight();
        float pixelRatio = (float) mc.displayWidth / screenWidth;

        float elapsed = (float) (currentTime - openTime);
        float openAlpha = Math.min(1.0f, Math.max(0.0f, elapsed / ANIMATION_DURATION_MS));

        Layout l = computeLayout(screenWidth, screenHeight);

        // Snap or ease the sidebar selection highlight onto the currently selected row. All
        // category rows share l.navRowHeight, so only the Y position needs to be animated.
        float targetHighlightY = l.navListY;
        for (NavRow row : l.navRows) {
            if (!row.header && row.categoryIndex == selectedCategory) {
                targetHighlightY = l.navListY + row.y;
                break;
            }
        }
        if (animatedHighlightY < 0.0f) {
            animatedHighlightY = targetHighlightY;
        } else {
            animatedHighlightY += (targetHighlightY - animatedHighlightY) * Math.min(1.0f, deltaSeconds * 16.0f);
        }

        if (openAlpha > 0.001f) {
            NanoVGManager nvg = NanoVGManager.getInstance();
            nvg.beginFrame(screenWidth, screenHeight, pixelRatio);
            nvg.setGlobalAlpha(openAlpha);

            // Soft drop shadow (Windows 7 windows do not glow, they cast a faint grey shadow)
            nvg.drawOuterGlow(l.winX, l.winY, l.winW, l.winH, l.radius, 4.0f * l.u, WINDOW_SHADOW_COLOR);

            // Aero glass title bar
            nvg.drawAeroTitleBar(l.winX, l.winY, l.winW, l.titleBarH, l.radius, l.radius, TITLE_BAR_TOP, TITLE_BAR_MID, TITLE_BAR_BOTTOM);

            // Explorer toolbar strip
            nvg.drawRoundedRect(l.winX, l.winY + l.titleBarH, l.winW, l.toolbarH, 0.0f, TOOLBAR_BG);

            // Navigation pane + content pane
            nvg.drawVerticalGradientRounded(l.winX, l.bodyY, l.sidebarW, l.bodyH, 0.0f, 0.0f, 0.0f, l.radius, NAV_PANE_TOP, NAV_PANE_BOTTOM);
            nvg.drawRoundedRectVarying(l.contentX, l.contentY, l.contentW, l.contentH, 0.0f, 0.0f, l.radius, 0.0f, CONTENT_BG);

            // Frame border + thin inner highlight (Aero bevel)
            nvg.drawRoundedRectOutline(l.winX, l.winY, l.winW, l.winH, l.radius, 1.0f, WINDOW_BORDER_OUTER);
            nvg.drawRoundedRectOutline(l.winX + 1.0f, l.winY + 1.0f, l.winW - 2.0f, l.winH - 2.0f, Math.max(0.0f, l.radius - 1.0f), 1.0f, WINDOW_BORDER_INNER);
            nvg.drawLine(l.winX, l.winY + l.titleBarH, l.winX + l.winW, l.winY + l.titleBarH, 1.0f, TITLE_BAR_BORDER);
            nvg.drawLine(l.winX, l.winY + l.titleBarH + l.toolbarH, l.winX + l.winW, l.winY + l.titleBarH + l.toolbarH, 1.0f, TOOLBAR_BORDER);
            nvg.drawLine(l.contentX, l.bodyY, l.contentX, l.bodyY + l.bodyH, 1.0f, 0xFFC5D1DD);
            nvg.drawLine(l.contentX + 1.0f, l.bodyY, l.contentX + 1.0f, l.bodyY + l.bodyH, 1.0f, 0xFFFFFFFF);

            drawTitleBar(nvg, l, mouseX, mouseY);
            drawToolbar(nvg, l, mouseX, mouseY, currentTime);
            drawSidebar(nvg, l, mouseX, mouseY);
            drawEditHudsButton(nvg, l, mouseX, mouseY, deltaSeconds);
            drawContent(nvg, l, mouseX, mouseY, deltaSeconds);

            nvg.resetScissor();
            nvg.setGlobalAlpha(1.0f);
            nvg.endFrame();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawTitleBar(NanoVGManager nvg, Layout l, int mouseX, int mouseY) {
        float iconSize = 16.0f * l.u;
        float iconX = l.winX + (8.0f * l.u);
        float iconY = l.winY + (l.titleBarH - iconSize) / 2.0f;
        nvg.drawVerticalGradientRounded(iconX, iconY, iconSize, iconSize, 2.0f, 0xFFE05B5F, KOSOVO_RED);
        nvg.drawRoundedRectOutline(iconX, iconY, iconSize, iconSize, 2.0f, 1.0f, 0x40000000);
        nvg.drawText("K", iconX + iconSize / 2.0f, iconY + iconSize / 2.0f + iconSize * 0.02f, NanoVGManager.FONT_INTER_BOLD, iconSize * 0.62f, 0xFFFFFFFF, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);

        float titleFontSize = 13.5f * l.u;
        nvg.drawText("Kosovo Client", iconX + iconSize + (6.0f * l.u), l.winY + l.titleBarH / 2.0f, NanoVGManager.FONT_INTER_BOLD, titleFontSize, 0xFFFFFFFF, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        boolean minHover = withinRect(mouseX, mouseY, l.minX, l.ctrlY, l.ctrlW, l.ctrlH);
        boolean maxHover = withinRect(mouseX, mouseY, l.maxX, l.ctrlY, l.ctrlW, l.ctrlH);
        boolean closeHover = withinRect(mouseX, mouseY, l.closeX, l.ctrlY, l.closeW, l.ctrlH);

        nvg.drawTitleBarControl(l.minX, l.ctrlY, l.ctrlW, l.ctrlH, NanoVGManager.TITLE_CONTROL_MINIMIZE, minHover);
        nvg.drawTitleBarControl(l.maxX, l.ctrlY, l.ctrlW, l.ctrlH, NanoVGManager.TITLE_CONTROL_MAXIMIZE, maxHover);
        nvg.drawTitleBarControl(l.closeX, l.ctrlY, l.closeW, l.ctrlH, NanoVGManager.TITLE_CONTROL_CLOSE, closeHover);
    }

    private void drawToolbar(NanoVGManager nvg, Layout l, int mouseX, int mouseY, long currentTime) {
        float backCx = l.backX + l.navBtnSize / 2.0f;
        float fwdCx = l.fwdX + l.navBtnSize / 2.0f;
        float navCy = l.navBtnY + l.navBtnSize / 2.0f;
        nvg.drawExplorerNavButton(backCx, navCy, l.navBtnSize / 2.0f, true, true, false);
        nvg.drawExplorerNavButton(fwdCx, navCy, l.navBtnSize / 2.0f, false, false, false);

        nvg.drawVerticalGradientRounded(l.breadcrumbX, l.breadcrumbY, l.breadcrumbW, l.breadcrumbH, 2.0f, 0xFFFFFFFF, 0xFFF8F8F8);
        nvg.drawRoundedRectOutline(l.breadcrumbX, l.breadcrumbY, l.breadcrumbW, l.breadcrumbH, 2.0f, 1.0f, BTN_BORDER);
        nvg.drawLine(l.breadcrumbX + 1.5f, l.breadcrumbY + 1.0f, l.breadcrumbX + l.breadcrumbW - 1.5f, l.breadcrumbY + 1.0f, 1.0f, 0x1A000000);

        String crumbCategory = searchQuery.isEmpty() ? CATEGORY_NAMES[selectedCategory] : "Search Results";
        float crumbFont = 11.5f * l.u;
        float crumbTextY = l.breadcrumbY + l.breadcrumbH / 2.0f;
        float crumbX = l.breadcrumbX + (7.0f * l.u);
        nvg.drawText("Kosovo Client", crumbX, crumbTextY, NanoVGManager.FONT_INTER, crumbFont, TEXT_DARK, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        float crumbAdvance = nvg.getTextWidth("Kosovo Client", NanoVGManager.FONT_INTER, crumbFont);
        float chevX = crumbX + crumbAdvance + (6.0f * l.u);
        nvg.drawChevron(chevX, crumbTextY, 5.0f * l.u, 0.0f, TEXT_MUTED, 1.1f);
        nvg.drawText(crumbCategory, chevX + (8.0f * l.u), crumbTextY, NanoVGManager.FONT_INTER, crumbFont, TEXT_DARK, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        int searchBorder = interpolateColor(SEARCH_IDLE_BORDER, SEARCH_ACTIVE_BORDER, searchFocusProgress);
        nvg.drawRoundedRect(l.searchX, l.searchY, l.searchW, l.searchH, 2.5f * l.u, SEARCH_BG);
        nvg.drawRoundedRectOutline(l.searchX, l.searchY, l.searchW, l.searchH, 2.5f * l.u, 1.0f, searchBorder);

        float iconSize = l.searchH * 0.55f;
        float iconX = l.searchX + (6.0f * l.u);
        float iconY = l.searchY + (l.searchH - iconSize) / 2.0f;
        nvg.drawVectorSearchIcon(iconX, iconY, iconSize, searchBorder, 1.1f);

        float searchFontSize = 11.5f * l.u;
        float textStartX = iconX + iconSize + (5.0f * l.u);
        float centerY = l.searchY + l.searchH / 2.0f;
        nvg.intersectScissor(l.searchX + 1.0f, l.searchY, l.searchW - (8.0f * l.u), l.searchH);

        if (cursorPosition > searchQuery.length()) {
            cursorPosition = searchQuery.length();
        }
        if (selectionEnd > searchQuery.length()) {
            selectionEnd = searchQuery.length();
        }

        float targetCursorOffset = 0.0f;
        if (searchFocused && cursorPosition > 0) {
            targetCursorOffset = nvg.getTextWidth(searchQuery.substring(0, cursorPosition), NanoVGManager.FONT_INTER, searchFontSize);
        }
        animatedCursorX += (targetCursorOffset - animatedCursorX) * 0.6f;

        if (searchFocused && cursorPosition != selectionEnd) {
            int selStart = Math.min(cursorPosition, selectionEnd);
            int selEnd = Math.max(cursorPosition, selectionEnd);
            float selStartX = textStartX + nvg.getTextWidth(searchQuery.substring(0, selStart), NanoVGManager.FONT_INTER, searchFontSize);
            float selWidth = nvg.getTextWidth(searchQuery.substring(selStart, selEnd), NanoVGManager.FONT_INTER, searchFontSize);
            nvg.drawRoundedRect(selStartX, l.searchY + 2.0f * l.u, selWidth, l.searchH - 4.0f * l.u, 1.0f, SEARCH_SELECTION_COLOR);
        }

        if (searchQuery.isEmpty()) {
            nvg.drawText("Search Kosovo Client", textStartX, centerY, NanoVGManager.FONT_INTER, searchFontSize, SEARCH_PLACEHOLDER_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        } else {
            nvg.drawText(searchQuery, textStartX, centerY, NanoVGManager.FONT_INTER, searchFontSize, SEARCH_TEXT_COLOR, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        }

        if (searchFocused && ((currentTime - lastTypeOrBlinkTime) % 1000) < 500) {
            float curX = textStartX + animatedCursorX;
            nvg.drawLine(curX, l.searchY + 3.0f * l.u, curX, l.searchY + l.searchH - 3.0f * l.u, 1.0f, SEARCH_CURSOR_COLOR);
        }
        nvg.resetScissor();
    }

    private void drawSidebar(NanoVGManager nvg, Layout l, int mouseX, int mouseY) {
        // Selection rectangle (smoothly slides between rows) - sized to a single nav row, never
        // the taller content-list row height, so it can never straddle two categories.
        nvg.drawVerticalGradientRounded(l.navListX + 6.0f * l.u, animatedHighlightY + 1.0f, l.navListW - 12.0f * l.u, l.navRowHeight - 2.0f, 2.0f, NAV_SELECTED_TOP, NAV_SELECTED_BOTTOM);
        nvg.drawRoundedRectOutline(l.navListX + 6.0f * l.u, animatedHighlightY + 1.0f, l.navListW - 12.0f * l.u, l.navRowHeight - 2.0f, 2.0f, 1.0f, NAV_SELECTED_BORDER);

        for (NavRow row : l.navRows) {
            float rowY = l.navListY + row.y;
            if (row.header) {
                float triSize = 4.0f * l.u;
                float tx = l.navListX + (8.0f * l.u);
                float ty = rowY + row.height / 2.0f;
                nvg.drawChevron(tx, ty, triSize, 0.0f, TEXT_MUTED, 1.1f);
                nvg.drawText(row.label, tx + triSize + (5.0f * l.u), ty, NanoVGManager.FONT_INTER_BOLD, 12.0f * l.u, TEXT_MUTED, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
                continue;
            }

            boolean isSelected = row.categoryIndex == selectedCategory;
            boolean isHovered = !isSelected && withinRect(mouseX, mouseY, l.navListX, rowY, l.navListW, row.height);
            if (isHovered) {
                nvg.drawVerticalGradientRounded(l.navListX + 6.0f * l.u, rowY + 1.0f, l.navListW - 12.0f * l.u, row.height - 2.0f, 2.0f, NAV_HOVER_TOP, NAV_HOVER_BOTTOM);
                nvg.drawRoundedRectOutline(l.navListX + 6.0f * l.u, rowY + 1.0f, l.navListW - 12.0f * l.u, row.height - 2.0f, 2.0f, 1.0f, NAV_HOVER_BORDER);
            }

            float indent = 22.0f * l.u;
            float iconSize = 16.0f * l.u;
            float iconX = l.navListX + indent;
            float iconY = rowY + (row.height - iconSize) / 2.0f;
            int image = nvg.getImage(CATEGORY_ICONS[row.categoryIndex]);
            nvg.drawImage(image, iconX, iconY, iconSize, iconSize, 0.85f);

            int textColor = isSelected ? ACCENT_BLUE : TEXT_DARK;
            nvg.drawText(row.label, iconX + iconSize + (6.0f * l.u), rowY + row.height / 2.0f, NanoVGManager.FONT_INTER, 12.0f * l.u, textColor, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        }
    }

    private void drawEditHudsButton(NanoVGManager nvg, Layout l, int mouseX, int mouseY, float deltaSeconds) {
        boolean hovered = withinRect(mouseX, mouseY, l.editHudsX, l.editHudsY, l.editHudsW, l.editHudsH);
        float target = hovered ? 1.0f : 0.0f;
        editHudsHoverProgress += (target - editHudsHoverProgress) * Math.min(1.0f, deltaSeconds * 18.0f);

        int top = interpolateColor(BTN_TOP, BTN_HOVER_TOP, editHudsHoverProgress);
        int bottom = interpolateColor(BTN_BOTTOM, BTN_HOVER_BOTTOM, editHudsHoverProgress);
        int border = interpolateColor(BTN_BORDER, BTN_HOVER_BORDER, editHudsHoverProgress);

        nvg.drawAeroButton(l.editHudsX, l.editHudsY, l.editHudsW, l.editHudsH, 3.0f * l.u, top, bottom, border, 0.5f);
        nvg.drawText("Edit HUDs...", l.editHudsX + l.editHudsW / 2.0f, l.editHudsY + l.editHudsH / 2.0f, NanoVGManager.FONT_INTER, 12.0f * l.u, TEXT_DARK, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
    }

    private void drawContent(NanoVGManager nvg, Layout l, int mouseX, int mouseY, float deltaSeconds) {
        nvg.drawText(CATEGORY_NAMES[selectedCategory], l.contentX + (17.0f * l.u), l.contentY + (22.0f * l.u), NanoVGManager.FONT_INTER_BOLD, 19.0f * l.u, TEXT_DARK, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        nvg.drawText(CATEGORY_DESCRIPTIONS[selectedCategory], l.contentX + (17.0f * l.u), l.contentY + (41.0f * l.u), NanoVGManager.FONT_INTER, 12.0f * l.u, TEXT_MUTED, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        nvg.drawLine(l.contentX, l.contentY + l.contentHeaderH, l.contentX + l.contentW, l.contentY + l.contentHeaderH, 1.0f, DIVIDER_COLOR);

        float listY = l.contentY + l.contentHeaderH;
        float listH = l.contentH - l.contentHeaderH;

        List<Module> visibleModules = searchQuery.isEmpty()
                ? ModuleManager.getInstance().getModulesByCategory(Category.values()[selectedCategory])
                : ModuleManager.getInstance().getModulesBySearch(searchQuery);

        float totalContentHeight = 0.0f;
        for (Module mod : visibleModules) {
            float targetExpand = mod.isExpanded() ? 1.0f : 0.0f;
            float curExpand = mod.getExpandProgress();
            curExpand += (targetExpand - curExpand) * Math.min(1.0f, deltaSeconds * 16.0f);
            mod.setExpandProgress(curExpand);

            float targetToggle = mod.isEnabled() ? 1.0f : 0.0f;
            float curToggle = mod.getToggleProgress();
            curToggle += (targetToggle - curToggle) * Math.min(1.0f, deltaSeconds * 16.0f);
            mod.setToggleProgress(curToggle);

            for (Setting<?> s : mod.getSettings()) {
                if (s instanceof BooleanSetting) {
                    BooleanSetting bs = (BooleanSetting) s;
                    float t = bs.isEnabled() ? 1.0f : 0.0f;
                    float c = bs.getToggleProgress();
                    c += (t - c) * Math.min(1.0f, deltaSeconds * 16.0f);
                    bs.setToggleProgress(c);
                } else if (s instanceof ColorSetting) {
                    ColorSetting cs = (ColorSetting) s;
                    float t = cs.isExpanded() ? 1.0f : 0.0f;
                    float c = cs.getExpandProgress();
                    c += (t - c) * Math.min(1.0f, deltaSeconds * 16.0f);
                    cs.setExpandProgress(c);
                    cs.animate(deltaSeconds);
                }
            }

            float settingsHeight = 0.0f;
            for (Setting<?> s : mod.getSettings()) {
                settingsHeight += getSettingTotalHeight(s, l);
            }
            totalContentHeight += l.rowHeight + (settingsHeight * curExpand);
        }

        float maxScroll = Math.max(0.0f, totalContentHeight - listH);
        targetScrollOffset = Math.max(0.0f, Math.min(maxScroll, targetScrollOffset));

        scrollOffset += scrollVelocity * deltaSeconds;
        scrollVelocity *= (float) Math.pow(0.04, deltaSeconds);
        if (Math.abs(scrollVelocity) < 1.0f) {
            scrollVelocity = 0.0f;
        }
        scrollOffset += (targetScrollOffset - scrollOffset) * Math.min(1.0f, deltaSeconds * 18.0f);
        scrollOffset = Math.max(0.0f, Math.min(maxScroll, scrollOffset));

        nvg.scissor(l.contentX, listY, l.contentW, listH);

        float rowY = listY - scrollOffset;
        for (Module mod : visibleModules) {
            float curExpand = mod.getExpandProgress();
            float settingsHeight = 0.0f;
            for (Setting<?> s : mod.getSettings()) {
                settingsHeight += getSettingTotalHeight(s, l);
            }
            float totalRowHeight = l.rowHeight + (settingsHeight * curExpand);

            boolean hasSettings = !mod.getSettings().isEmpty();
            boolean rowHovered = withinRect(mouseX, mouseY, l.contentX, rowY, l.contentW, l.rowHeight) &&
                    mouseY >= listY && mouseY <= listY + listH;

            float targetHover = rowHovered ? 1.0f : 0.0f;
            float curHover = mod.getHoverProgress();
            curHover += (targetHover - curHover) * Math.min(1.0f, deltaSeconds * 18.0f);
            mod.setHoverProgress(curHover);

            if (mod.getHoverProgress() > 0.01f) {
                int hoverTop = NanoVGManager.withAlpha(ROW_HOVER_TOP, mod.getHoverProgress());
                int hoverBottom = NanoVGManager.withAlpha(ROW_HOVER_BOTTOM, mod.getHoverProgress());
                nvg.drawVerticalGradientRounded(l.contentX + 1.0f, rowY, l.contentW - 2.0f, l.rowHeight, 2.0f, hoverTop, hoverBottom);
                if (mod.getHoverProgress() > 0.4f) {
                    nvg.drawRoundedRectOutline(l.contentX + 1.0f, rowY + 0.5f, l.contentW - 2.0f, l.rowHeight - 1.0f, 2.0f, 1.0f, NanoVGManager.withAlpha(ROW_HOVER_BORDER, mod.getHoverProgress()));
                }
            } else {
                nvg.drawLine(l.contentX + l.rowPadX, rowY + l.rowHeight, l.contentX + l.contentW - l.rowPadX, rowY + l.rowHeight, 1.0f, 0xFFE9E9E9);
            }

            float iconX = l.contentX + l.rowPadX;
            float iconY = rowY + (l.rowHeight - l.iconSize) / 2.0f;
            int image = nvg.getImage(CATEGORY_ICONS[mod.getCategory().ordinal()]);
            nvg.drawImage(image, iconX, iconY, l.iconSize, l.iconSize, 0.9f);

            float textX = iconX + l.iconSize + (9.0f * l.u);
            nvg.drawText(mod.getName(), textX, rowY + (18.5f * l.u), NanoVGManager.FONT_INTER_BOLD, 13.5f * l.u, TEXT_DARK, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
            nvg.drawText(mod.getDescription(), textX, rowY + (33.0f * l.u), NanoVGManager.FONT_INTER, 11.5f * l.u, TEXT_MUTED, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

            float checkboxY = rowY + (l.rowHeight - l.checkboxSize) / 2.0f;
            float checkboxX;
            if (hasSettings) {
                float arrowX = l.contentX + l.contentW - l.rowPadX - l.arrowSize;
                float arrowY = rowY + l.rowHeight / 2.0f;
                nvg.drawChevron(arrowX + l.arrowSize / 2.0f, arrowY, l.arrowSize, mod.getExpandProgress(), TEXT_MUTED, 1.1f);
                checkboxX = arrowX - (10.0f * l.u) - l.checkboxSize;
            } else {
                checkboxX = l.contentX + l.contentW - l.rowPadX - l.checkboxSize;
            }
            boolean checkboxHovered = withinRect(mouseX, mouseY, checkboxX - 2.0f, checkboxY - 2.0f, l.checkboxSize + 4.0f, l.checkboxSize + 4.0f);
            nvg.drawWin7Checkbox(checkboxX, checkboxY, l.checkboxSize, mod.getToggleProgress(), checkboxHovered);

            if (curExpand > 0.002f) {
                float settingsY = rowY + l.rowHeight;
                float visibleSettingsHeight = settingsHeight * curExpand;
                nvg.intersectScissor(l.contentX, settingsY, l.contentW, visibleSettingsHeight);
                nvg.drawLine(l.contentX + 12.0f * l.u, settingsY, l.contentX + l.contentW - 12.0f * l.u, settingsY, 1.0f, DIVIDER_COLOR);

                float settingRowY = settingsY;
                for (int sIdx = 0; sIdx < mod.getSettings().size(); sIdx++) {
                    Setting<?> s = mod.getSettings().get(sIdx);
                    float thisH = getSettingTotalHeight(s, l);
                    drawSettingRow(nvg, l, s, settingRowY, mouseX, mouseY, settingsY, visibleSettingsHeight, deltaSeconds);
                    if (sIdx < mod.getSettings().size() - 1) {
                        nvg.drawLine(l.contentX + 16.0f * l.u, settingRowY + thisH, l.contentX + l.contentW - 16.0f * l.u, settingRowY + thisH, 0.5f, DIVIDER_COLOR);
                    }
                    settingRowY += thisH;
                }
                nvg.scissor(l.contentX, listY, l.contentW, listH);
            }

            rowY += totalRowHeight;
        }

        nvg.scissor(l.contentX, listY, l.contentW, listH);
    }

    private void drawSettingRow(NanoVGManager nvg, Layout l, Setting<?> s, float rowY, int mouseX, int mouseY, float settingsY, float visibleSettingsHeight, float deltaSeconds) {
        float indentX = l.contentX + (24.0f * l.u);
        float rowCenterY = rowY + l.settingRowHeight / 2.0f;
        float fontSize = l.settingRowHeight * 0.46f;
        nvg.drawText(s.getName(), indentX, rowCenterY, NanoVGManager.FONT_INTER, fontSize, TEXT_DARK, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);

        float rightEdge = l.contentX + l.contentW - (16.0f * l.u);

        if (s instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) s;
            float boxSize = l.checkboxSize * 0.92f;
            float boxX = rightEdge - boxSize;
            float boxY = rowY + (l.settingRowHeight - boxSize) / 2.0f;
            boolean hovered = withinRect(mouseX, mouseY, boxX - 2.0f, boxY - 2.0f, boxSize + 4.0f, boxSize + 4.0f);
            nvg.drawWin7Checkbox(boxX, boxY, boxSize, bs.getToggleProgress(), hovered);
        } else if (s instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) s;
            float sliderWidth = (l.contentW - l.rowPadX * 2.0f) * 0.30f;
            float sliderHeight = 4.0f * l.u;
            float sliderX = rightEdge - sliderWidth;
            float sliderY = rowY + (l.settingRowHeight - sliderHeight) / 2.0f;
            nvg.drawAeroSlider(sliderX, sliderY, sliderWidth, sliderHeight, ns.getNormalized(), 0xFFC7CFD8, ACCENT_BLUE, 0xFF7A8FA3);

            String valStr = String.format("%.1f", ns.getValue());
            float valWidth = nvg.getTextWidth(valStr, NanoVGManager.FONT_INTER, fontSize);
            nvg.drawText(valStr, sliderX - valWidth - (6.0f * l.u), rowCenterY, NanoVGManager.FONT_INTER, fontSize, TEXT_MUTED, NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_MIDDLE);
        } else if (s instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) s;
            String modeText = ms.getValue();
            float modeTextWidth = nvg.getTextWidth(modeText, NanoVGManager.FONT_INTER, fontSize);
            float modeButtonWidth = Math.max((l.contentW - l.rowPadX * 2.0f) * 0.24f, modeTextWidth + (14.0f * l.u));
            float modeButtonHeight = l.settingRowHeight * 0.72f;
            float modeButtonX = rightEdge - modeButtonWidth;
            float modeButtonY = rowY + (l.settingRowHeight - modeButtonHeight) / 2.0f;

            boolean hovered = withinRect(mouseX, mouseY, modeButtonX, modeButtonY, modeButtonWidth, modeButtonHeight) &&
                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;
            float target = hovered ? 1.0f : 0.0f;
            float cur = ms.getHoverProgress();
            cur += (target - cur) * Math.min(1.0f, deltaSeconds * 18.0f);
            ms.setHoverProgress(cur);

            int top = interpolateColor(BTN_TOP, BTN_HOVER_TOP, ms.getHoverProgress());
            int bottom = interpolateColor(BTN_BOTTOM, BTN_HOVER_BOTTOM, ms.getHoverProgress());
            int border = interpolateColor(BTN_BORDER, BTN_HOVER_BORDER, ms.getHoverProgress());
            nvg.drawAeroButton(modeButtonX, modeButtonY, modeButtonWidth, modeButtonHeight, 3.0f * l.u, top, bottom, border, 0.5f);
            nvg.drawText(modeText, modeButtonX + modeButtonWidth / 2.0f, rowCenterY, NanoVGManager.FONT_INTER, fontSize, TEXT_DARK, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
        } else if (s instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) s;
            String keyText = ks.isListening() ? "..." : "[" + ks.getKeyName() + "]";
            float keyTextWidth = nvg.getTextWidth(keyText, NanoVGManager.FONT_INTER, fontSize);
            float keyButtonWidth = Math.max((l.contentW - l.rowPadX * 2.0f) * 0.18f, keyTextWidth + (12.0f * l.u));
            float keyButtonHeight = l.settingRowHeight * 0.72f;
            float keyButtonX = rightEdge - keyButtonWidth;
            float keyButtonY = rowY + (l.settingRowHeight - keyButtonHeight) / 2.0f;

            boolean hovered = withinRect(mouseX, mouseY, keyButtonX, keyButtonY, keyButtonWidth, keyButtonHeight) &&
                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;
            float target = hovered || ks.isListening() ? 1.0f : 0.0f;
            float cur = ks.getHoverProgress();
            cur += (target - cur) * Math.min(1.0f, deltaSeconds * 18.0f);
            ks.setHoverProgress(cur);

            int top = interpolateColor(BTN_TOP, BTN_HOVER_TOP, ks.getHoverProgress());
            int bottom = interpolateColor(BTN_BOTTOM, BTN_HOVER_BOTTOM, ks.getHoverProgress());
            int border = interpolateColor(BTN_BORDER, BTN_HOVER_BORDER, ks.getHoverProgress());
            nvg.drawAeroButton(keyButtonX, keyButtonY, keyButtonWidth, keyButtonHeight, 3.0f * l.u, top, bottom, border, 0.5f);
            int textColor = ks.isListening() ? ACCENT_BLUE : TEXT_DARK;
            nvg.drawText(keyText, keyButtonX + keyButtonWidth / 2.0f, rowCenterY, NanoVGManager.FONT_INTER, fontSize, textColor, NanoVG.NVG_ALIGN_CENTER | NanoVG.NVG_ALIGN_MIDDLE);
        } else if (s instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) s;
            float previewWidth = l.settingRowHeight * 1.1f;
            float previewHeight = l.settingRowHeight * 0.6f;
            float previewX = rightEdge - previewWidth;
            float previewY = rowY + (l.settingRowHeight - previewHeight) / 2.0f;

            boolean hovered = withinRect(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight) &&
                    mouseY >= settingsY && mouseY <= settingsY + visibleSettingsHeight;
            float target = hovered || cs.isExpanded() ? 1.0f : 0.0f;
            float cur = cs.getHoverProgress();
            cur += (target - cur) * Math.min(1.0f, deltaSeconds * 18.0f);
            cs.setHoverProgress(cur);

            int borderCol = interpolateColor(BTN_BORDER, ACCENT_BLUE, cs.getHoverProgress());
            nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 2.0f, 0xFFE7E7E7);
            nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 2.0f, cs.getColor());
            nvg.drawRoundedRectOutline(previewX, previewY, previewWidth, previewHeight, 2.0f, 1.0f, borderCol);

            if (cs.getExpandProgress() > 0.01f) {
                float pickerX = l.contentX + (24.0f * l.u);
                float pickerY = rowY + l.settingRowHeight;
                float pickerWidth = (l.contentW - l.rowPadX * 2.0f) - 40.0f;
                float svHeight = pickerWidth * 0.40f;
                float barHeight = 7.0f * l.u;
                float gap = 5.0f * l.u;
                float fullPickerH = svHeight + gap + barHeight + gap + barHeight + gap;

                nvg.intersectScissor(pickerX - 8.0f, pickerY, pickerWidth + 16.0f, fullPickerH * cs.getExpandProgress());

                float curSVY = pickerY;
                float curHueY = curSVY + svHeight + gap;
                float curAlphaY = curHueY + barHeight + gap;

                nvg.drawSVBox(pickerX, curSVY, pickerWidth, svHeight, 2.0f, cs.getHue());
                float svKnobX = pickerX + (cs.getSaturation() * pickerWidth);
                float svKnobY = curSVY + ((1.0f - cs.getBrightness()) * svHeight);
                nvg.drawPickerKnob(svKnobX, svKnobY, 4.0f, cs.getColor());

                nvg.drawHueBar(pickerX, curHueY, pickerWidth, barHeight, 2.0f);
                float hueKnobX = pickerX + (cs.getHue() * pickerWidth);
                nvg.drawPickerKnob(hueKnobX, curHueY + barHeight / 2.0f, 4.0f, java.awt.Color.HSBtoRGB(cs.getHue(), 1.0f, 1.0f));

                nvg.drawAlphaBar(pickerX, curAlphaY, pickerWidth, barHeight, 2.0f, cs.getColor() & 0x00FFFFFF);
                float alphaKnobX = pickerX + (cs.getAlpha() * pickerWidth);
                nvg.drawPickerKnob(alphaKnobX, curAlphaY + barHeight / 2.0f, 4.0f, cs.getColor());
            }
        }
    }

    private static boolean withinRect(float px, float py, float x, float y, float w, float h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
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
        Layout l = computeLayout(sr.getScaledWidth(), sr.getScaledHeight());

        if (mouseButton == 0) {
            if (withinRect(mouseX, mouseY, l.closeX, l.ctrlY, l.closeW, l.ctrlH)) {
                mc.displayGuiScreen(null);
                if (mc.currentScreen == null) {
                    mc.setIngameFocus();
                }
                return;
            }

            if (withinRect(mouseX, mouseY, l.searchX, l.searchY, l.searchW, l.searchH)) {
                searchFocused = true;
                cursorPosition = searchQuery.length();
                selectionEnd = cursorPosition;
                lastTypeOrBlinkTime = System.currentTimeMillis();
                return;
            } else {
                searchFocused = false;
            }

            for (NavRow row : l.navRows) {
                if (row.header) {
                    continue;
                }
                float rowY = l.navListY + row.y;
                if (withinRect(mouseX, mouseY, l.navListX, rowY, l.navListW, row.height)) {
                    selectedCategory = row.categoryIndex;
                    targetScrollOffset = 0.0f;
                    scrollOffset = 0.0f;
                    scrollVelocity = 0.0f;
                    return;
                }
            }

            if (withinRect(mouseX, mouseY, l.editHudsX, l.editHudsY, l.editHudsW, l.editHudsH)) {
                mc.displayGuiScreen(new EditHudsScreen(this));
                return;
            }
        }

        float listY = l.contentY + l.contentHeaderH;
        float listH = l.contentH - l.contentHeaderH;

        if (!withinRect(mouseX, mouseY, l.contentX, listY, l.contentW, listH)) {
            return;
        }

        List<Module> visibleModules = searchQuery.isEmpty()
                ? ModuleManager.getInstance().getModulesByCategory(Category.values()[selectedCategory])
                : ModuleManager.getInstance().getModulesBySearch(searchQuery);

        float rowY = listY - scrollOffset;
        for (Module mod : visibleModules) {
            float settingsHeight = 0.0f;
            for (Setting<?> s : mod.getSettings()) {
                settingsHeight += getSettingTotalHeight(s, l);
            }
            float totalRowHeight = l.rowHeight + (settingsHeight * mod.getExpandProgress());
            boolean hasSettings = !mod.getSettings().isEmpty();

            if (mouseButton == 0) {
                if (withinRect(mouseX, mouseY, l.contentX, rowY, l.contentW, l.rowHeight)) {
                    if (hasSettings) {
                        float arrowX = l.contentX + l.contentW - l.rowPadX - l.arrowSize;
                        if (mouseX >= arrowX - (12.0f * l.u) && mouseX <= l.contentX + l.contentW) {
                            mod.toggleExpanded();
                        } else {
                            mod.toggle();
                        }
                    } else {
                        mod.toggle();
                    }
                    return;
                }

                if (mod.getExpandProgress() > 0.05f &&
                        mouseY >= rowY + l.rowHeight && mouseY <= rowY + totalRowHeight) {
                    handleSettingClick(mod, l, rowY + l.rowHeight, mouseX, mouseY);
                    return;
                }
            } else if (mouseButton == 1) {
                if (withinRect(mouseX, mouseY, l.contentX, rowY, l.contentW, l.rowHeight)) {
                    if (hasSettings) {
                        mod.toggleExpanded();
                    }
                    return;
                }

                if (mod.getExpandProgress() > 0.05f &&
                        mouseY >= rowY + l.rowHeight && mouseY <= rowY + totalRowHeight) {
                    handleSettingRightClick(mod, l, rowY + l.rowHeight, mouseX, mouseY);
                    return;
                }
            }

            rowY += totalRowHeight;
        }
    }

    private void handleSettingClick(Module mod, Layout l, float settingsY, int mouseX, int mouseY) {
        float rowY = settingsY;
        float rightEdge = l.contentX + l.contentW - (16.0f * l.u);
        for (Setting<?> s : mod.getSettings()) {
            float thisH = getSettingTotalHeight(s, l);
            if (mouseY >= rowY && mouseY <= rowY + thisH) {
                if (s instanceof BooleanSetting) {
                    ((BooleanSetting) s).toggle();
                } else if (s instanceof NumberSetting) {
                    NumberSetting ns = (NumberSetting) s;
                    float sliderWidth = (l.contentW - l.rowPadX * 2.0f) * 0.30f;
                    float sliderX = rightEdge - sliderWidth;
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
                    if (mouseY <= rowY + l.settingRowHeight) {
                        cs.toggleExpanded();
                    } else if (cs.getExpandProgress() > 0.05f) {
                        float pickerX = l.contentX + (24.0f * l.u);
                        float pickerY = rowY + l.settingRowHeight;
                        float pickerWidth = (l.contentW - l.rowPadX * 2.0f) - 40.0f;
                        float svHeight = pickerWidth * 0.40f;
                        float barHeight = 7.0f * l.u;
                        float gap = 5.0f * l.u;

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
            rowY += thisH;
        }
    }

    private void handleSettingRightClick(Module mod, Layout l, float settingsY, int mouseX, int mouseY) {
        float rowY = settingsY;
        for (Setting<?> s : mod.getSettings()) {
            float thisH = getSettingTotalHeight(s, l);
            if (mouseY >= rowY && mouseY <= rowY + thisH) {
                if (s instanceof ModeSetting) {
                    ((ModeSetting) s).cyclePrevious();
                } else if (s instanceof KeybindSetting) {
                    KeybindSetting ks = (KeybindSetting) s;
                    ks.setKeyCode(Keyboard.KEY_NONE);
                    ks.setListening(false);
                } else if (s instanceof ColorSetting) {
                    ((ColorSetting) s).toggleExpanded();
                }
                return;
            }
            rowY += thisH;
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
        if (clickedMouseButton != 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        Layout l = computeLayout(sr.getScaledWidth(), sr.getScaledHeight());
        float rightEdge = l.contentX + l.contentW - (16.0f * l.u);

        if (draggingSlider != null) {
            float sliderWidth = (l.contentW - l.rowPadX * 2.0f) * 0.30f;
            float sliderX = rightEdge - sliderWidth;
            float ratio = (mouseX - sliderX) / sliderWidth;
            draggingSlider.setNormalized(ratio);
        } else if (draggingColorSetting != null) {
            float pickerX = l.contentX + (24.0f * l.u);
            float pickerWidth = (l.contentW - l.rowPadX * 2.0f) - 40.0f;
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
