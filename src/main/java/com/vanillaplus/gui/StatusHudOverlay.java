package com.vanillaplus.gui;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class StatusHudOverlay implements HudElement {
    private final ModuleManager moduleManager;

    public StatusHudOverlay(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        if (!config.showStatusHud) {
            return;
        }

        List<Module> activeModules = new ArrayList<>();
        for (Module m : moduleManager.getModules()) {
            if (m.isEnabled()) {
                activeModules.add(m);
            }
        }

        if (activeModules.isEmpty()) {
            return;
        }

        Font font = client.font;
        int screenWidth = extractor.guiWidth();
        int y = 6;

        for (Module module : activeModules) {
            String name = module.getName();
            int textWidth = font.width(name);
            int x = screenWidth - textWidth - 8;

            extractor.fill(x - 4, y - 2, screenWidth - 4, y + 10, 0x90000000);
            extractor.fill(screenWidth - 4, y - 2, screenWidth - 2, y + 10, 0xFF44FF44);

            extractor.text(font, Component.literal(name), x, y, 0xFF55FF55, true);

            y += 13;
        }
    }
}
