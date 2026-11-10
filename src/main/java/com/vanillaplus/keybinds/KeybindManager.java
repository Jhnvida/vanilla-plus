package com.vanillaplus.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.gui.dashboard.VanillaPlusDashboardScreen;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class KeybindManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("vanillaplus", "main"));

    private static @Nullable KeyMapping openDashboardKey;

    private static @Nullable KeyMapping toggleAutoBreedKey;
    private static @Nullable KeyMapping toggleAutoReplantKey;
    private static @Nullable KeyMapping toggleFreecamKey;
    private static @Nullable KeyMapping toggleAutoToolKey;
    private static @Nullable KeyMapping toggleAutoRefillKey;
    private static @Nullable KeyMapping toggleFullbrightKey;
    private static @Nullable KeyMapping toggleLightLevelKey;
    private static @Nullable KeyMapping toggleAutoMineKey;
    private static @Nullable KeyMapping resetAutoMineKey;

    private static final Map<String, KeyMapping> MODULE_KEYMAPS = new HashMap<>();

    private static boolean initialSyncDone = false;

    public static void register() {
        openDashboardKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaplus.open_dashboard",
                InputConstants.KEY_RCONTROL,
                CATEGORY));

        toggleAutoBreedKey = registerModuleMapping("auto_breed", "key.vanillaplus.toggle_auto_breed");
        toggleAutoReplantKey = registerModuleMapping("auto_replant", "key.vanillaplus.toggle_auto_replant");
        toggleFreecamKey = registerModuleMapping("freecam", "key.vanillaplus.toggle_freecam");
        toggleAutoToolKey = registerModuleMapping("auto_tool", "key.vanillaplus.toggle_auto_tool");
        toggleAutoRefillKey = registerModuleMapping("auto_refill", "key.vanillaplus.toggle_auto_refill");
        toggleFullbrightKey = registerModuleMapping("fullbright", "key.vanillaplus.toggle_fullbright");
        toggleLightLevelKey = registerModuleMapping("light_level_overlay", "key.vanillaplus.toggle_light_level");
        toggleAutoMineKey = registerModuleMapping("auto_mine", "key.vanillaplus.toggle_auto_mine");
        resetAutoMineKey = registerModuleMapping("auto_mine_reset", "key.vanillaplus.reset_auto_mine");

        applySavedKeybinds();
    }

    private static KeyMapping registerModuleMapping(String moduleId, String translationKey) {
        KeyMapping mapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                translationKey,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY));
        MODULE_KEYMAPS.put(moduleId, mapping);
        return mapping;
    }

    public static void applySavedKeybinds() {
        ModConfig config = ConfigManager.getConfig();
        Map<String, Integer> saved = config.moduleKeybinds;

        boolean optionsNeedSave = false;

        for (Map.Entry<String, KeyMapping> entry : MODULE_KEYMAPS.entrySet()) {
            String moduleId = entry.getKey();
            KeyMapping mapping = entry.getValue();
            Integer keyCode = (saved != null) ? saved.get(moduleId) : null;
            if (keyCode != null && keyCode != InputConstants.UNKNOWN.getValue() && keyCode > 0) {
                InputConstants.Key targetKey = InputConstants.Type.KEYBOARD.getOrCreate(keyCode);
                if (!mapping.matches(targetKey)) {
                    mapping.setKey(targetKey);
                    optionsNeedSave = true;
                }
            } else {
                if (!mapping.isUnbound()) {
                    mapping.setKey(InputConstants.UNKNOWN);
                    optionsNeedSave = true;
                }
            }
        }

        KeyMapping.resetMapping();

        if (optionsNeedSave) {
            saveMinecraftOptions();
        }
    }

    public static void setModuleKey(String moduleId, int keyCode) {
        KeyMapping mapping = MODULE_KEYMAPS.get(moduleId);
        if (mapping != null) {
            if (keyCode <= 0 || keyCode == InputConstants.KEY_ESCAPE || keyCode == InputConstants.KEY_BACKSPACE
                    || keyCode == InputConstants.KEY_DELETE) {
                removeModuleKey(moduleId);
                return;
            } else {
                mapping.setKey(InputConstants.Type.KEYBOARD.getOrCreate(keyCode));
                ConfigManager.getConfig().moduleKeybinds.put(moduleId, keyCode);
            }
            KeyMapping.resetMapping();
            ConfigManager.save();
            saveMinecraftOptions();
        }
    }

    public static void removeModuleKey(String moduleId) {
        KeyMapping mapping = MODULE_KEYMAPS.get(moduleId);
        if (mapping != null) {
            mapping.setKey(InputConstants.UNKNOWN);
        }
        ConfigManager.getConfig().moduleKeybinds.remove(moduleId);
        KeyMapping.resetMapping();
        ConfigManager.save();
        saveMinecraftOptions();
    }

    @SuppressWarnings("null")
    public static String getKeyDisplayName(String moduleId) {
        KeyMapping mapping = MODULE_KEYMAPS.get(moduleId);
        if (mapping == null || mapping.isUnbound()) {
            return "None";
        }
        String name = mapping.getTranslatedKeyMessage().getString();
        if (name.isBlank() || name.equalsIgnoreCase("unknown")) {
            return "None";
        }
        return name.toUpperCase(java.util.Locale.ROOT);
    }

    public static boolean hasCustomKey(String moduleId) {
        return ConfigManager.getConfig().moduleKeybinds.containsKey(moduleId);
    }

    private static void saveMinecraftOptions() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.options != null) {
                client.options.save();
            }
        } catch (Throwable ignored) {
        }
    }

    public static @Nullable KeyMapping getKeyMappingForModule(String moduleId) {
        return MODULE_KEYMAPS.get(moduleId);
    }

    public static void handleKeyInputs(ModuleManager moduleManager) {
        if (!initialSyncDone) {
            applySavedKeybinds();
            initialSyncDone = true;
        }

        KeyMapping openDashboard = openDashboardKey;
        if (openDashboard != null && openDashboard.consumeClick()) {
            Minecraft client = Minecraft.getInstance();
            if (client.gui != null && client.gui.screen() == null) {
                client.gui.setScreen(new VanillaPlusDashboardScreen(null, moduleManager));
                return;
            }
        }

        for (Map.Entry<String, KeyMapping> entry : MODULE_KEYMAPS.entrySet()) {
            String id = entry.getKey();
            KeyMapping key = entry.getValue();

            if (!key.isUnbound()) {
                while (key.consumeClick()) {
                    if ("auto_mine_reset".equals(id)) {
                        moduleManager.getModule("auto_mine").ifPresent(m -> {
                            if (m instanceof com.vanillaplus.modules.automation.AutoMineModule mine) {
                                mine.resetSelection();
                            }
                        });
                    } else {
                        moduleManager.getModule(id).ifPresent(Module::toggle);
                    }
                }
            }
        }
    }

    public static @Nullable KeyMapping getOpenDashboardKey() {
        return openDashboardKey;
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

    public static @Nullable KeyMapping getToggleAutoMineKey() {
        return toggleAutoMineKey;
    }

    public static @Nullable KeyMapping getResetAutoMineKey() {
        return resetAutoMineKey;
    }
}
