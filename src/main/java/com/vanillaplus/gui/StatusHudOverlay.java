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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
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

        float animSpeed = 4.2f;

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

        List<Module> sortedModules = new ArrayList<>(allModules);
        sortedModules.sort((a, b) -> {
            String nameA = a.getDisplayName();
            String nameB = b.getDisplayName();
            int lenCompare = Integer.compare(nameB.length(), nameA.length());
            if (lenCompare != 0) {
                return lenCompare;
            }
            int widthCompare = Integer.compare(font.width(nameB), font.width(nameA));
            if (widthCompare != 0) {
                return widthCompare;
            }
            return nameA.compareTo(nameB);
        });

        int y = 6;
        int rightMargin = 6;

        for (Module module : sortedModules) {
            @Nullable
            Float progress = animationProgress.get(module.getId());
            if (progress == null || progress <= 0.001f) {
                continue;
            }

            float eased = 1.0f - (float) Math.pow(1.0f - progress, 3);

            String fullName = module.getDisplayName();
            int textWidth = font.width(fullName);

            int slideOffset = (int) ((1.0f - eased) * 20.0f);

            int baseTargetX = screenWidth - textWidth - rightMargin - 4;
            int x = baseTargetX + slideOffset;
            int boxRight = screenWidth - rightMargin + slideOffset;
            int boxLeft = x - 5;

            int alpha = (int) (eased * 255.0f);
            int bgAlpha = (int) (eased * 0x58);

            int bgColor = (bgAlpha << 24) | 0x0B0F17;
            int accentColor = (alpha << 24) | 0x7DD3FC;
            int textColor = (alpha << 24) | 0xF8FAFC;
            int tagColor = (alpha << 24) | 0x94A3B8;

            extractor.fill(boxLeft, y - 2, boxRight, y + 10, bgColor);
            extractor.fill(boxRight - 1, y - 2, boxRight, y + 10, accentColor);

            int bracketIdx = fullName.indexOf(" [");
            if (bracketIdx != -1) {
                String title = Objects.requireNonNull(fullName.substring(0, bracketIdx));
                String tag = Objects.requireNonNull(fullName.substring(bracketIdx));
                extractor.text(font, Component.literal(title), x, y, textColor, true);
                int titleWidth = font.width(title);
                extractor.text(font, Component.literal(tag), x + titleWidth, y, tagColor, true);
            } else {
                extractor.text(font, Component.literal(fullName), x, y, textColor, true);
            }

            y += (int) (13 * eased);
        }
    }
}
