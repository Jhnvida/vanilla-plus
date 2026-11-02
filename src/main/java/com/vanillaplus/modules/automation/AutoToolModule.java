package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class AutoToolModule extends Module {
    private static @Nullable AutoToolModule instance;
    private int previousSlot = -1;
    private boolean isSwapped = false;

    public AutoToolModule() {
        super("auto_tool", "Auto Tool", "Seleciona automaticamente a melhor ferramenta para o bloco minerado",
                ModuleCategory.AUTOMATION);
        instance = this;
    }

    public static @Nullable AutoToolModule getInstance() {
        return instance;
    }

    @Override
    public void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        MultiPlayerGameMode gameMode = client.gameMode;
        if (player == null || level == null || gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();

        if (client.options.keyAttack.isDown() && client.hitResult instanceof BlockHitResult hitResult) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (!state.isAir()) {
                int bestSlot = findBestToolSlot(player, state, config);
                int currentSlot = player.getInventory().getSelectedSlot();

                if (bestSlot != -1 && bestSlot != currentSlot) {
                    if (!isSwapped) {
                        previousSlot = currentSlot;
                        isSwapped = true;
                    }
                    player.getInventory().setSelectedSlot(bestSlot);
                }
            }
        } else {
            if (isSwapped && config.autoToolSwitchBack && previousSlot != -1) {
                player.getInventory().setSelectedSlot(previousSlot);
                isSwapped = false;
                previousSlot = -1;
            } else if (!client.options.keyAttack.isDown()) {
                isSwapped = false;
                previousSlot = -1;
            }
        }
    }

    public static int findBestToolSlot(LocalPlayer player, BlockState state, ModConfig config) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        int currentSlot = player.getInventory().getSelectedSlot();
        ItemStack currentStack = player.getInventory().getItem(currentSlot);
        if (isUsable(currentStack, config)) {
            bestSpeed = currentStack.getDestroySpeed(state);
            bestSlot = currentSlot;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !isUsable(stack, config)) {
                continue;
            }

            float speed = stack.getDestroySpeed(state);
            boolean isCorrectTool = stack.isCorrectToolForDrops(state);

            float score = speed + (isCorrectTool ? 100.0f : 0.0f);
            float currentBestScore = bestSpeed
                    + ((bestSlot != -1 && player.getInventory().getItem(bestSlot).isCorrectToolForDrops(state))
                            ? 100.0f
                            : 0.0f);

            if (score > currentBestScore) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    public static boolean isUsable(ItemStack stack, ModConfig config) {
        if (!config.autoToolProtectDurability || !stack.isDamageableItem()) {
            return true;
        }
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        return remainingDurability > config.autoToolMinDurability;
    }
}
