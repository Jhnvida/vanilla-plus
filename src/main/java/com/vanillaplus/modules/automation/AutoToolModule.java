package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AutoToolModule extends Module {
    private static AutoToolModule instance;
    private int previousSlot = -1;
    private boolean isSwapped = false;

    public AutoToolModule() {
        super("auto_tool", "Auto Tool", "Seleciona automaticamente a melhor ferramenta para o bloco minerado",
                ModuleCategory.AUTOMATION);
        instance = this;
    }

    public static AutoToolModule getInstance() {
        return instance;
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();

        if (client.options.keyAttack.isDown() && client.hitResult instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = client.level.getBlockState(pos);

            if (!state.isAir()) {
                int bestSlot = findBestToolSlot(client, state, config);
                int currentSlot = client.player.getInventory().getSelectedSlot();

                if (bestSlot != -1 && bestSlot != currentSlot) {
                    if (!isSwapped) {
                        previousSlot = currentSlot;
                        isSwapped = true;
                    }
                    client.player.getInventory().setSelectedSlot(bestSlot);
                }
            }
        } else {
            if (isSwapped && config.autoToolSwitchBack && previousSlot != -1) {
                client.player.getInventory().setSelectedSlot(previousSlot);
                isSwapped = false;
                previousSlot = -1;
            } else if (!client.options.keyAttack.isDown()) {
                isSwapped = false;
                previousSlot = -1;
            }
        }
    }

    private int findBestToolSlot(Minecraft client, BlockState state, ModConfig config) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        int currentSlot = client.player.getInventory().getSelectedSlot();
        ItemStack currentStack = client.player.getInventory().getItem(currentSlot);
        if (isUsable(currentStack, config)) {
            bestSpeed = currentStack.getDestroySpeed(state);
            bestSlot = currentSlot;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.isEmpty() || !isUsable(stack, config)) {
                continue;
            }

            float speed = stack.getDestroySpeed(state);
            boolean isCorrectTool = stack.isCorrectToolForDrops(state);

            float score = speed + (isCorrectTool ? 100.0f : 0.0f);
            float currentBestScore = bestSpeed
                    + ((bestSlot != -1 && client.player.getInventory().getItem(bestSlot).isCorrectToolForDrops(state))
                            ? 100.0f
                            : 0.0f);

            if (score > currentBestScore) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private boolean isUsable(ItemStack stack, ModConfig config) {
        if (!config.autoToolProtectDurability || !stack.isDamageableItem()) {
            return true;
        }
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        return remainingDurability > config.autoToolMinDurability;
    }
}
