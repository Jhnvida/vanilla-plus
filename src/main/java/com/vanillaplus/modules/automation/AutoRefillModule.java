package com.vanillaplus.modules.automation;

import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class AutoRefillModule extends Module {
    private static @Nullable AutoRefillModule instance;
    private @Nullable Item lastHeldItem = null;
    private int lastSelectedSlot = -1;

    public AutoRefillModule() {
        super("auto_refill", "Auto Refill", "Repõe automaticamente itens na hotbar quando o stack atual esgota",
                ModuleCategory.AUTOMATION);
        instance = this;
    }

    public static @Nullable AutoRefillModule getInstance() {
        return instance;
    }

    @Override
    public void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        MultiPlayerGameMode gameMode = client.gameMode;
        if (player == null || gameMode == null) {
            lastHeldItem = null;
            lastSelectedSlot = -1;
            return;
        }

        int currentSlot = player.getInventory().getSelectedSlot();
        ItemStack currentStack = player.getInventory().getItem(currentSlot);

        Item itemToRefill = lastHeldItem;
        if (currentSlot == lastSelectedSlot && itemToRefill != null && currentStack.isEmpty()) {
            int refillSlot = findMatchingInventorySlot(player, itemToRefill);
            if (refillSlot != -1) {
                gameMode.handleContainerInput(
                        player.inventoryMenu.containerId,
                        refillSlot,
                        currentSlot,
                        ContainerInput.SWAP,
                        player);
            }
        }

        lastSelectedSlot = currentSlot;
        lastHeldItem = currentStack.isEmpty() ? null : currentStack.getItem();
    }

    private int findMatchingInventorySlot(LocalPlayer player, Item item) {
        for (int i = 9; i <= 35; i++) {
            ItemStack stack = player.inventoryMenu.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
}
