package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
    public String getDisplayName() {
        ModConfig config = ConfigManager.getConfig();
        if (config.rightClickHarvestOnly) {
            return "Auto Replant [click]";
        } else {
            return "Auto Replant [click|area]";
        }
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
                                harvestAndReplant(client, targetPos, seed);
                                lastHarvestTime = now;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (!isEnabled() || !level.isClientSide()) {
            return InteractionResult.PASS;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player != player) {
            return InteractionResult.PASS;
        }

        ModConfig config = ConfigManager.getConfig();
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (isTargetCropMature(state, config)) {
            Item seed = getRequiredSeed(state.getBlock());
            if (seed != null && hasSeedInInventory(client, seed)) {
                harvestAndReplant(client, pos, seed);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void harvestAndReplant(Minecraft client, BlockPos pos, Item seed) {
        if (client.gameMode == null || client.player == null)
            return;

        BlockHitResult farmHit = new BlockHitResult(
                new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                Direction.UP,
                pos.below(),
                false);

        InteractionHand hand = InteractionHand.MAIN_HAND;
        int previousSlot = client.player.getInventory().getSelectedSlot();
        boolean swapped = false;

        if (client.player.getMainHandItem().is(seed)) {
            hand = InteractionHand.MAIN_HAND;
        } else if (client.player.getOffhandItem().is(seed)) {
            hand = InteractionHand.OFF_HAND;
        } else {
            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getItem(i).is(seed)) {
                    client.player.getInventory().setSelectedSlot(i);
                    swapped = true;
                    hand = InteractionHand.MAIN_HAND;
                    break;
                }
            }
        }

        client.gameMode.startDestroyBlock(pos, Direction.UP);

        client.gameMode.useItemOn(client.player, hand, farmHit);
        client.player.swing(hand, new ItemStack(seed).getInteractAnimation(), true);

        if (swapped) {
            client.player.getInventory().setSelectedSlot(previousSlot);
        }
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
