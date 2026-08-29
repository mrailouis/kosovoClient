package com.mrailouis.kosovoclient.features.impl.animations;

import com.mrailouis.kosovoclient.features.BooleanSetting;
import com.mrailouis.kosovoclient.features.Category;
import com.mrailouis.kosovoclient.features.Module;
import lombok.Getter;

@Getter
public class OldAnimations extends Module {
    private static final OldAnimations INSTANCE = new OldAnimations();

    private final BooleanSetting bowPosition = new BooleanSetting("Bow Position", "Reverts bow held position and pulling animation to 1.7.", true);
    private final BooleanSetting fishingRod = new BooleanSetting("Fishing Rod", "Reverts held fishing rod position and line origin.", true);
    private final BooleanSetting itemSwitch = new BooleanSetting("Item Switch", "Prevents re-equip drop animation when using items.", true);
    private final BooleanSetting blockHit = new BooleanSetting("Block Hit", "Restores smooth 1.7 sword block-hitting animation.", true);
    private final BooleanSetting swordPosition = new BooleanSetting("Sword Position", "Restores 1.7 first-person sword blocking pose.", true);
    private final BooleanSetting thirdPersonBlock = new BooleanSetting("3rd Person Block", "Restores 1.7 third-person sword blocking pose.", true);
    private final BooleanSetting punchDuringUsage = new BooleanSetting("Punch During Usage", "Allows punching blocks while eating or blocking.", true);
    private final BooleanSetting eatingDrinking = new BooleanSetting("Eating & Drinking", "Restores 1.7 item bobbing while consuming.", true);
    private final BooleanSetting smoothSneaking = new BooleanSetting("Smooth Sneaking", "Smoothly interpolates camera eye height on sneak.", true);
    private final BooleanSetting longerUnsneak = new BooleanSetting("Longer Unsneak", "Slows camera rise when un-sneaking to match 1.7.", true);
    private final BooleanSetting redArmour = new BooleanSetting("Red Armour", "Makes entity armor flash red when taking damage.", true);
    private final BooleanSetting healthBarFlashRemoval = new BooleanSetting("Health Flash Removal", "Removes white heart flashing when taking damage.", true);

    public static OldAnimations getInstance() {
        return INSTANCE;
    }

    private OldAnimations() {
        super("Old Animations", "Old animations from 1.7", Category.ANIMATIONS, true);
        registerSetting(bowPosition);
        registerSetting(fishingRod);
        registerSetting(itemSwitch);
        registerSetting(blockHit);
        registerSetting(swordPosition);
        registerSetting(thirdPersonBlock);
        registerSetting(punchDuringUsage);
        registerSetting(eatingDrinking);
        registerSetting(smoothSneaking);
        registerSetting(longerUnsneak);
        registerSetting(redArmour);
        registerSetting(healthBarFlashRemoval);
    }
}
