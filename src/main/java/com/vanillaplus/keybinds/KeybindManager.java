package com.vanillaplus.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.vanillaplus.core.ModuleManager;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class KeybindManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("vanillaplus", "main"));

    private static KeyMapping toggleAutoBreedKey;
    private static KeyMapping toggleAutoReplantKey;
    private static KeyMapping toggleFreecamKey;

    private static KeyMapping toggleAutoToolKey;
    private static KeyMapping toggleAutoFishKey;
    private static KeyMapping toggleAutoRefillKey;
    private static KeyMapping toggleFullbrightKey;
    private static KeyMapping toggleLightLevelKey;

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

        toggleAutoFishKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.toggle_auto_fish",
                InputConstants.KEY_K,
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
    }

    public static void handleKeyInputs(ModuleManager moduleManager) {
        if (toggleAutoBreedKey != null) {
            while (toggleAutoBreedKey.consumeClick()) {
                moduleManager.getModule("auto_breed").ifPresent(m -> m.toggle());
            }
        }

        if (toggleAutoReplantKey != null) {
            while (toggleAutoReplantKey.consumeClick()) {
                moduleManager.getModule("auto_replant").ifPresent(m -> m.toggle());
            }
        }

        if (toggleFreecamKey != null) {
            while (toggleFreecamKey.consumeClick()) {
                moduleManager.getModule("freecam").ifPresent(m -> m.toggle());
            }
        }

        if (toggleAutoToolKey != null) {
            while (toggleAutoToolKey.consumeClick()) {
                moduleManager.getModule("auto_tool").ifPresent(m -> m.toggle());
            }
        }

        if (toggleAutoFishKey != null) {
            while (toggleAutoFishKey.consumeClick()) {
                moduleManager.getModule("auto_fish").ifPresent(m -> m.toggle());
            }
        }

        if (toggleAutoRefillKey != null) {
            while (toggleAutoRefillKey.consumeClick()) {
                moduleManager.getModule("auto_refill").ifPresent(m -> m.toggle());
            }
        }

        if (toggleFullbrightKey != null) {
            while (toggleFullbrightKey.consumeClick()) {
                moduleManager.getModule("fullbright").ifPresent(m -> m.toggle());
            }
        }

        if (toggleLightLevelKey != null) {
            while (toggleLightLevelKey.consumeClick()) {
                moduleManager.getModule("light_level_overlay").ifPresent(m -> m.toggle());
            }
        }
    }

    public static KeyMapping getToggleAutoBreedKey() {
        return toggleAutoBreedKey;
    }

    public static KeyMapping getToggleAutoReplantKey() {
        return toggleAutoReplantKey;
    }

    public static KeyMapping getToggleFreecamKey() {
        return toggleFreecamKey;
    }

    public static KeyMapping getToggleAutoToolKey() {
        return toggleAutoToolKey;
    }

    public static KeyMapping getToggleAutoFishKey() {
        return toggleAutoFishKey;
    }

    public static KeyMapping getToggleAutoRefillKey() {
        return toggleAutoRefillKey;
    }

    public static KeyMapping getToggleFullbrightKey() {
        return toggleFullbrightKey;
    }

    public static KeyMapping getToggleLightLevelKey() {
        return toggleLightLevelKey;
    }
}
