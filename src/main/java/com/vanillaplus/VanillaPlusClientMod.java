package com.vanillaplus;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.gui.StatusHudOverlay;
import com.vanillaplus.keybinds.KeybindManager;
import com.vanillaplus.modules.automation.AutoBreedModule;
import com.vanillaplus.modules.automation.AutoReplantModule;
import com.vanillaplus.modules.movement.FreecamModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaPlusClientMod implements ClientModInitializer {
    public static final String MOD_ID = "vanillaplus";
    public static final Logger LOGGER = LoggerFactory.getLogger("VanillaPlus");

    private static VanillaPlusClientMod instance;
    private final ModuleManager moduleManager = new ModuleManager();

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Inicializando VanillaPlus para Minecraft 26.3...");

        ConfigManager.load();
        ModConfig config = ConfigManager.getConfig();

        AutoBreedModule autoBreed = new AutoBreedModule();
        AutoReplantModule autoReplant = new AutoReplantModule();
        FreecamModule freecam = new FreecamModule();

        autoBreed.setEnabled(config.autoBreedEnabled);
        autoReplant.setEnabled(config.autoReplantEnabled);
        freecam.setEnabled(config.freecamEnabled);

        moduleManager.registerModule(autoBreed);
        moduleManager.registerModule(autoReplant);
        moduleManager.registerModule(freecam);

        UseBlockCallback.EVENT.register(autoReplant::onUseBlock);

        KeybindManager.register();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "status_hud"),
                new StatusHudOverlay(moduleManager));

        moduleManager.initEvents();

        LOGGER.info("VanillaPlus inicializado com sucesso. {} módulos carregados.", moduleManager.getModules().size());
    }

    public static VanillaPlusClientMod getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
