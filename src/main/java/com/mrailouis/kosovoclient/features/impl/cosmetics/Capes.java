package com.mrailouis.kosovoclient.features.impl.cosmetics;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.ModeSetting;
import com.mrailouis.kosovoclient.features.Module;
import com.mrailouis.kosovoclient.features.NumberSetting;
import com.mrailouis.kosovoclient.features.impl.cosmetics.capes.CapeManager;
import com.mrailouis.kosovoclient.features.impl.cosmetics.capes.CapeRenderer;
import com.mrailouis.kosovoclient.features.impl.cosmetics.capes.CapeTexture;
import lombok.Getter;
import net.minecraft.client.entity.AbstractClientPlayer;

@Getter
public class Capes extends Module {
    private static final String[] STYLES = {"Flowing", "Wave", "Flat"};
    private static final String[] DEFAULT_CAPES = {"Optifine", "Kosovo", "Lunar", "Galaxy", "Fire"};
    private static final Capes INSTANCE = new Capes();

    private final ModeSetting style = new ModeSetting("Style", "Cape visual style / physics.", "Flowing", STYLES);
    private final ModeSetting cape = new ModeSetting("Cape", "Active cape design or uploaded file.", "Optifine", DEFAULT_CAPES);
    private final NumberSetting waveSpeed = new NumberSetting("Wave Speed", "Flowing wave animation speed.", 1.0, 0.2, 3.0, 0.1);
    private final NumberSetting waveAmount = new NumberSetting("Wave Flow", "Flow / wave movement amplitude.", 1.0, 0.2, 2.5, 0.1);
    private final BooleanSetting idleFlow = new BooleanSetting("Idle Flow", "Animate flowing wave when standing still.", true);
    private final BooleanSetting physics = new BooleanSetting("Cape Physics", "Realistic inertia when moving and turning.", true);
    private final BooleanSetting uploadCape = new BooleanSetting("Upload Cape", "Select a .png or .gif cape file from your computer.", false, () -> CapeManager.getInstance().promptUploadCape());
    private final BooleanSetting openFolder = new BooleanSetting("Open Capes Folder", "Open the folder where cape files are stored.", false, () -> CapeManager.getInstance().openCapesFolder());

    public static Capes getInstance() {
        return INSTANCE;
    }

    private Capes() {
        super("Capes", "Custom and animated capes with flowing wave physics.", Category.COSMETICS, true);
        registerSetting(this.style);
        registerSetting(this.cape);
        registerSetting(this.waveSpeed);
        registerSetting(this.waveAmount);
        registerSetting(this.idleFlow);
        registerSetting(this.physics);
        registerSetting(this.uploadCape);
        registerSetting(this.openFolder);
    }

    public CapeTexture getActiveCapeTexture() {
        String selected = this.cape.getValue();
        CapeTexture texture = CapeManager.getInstance().getCapeTexture(selected);
        if (texture == null) {
            texture = CapeManager.getInstance().getCapeTexture("optifine");
        }
        return texture;
    }

    public void renderCape(AbstractClientPlayer player, float partialTicks) {
        if (!isEnabled() || player == null) {
            return;
        }
        CapeTexture texture = getActiveCapeTexture();
        if (texture != null) {
            CapeRenderer.render(
                    player,
                    texture,
                    this.style.getValue(),
                    this.waveSpeed.getValue().floatValue(),
                    this.waveAmount.getValue().floatValue(),
                    this.idleFlow.isEnabled(),
                    this.physics.isEnabled(),
                    partialTicks
            );
        }
    }
}
