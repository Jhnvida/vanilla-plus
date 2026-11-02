package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoReplantModule extends Module {
    private long lastHarvestTime = 0;

    public AutoReplantModule() {
        super("auto_replant", "Auto Replant", "Colhe e replanta culturas maduras no inventário automaticamente",
                ModuleCategory.AUTOMATION);
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();

        if (config.rightClickHarvestOnly) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastHarvestTime < config.autoReplantIntervalMs) {
            return;
        }

        int radius = (int) Math.ceil(config.autoReplantRadius);
        BlockPos playerPos = client.player.blockPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = playerPos.offset(x, y, z);
                    if (targetPos.distSqr(playerPos) <= config.autoReplantRadius * config.autoReplantRadius) {
                        BlockState state = client.level.getBlockState(targetPos);
                        if (isTargetCropMature(state, config)) {
                            Item seed = getRequiredSeed(state.getBlock());
                            if (seed != null && hasSeedInInventory(client, seed)) {
                                harvestAndReplant(client, targetPos);
                                lastHarvestTime = now;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean handleRightClick(Minecraft client, BlockHitResult hitResult) {
        if (!isEnabled() || client.level == null || client.player == null) {
            return false;
        }

        ModConfig config = ConfigManager.getConfig();
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = client.level.getBlockState(pos);

        if (isTargetCropMature(state, config)) {
            Item seed = getRequiredSeed(state.getBlock());
            if (seed != null && hasSeedInInventory(client, seed)) {
                harvestAndReplant(client, pos);
                return true;
            }
        }
        return false;
    }

    private void harvestAndReplant(Minecraft client, BlockPos pos) {
        if (client.gameMode == null || client.player == null)
            return;

        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                Direction.UP,
                pos,
                false);

        client.gameMode.startDestroyBlock(pos, Direction.UP);
        client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hitResult);
        client.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
    }

    public static boolean isTargetCropMature(BlockState state, ModConfig config) {
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            if (crop instanceof CarrotBlock && !config.replantCarrots)
                return false;
            if (crop instanceof PotatoBlock && !config.replantPotatoes)
                return false;
            if (crop instanceof BeetrootBlock && !config.replantBeetroots)
                return false;
            if (block == Blocks.WHEAT && !config.replantWheat)
                return false;

            return crop.isMaxAge(state);
        }

        return false;
    }

    public static Item getRequiredSeed(Block block) {
        if (block == Blocks.WHEAT)
            return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS)
            return Items.CARROT;
        if (block == Blocks.POTATOES)
            return Items.POTATO;
        if (block == Blocks.BEETROOTS)
            return Items.BEETROOT_SEEDS;
        return null;
    }

    private boolean hasSeedInInventory(Minecraft client, Item seed) {
        if (client.player == null)
            return false;
        return client.player.getInventory().contains(new ItemStack(seed));
    }
}
