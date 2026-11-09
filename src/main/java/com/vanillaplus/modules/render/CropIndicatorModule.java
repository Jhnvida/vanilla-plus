package com.vanillaplus.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class CropIndicatorModule extends Module {
    private final Map<Item, ItemStackRenderState> renderStateCache = new HashMap<>();

    public CropIndicatorModule() {
        super("crop_indicator", "Crop Indicator", "Exibe o modelo 3D do produto sobre plantações totalmente maduras",
                ModuleCategory.RENDER);
    }

    public void renderOverlay(LevelRenderContext context) {
        if (!isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();

        if (config.cropIndicatorRequireFarmingItem && !isHoldingFarmingItem(player)) {
            return;
        }

        int radius = config.cropIndicatorRadius;
        int maxCount = config.cropIndicatorMaxCount;
        BlockPos playerPos = player.blockPosition();

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        if (cameraPos == null) {
            cameraPos = player.getEyePosition();
        }

        List<BlockPos> matureCrops = new ArrayList<>();
        int radSq = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radSq) {
                        continue;
                    }

                    BlockPos targetPos = playerPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(targetPos);

                    if (isMatureCrop(state, config)) {
                        matureCrops.add(targetPos);
                    }
                }
            }
        }

        if (matureCrops.isEmpty()) {
            return;
        }

        final Vec3 cam = cameraPos;
        matureCrops.sort(Comparator.comparingDouble(p -> cam.distanceToSqr(Vec3.atCenterOf(p))));

        int countToRender = Math.min(matureCrops.size(), maxCount);

        ItemModelResolver resolver = client.getItemModelResolver();
        SubmitNodeCollector submitNodes = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();
        if (poseStack == null) {
            poseStack = new PoseStack();
        }

        long now = System.currentTimeMillis();
        double bobOffset = Math.sin((now % 2400) / 2400.0 * Math.PI * 2) * 0.04;
        float spinDegrees = (float) ((now % 4000) / 4000.0 * 360.0);

        renderStateCache.clear();

        for (int i = 0; i < countToRender; i++) {
            BlockPos cropPos = matureCrops.get(i);
            BlockState cropState = level.getBlockState(cropPos);
            ItemStack productStack = getProductForCrop(cropState);

            if (productStack.isEmpty()) {
                continue;
            }

            Item itemKey = productStack.getItem();
            ItemStackRenderState itemState = renderStateCache.computeIfAbsent(itemKey, k -> {
                ItemStackRenderState state = new ItemStackRenderState();
                resolver.updateForLiving(state, productStack, ItemDisplayContext.GROUND, player);
                return state;
            });

            if (itemState.isEmpty()) {
                continue;
            }

            double relX = (cropPos.getX() + 0.5) - cameraPos.x;
            double relY = (cropPos.getY() + 0.65 + bobOffset) - cameraPos.y;
            double relZ = (cropPos.getZ() + 0.5) - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(relX, relY, relZ);
            poseStack.rotateDegrees(Axis.YP, spinDegrees);
            poseStack.scale(0.55f, 0.55f, 0.55f);

            itemState.submit(poseStack, submitNodes, 0x00F000F0, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static boolean isMatureCrop(BlockState state, ModConfig config) {
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }

        if (config.cropIndicatorShowNetherWart && block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }

        if (block instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        }

        return false;
    }

    public static ItemStack getProductForCrop(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof BeetrootBlock) {
            return new ItemStack(Items.BEETROOT);
        } else if (block instanceof CarrotBlock) {
            return new ItemStack(Items.CARROT);
        } else if (block instanceof PotatoBlock) {
            return new ItemStack(Items.POTATO);
        } else if (block instanceof CropBlock) {
            return new ItemStack(Items.WHEAT);
        } else if (block instanceof NetherWartBlock) {
            return new ItemStack(Items.NETHER_WART);
        } else if (block instanceof CocoaBlock) {
            return new ItemStack(Items.COCOA_BEANS);
        }

        return ItemStack.EMPTY;
    }

    public static boolean isHoldingFarmingItem(LocalPlayer player) {
        return isFarmingItem(player.getMainHandItem()) || isFarmingItem(player.getOffhandItem());
    }

    private static boolean isFarmingItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        Item item = stack.getItem();
        String name = item.toString().toLowerCase();

        return name.contains("hoe")
                || item == Items.BONE_MEAL
                || item == Items.WHEAT
                || item == Items.WHEAT_SEEDS
                || item == Items.CARROT
                || item == Items.POTATO
                || item == Items.BEETROOT
                || item == Items.BEETROOT_SEEDS
                || item == Items.NETHER_WART
                || item == Items.PUMPKIN_SEEDS
                || item == Items.MELON_SEEDS
                || item == Items.TORCHFLOWER_SEEDS
                || item == Items.PITCHER_POD;
    }
}
