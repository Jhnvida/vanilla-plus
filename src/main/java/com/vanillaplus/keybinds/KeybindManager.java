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
    }
}
