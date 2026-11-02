package com.vanillaplus.modules.automation;

import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AutoRefillModule extends Module {
    private static AutoRefillModule instance;
    private Item lastHeldItem = null;
    private int lastSelectedSlot = -1;

    public AutoRefillModule() {
        super("auto_refill", "Auto Refill", "Repõe automaticamente itens na hotbar quando o stack atual esgota",
                ModuleCategory.AUTOMATION);
        instance = this;
    }

    public static AutoRefillModule getInstance() {
        return instance;
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null || client.gameMode == null || client.player.inventoryMenu == null) {
            lastHeldItem = null;
            lastSelectedSlot = -1;
            return;
        }

        int currentSlot = client.player.getInventory().getSelectedSlot();
        ItemStack currentStack = client.player.getInventory().getItem(currentSlot);

        if (currentSlot == lastSelectedSlot && lastHeldItem != null && currentStack.isEmpty()) {
            int refillSlot = findMatchingInventorySlot(client, lastHeldItem);
            if (refillSlot != -1) {
                client.gameMode.handleContainerInput(
                        client.player.inventoryMenu.containerId,
                        refillSlot,
                        currentSlot,
                        ContainerInput.SWAP,
                        client.player);
            }
        }

        lastSelectedSlot = currentSlot;
        lastHeldItem = currentStack.isEmpty() ? null : currentStack.getItem();
    }

    private int findMatchingInventorySlot(Minecraft client, Item item) {
        for (int i = 9; i <= 35; i++) {
            ItemStack stack = client.player.inventoryMenu.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }
}
