package com.vanillaplus.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.vanillaplus.core.ModuleManager;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class KeybindManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("vanillaplus", "main"));

    private static @Nullable KeyMapping toggleAutoBreedKey;
    private static @Nullable KeyMapping toggleAutoReplantKey;
    private static @Nullable KeyMapping toggleFreecamKey;

    private static @Nullable KeyMapping toggleAutoToolKey;
    private static @Nullable KeyMapping toggleAutoRefillKey;
    private static @Nullable KeyMapping toggleFullbrightKey;
    private static @Nullable KeyMapping toggleLightLevelKey;
    private static @Nullable KeyMapping toggleBreedIndicatorKey;
    private static @Nullable KeyMapping toggleCropIndicatorKey;
    private static @Nullable KeyMapping toggleAutoMineKey;
    private static @Nullable KeyMapping resetAutoMineKey;

    public static void register() {
        toggleAutoBreedKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_breed",
                InputConstants.KEY_B,
                CATEGORY));

        toggleAutoReplantKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_replant",
                InputConstants.KEY_R,
                CATEGORY));

        toggleFreecamKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_freecam",
                InputConstants.KEY_U,
                CATEGORY));

        toggleAutoToolKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_tool",
                InputConstants.KEY_G,
                CATEGORY));

        toggleAutoRefillKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_refill",
                InputConstants.KEY_J,
                CATEGORY));

        toggleFullbrightKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_fullbright",
                InputConstants.KEY_H,
                CATEGORY));

        toggleLightLevelKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_light_level",
                InputConstants.KEY_F7,
                CATEGORY));

        toggleBreedIndicatorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_breed_indicator",
                InputConstants.KEY_V,
                CATEGORY));

        toggleCropIndicatorKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_crop_indicator",
                InputConstants.KEY_K,
                CATEGORY));

        toggleAutoMineKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_mine",
                InputConstants.KEY_M,
                CATEGORY));

        resetAutoMineKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.reset_auto_mine",
                InputConstants.KEY_N,
                CATEGORY));
    }

    public static void handleKeyInputs(ModuleManager moduleManager) {
        KeyMapping autoBreed = toggleAutoBreedKey;
        if (autoBreed != null) {
            while (autoBreed.consumeClick()) {
                moduleManager.getModule("auto_breed").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping autoReplant = toggleAutoReplantKey;
        if (autoReplant != null) {
            while (autoReplant.consumeClick()) {
                moduleManager.getModule("auto_replant").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping freecam = toggleFreecamKey;
        if (freecam != null) {
            while (freecam.consumeClick()) {
                moduleManager.getModule("freecam").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping autoTool = toggleAutoToolKey;
        if (autoTool != null) {
            while (autoTool.consumeClick()) {
                moduleManager.getModule("auto_tool").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping autoRefill = toggleAutoRefillKey;
        if (autoRefill != null) {
            while (autoRefill.consumeClick()) {
                moduleManager.getModule("auto_refill").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping fullbright = toggleFullbrightKey;
        if (fullbright != null) {
            while (fullbright.consumeClick()) {
                moduleManager.getModule("fullbright").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping lightLevel = toggleLightLevelKey;
        if (lightLevel != null) {
            while (lightLevel.consumeClick()) {
                moduleManager.getModule("light_level_overlay").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping breedIndicator = toggleBreedIndicatorKey;
        if (breedIndicator != null) {
            while (breedIndicator.consumeClick()) {
                moduleManager.getModule("breed_indicator").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping cropIndicator = toggleCropIndicatorKey;
        if (cropIndicator != null) {
            while (cropIndicator.consumeClick()) {
                moduleManager.getModule("crop_indicator").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping autoMine = toggleAutoMineKey;
        if (autoMine != null) {
            while (autoMine.consumeClick()) {
                moduleManager.getModule("auto_mine").ifPresent(m -> m.toggle());
            }
        }

        KeyMapping resetAutoMine = resetAutoMineKey;
        if (resetAutoMine != null) {
            while (resetAutoMine.consumeClick()) {
                moduleManager.getModule("auto_mine").ifPresent(m -> {
                    if (m instanceof com.vanillaplus.modules.automation.AutoMineModule mine) {
                        mine.resetSelection();
                    }
                });
            }
        }
    }

    public static @Nullable KeyMapping getToggleAutoBreedKey() {
        return toggleAutoBreedKey;
    }

    public static @Nullable KeyMapping getToggleAutoReplantKey() {
        return toggleAutoReplantKey;
    }

    public static @Nullable KeyMapping getToggleFreecamKey() {
        return toggleFreecamKey;
    }

    public static @Nullable KeyMapping getToggleAutoToolKey() {
        return toggleAutoToolKey;
    }

    public static @Nullable KeyMapping getToggleAutoRefillKey() {
        return toggleAutoRefillKey;
    }

    public static @Nullable KeyMapping getToggleFullbrightKey() {
        return toggleFullbrightKey;
    }

    public static @Nullable KeyMapping getToggleLightLevelKey() {
        return toggleLightLevelKey;
    }

    public static @Nullable KeyMapping getToggleBreedIndicatorKey() {
        return toggleBreedIndicatorKey;
    }

    public static @Nullable KeyMapping getToggleCropIndicatorKey() {
        return toggleCropIndicatorKey;
    }

    public static @Nullable KeyMapping getToggleAutoMineKey() {
        return toggleAutoMineKey;
    }

    public static @Nullable KeyMapping getResetAutoMineKey() {
        return resetAutoMineKey;
    }
}
