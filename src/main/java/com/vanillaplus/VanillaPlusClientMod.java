package com.vanillaplus;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.gui.StatusHudOverlay;
import com.vanillaplus.keybinds.KeybindManager;
import com.vanillaplus.modules.automation.AutoBreedModule;
import com.vanillaplus.modules.automation.AutoMineModule;
import com.vanillaplus.modules.automation.AutoRefillModule;
import com.vanillaplus.modules.automation.AutoReplantModule;
import com.vanillaplus.modules.automation.AutoToolModule;
import com.vanillaplus.modules.movement.FreecamModule;
import com.vanillaplus.modules.render.AutoBreedIndicatorModule;
import com.vanillaplus.modules.render.CropIndicatorModule;
import com.vanillaplus.modules.render.FullbrightModule;
import com.vanillaplus.modules.render.LightLevelOverlayModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@NullMarked
public class VanillaPlusClientMod implements ClientModInitializer {
    public static final String MOD_ID = "vanillaplus";
    public static final Logger LOGGER = Objects.requireNonNull(LoggerFactory.getLogger("VanillaPlus"));

    private static @Nullable VanillaPlusClientMod instance;
    private final ModuleManager moduleManager = new ModuleManager();

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Inicializando VanillaPlus 2.1 para Minecraft 26.3...");

        ConfigManager.load();
        ModConfig config = ConfigManager.getConfig();

        AutoBreedModule autoBreed = new AutoBreedModule();
        AutoReplantModule autoReplant = new AutoReplantModule();
        FreecamModule freecam = new FreecamModule();

        AutoToolModule autoTool = new AutoToolModule();
        AutoRefillModule autoRefill = new AutoRefillModule();
        FullbrightModule fullbright = new FullbrightModule();
        LightLevelOverlayModule lightLevelOverlay = new LightLevelOverlayModule();
        AutoMineModule autoMine = new AutoMineModule();

        AutoBreedIndicatorModule breedIndicator = new AutoBreedIndicatorModule();
        CropIndicatorModule cropIndicator = new CropIndicatorModule();

        autoBreed.setEnabled(config.autoBreedEnabled);
        autoReplant.setEnabled(config.autoReplantEnabled);
        freecam.setEnabled(config.freecamEnabled);
        autoTool.setEnabled(config.autoToolEnabled);
        autoRefill.setEnabled(config.autoRefillEnabled);
        fullbright.setEnabled(config.fullbrightEnabled);
        lightLevelOverlay.setEnabled(config.lightLevelOverlayEnabled);
        autoMine.setEnabled(config.autoMineEnabled);

        moduleManager.registerModule(autoBreed);
        moduleManager.registerModule(autoReplant);
        moduleManager.registerModule(freecam);
        moduleManager.registerModule(autoTool);
        moduleManager.registerModule(autoRefill);
        moduleManager.registerModule(fullbright);
        moduleManager.registerModule(lightLevelOverlay);
        moduleManager.registerModule(autoMine);

        UseBlockCallback.EVENT.register(autoReplant::onUseBlock);
        AttackBlockCallback.EVENT.register(autoMine::onAttackBlock);
        UseBlockCallback.EVENT.register(autoMine::onUseBlock);
        UseEntityCallback.EVENT.register(AutoBreedModule::onUseEntity);
        ClientEntityEvents.ENTITY_LOAD.register(AutoBreedModule::onEntityLoad);

        LevelRenderEvents.BEFORE_GIZMOS.register(lightLevelOverlay::renderOverlay);
        LevelRenderEvents.BEFORE_GIZMOS.register(breedIndicator::renderOverlay);
        LevelRenderEvents.BEFORE_GIZMOS.register(autoMine::renderOverlay);
        LevelRenderEvents.BEFORE_GIZMOS.register(cropIndicator::renderOverlay);

        KeybindManager.register();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "status_hud"),
                new StatusHudOverlay(moduleManager));

        moduleManager.initEvents();

        LOGGER.info("VanillaPlus inicializado com sucesso. {} módulos carregados.", moduleManager.getModules().size());
    }

    public static @Nullable VanillaPlusClientMod getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
