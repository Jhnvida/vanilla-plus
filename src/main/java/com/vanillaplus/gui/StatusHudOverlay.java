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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StatusHudOverlay implements HudElement {
    private final ModuleManager moduleManager;
    private final Map<String, Float> animationProgress = new ConcurrentHashMap<>();
    private long lastFrameTime = System.currentTimeMillis();

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

        long now = System.currentTimeMillis();
        float deltaSeconds = Math.clamp((now - lastFrameTime) / 1000.0f, 0.001f, 0.1f);
        lastFrameTime = now;

        float animSpeed = 4.0f;

        Collection<Module> allModules = moduleManager.getModules();
        for (Module m : allModules) {
            float current = animationProgress.getOrDefault(m.getId(), 0.0f);
            if (m.isEnabled()) {
                current = Math.min(1.0f, current + deltaSeconds * animSpeed);
            } else {
                current = Math.max(0.0f, current - deltaSeconds * animSpeed);
            }

            if (current <= 0.001f && !m.isEnabled()) {
                animationProgress.remove(m.getId());
            } else {
                animationProgress.put(m.getId(), current);
            }
        }

        if (animationProgress.isEmpty()) {
            return;
        }

        Font font = client.font;
        int screenWidth = extractor.guiWidth();
        int y = 6;

        for (Module module : allModules) {
            Float progress = animationProgress.get(module.getId());
            if (progress == null || progress <= 0.001f) {
                continue;
            }

            float eased = 1.0f - (float) Math.pow(1.0f - progress, 3);

            String name = module.getDisplayName();
            int textWidth = font.width(name);
            int baseTargetX = screenWidth - textWidth - 8;

            int slideOffset = (int) ((1.0f - eased) * 35.0f);
            int x = baseTargetX + slideOffset;

            int alpha = (int) (eased * 255.0f);
            int bgAlpha = (int) (eased * 0x90);

            int bgColor = (bgAlpha << 24);
            int borderColor = (alpha << 24) | 0x0044FF44;
            int textColor = (alpha << 24) | 0x0055FF55;

            extractor.fill(x - 4, y - 2, screenWidth - 4 + slideOffset, y + 10, bgColor);
            extractor.fill(screenWidth - 4 + slideOffset, y - 2, screenWidth - 2 + slideOffset, y + 10, borderColor);
            extractor.text(font, Component.literal(name), x, y, textColor, true);

            y += (int) (13 * eased);
        }
    }
}
