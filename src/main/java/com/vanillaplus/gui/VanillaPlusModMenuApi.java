package com.vanillaplus.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.vanillaplus.VanillaPlusClientMod;
import com.vanillaplus.core.ModuleManager;
import com.vanillaplus.gui.dashboard.VanillaPlusDashboardScreen;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaPlusModMenuApi implements ModMenuApi {
    private static final Logger LOGGER = LoggerFactory.getLogger("VanillaPlus/ModMenu");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    public Screen createConfigScreen(Screen parent) {
        try {
            VanillaPlusClientMod mod = VanillaPlusClientMod.getInstance();
            ModuleManager moduleManager = mod != null ? mod.getModuleManager() : new ModuleManager();
            return new VanillaPlusDashboardScreen(parent, moduleManager);
        } catch (Throwable t) {
            LOGGER.error("Falha ao abrir a central de configurações do VanillaPlus:", t);
            return parent;
        }
    }
}
