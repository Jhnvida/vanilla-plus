package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.mixin.FishingHookAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;

public class AutoFishModule extends Module {
    private static AutoFishModule instance;
    private long recastScheduledTime = -1;
    private InteractionHand activeHand = InteractionHand.MAIN_HAND;

    public AutoFishModule() {
        super("auto_fish", "Auto Fish", "Detecta mordida de peixes e recolhe/relança a vara automaticamente",
                ModuleCategory.AUTOMATION);
        instance = this;
    }

    public static AutoFishModule getInstance() {
        return instance;
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        long now = System.currentTimeMillis();

        if (recastScheduledTime != -1 && now >= recastScheduledTime) {
            recastScheduledTime = -1;
            if (isHoldingFishingRod(client)) {
                client.gameMode.useItem(client.player, activeHand);
                client.player.swing(activeHand, client.player.getItemInHand(activeHand).getInteractAnimation(), true);
            }
            return;
        }

        if (recastScheduledTime != -1) {
            return;
        }

        FishingHook hook = client.player.fishing;
        if (hook != null && isHoldingFishingRod(client)) {
            boolean isBiting = false;
            try {
                isBiting = ((FishingHookAccessor) hook).isBiting();
            } catch (Exception ignored) {
            }

            if (!isBiting && hook.getDeltaMovement().y < -0.06) {
                isBiting = true;
            }

            if (isBiting) {
                client.gameMode.useItem(client.player, activeHand);
                client.player.swing(activeHand, client.player.getItemInHand(activeHand).getInteractAnimation(), true);

                recastScheduledTime = now + Math.max(300, config.autoFishRecastDelayMs);
            }
        }
    }

    private boolean isHoldingFishingRod(Minecraft client) {
        if (client.player.getMainHandItem().getItem() == Items.FISHING_ROD) {
            activeHand = InteractionHand.MAIN_HAND;
            return true;
        }
        if (client.player.getOffhandItem().getItem() == Items.FISHING_ROD) {
            activeHand = InteractionHand.OFF_HAND;
            return true;
        }
        return false;
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        recastScheduledTime = -1;
    }
}
