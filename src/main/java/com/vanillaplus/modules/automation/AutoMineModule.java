package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@NullMarked
public class AutoMineModule extends Module {
    public enum MineState {
        IDLE,
        SELECTING_POS1,
        SELECTING_POS2,
        MINING,
        PAUSED,
        COMPLETED
    }

    private MineState state = MineState.IDLE;
    private @Nullable BlockPos pos1;
    private @Nullable BlockPos pos2;
    private @Nullable BlockPos currentMiningPos;

    private int totalBlocksInArea = 0;
    private int minedBlocksCount = 0;
    private int delayCooldown = 0;

    public AutoMineModule() {
        super("auto_mine", "Auto Mine", "Escava automaticamente toda a área delimitada entre dois pontos",
                ModuleCategory.AUTOMATION);
    }

    public MineState getMineState() {
        return state;
    }

    public @Nullable BlockPos getPos1() {
        return pos1;
    }

    public @Nullable BlockPos getPos2() {
        return pos2;
    }

    public void setPos1(@Nullable BlockPos pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(@Nullable BlockPos pos2) {
        this.pos2 = pos2;
    }

    public void resetSelection() {
        this.pos1 = null;
        this.pos2 = null;
        this.currentMiningPos = null;
        this.totalBlocksInArea = 0;
        this.minedBlocksCount = 0;
        if (isEnabled()) {
            this.state = MineState.SELECTING_POS1;
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.sendSystemMessage(
                        Component.literal("§e[Auto Mine] Seleção reiniciada. Clique no Ponto 1 (inicial)."));
            }
        } else {
            this.state = MineState.IDLE;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Minecraft client = Minecraft.getInstance();

        if (enabled) {
            if (pos1 == null) {
                state = MineState.SELECTING_POS1;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("§e[Auto Mine] Modo de seleção ativo. Clique no Ponto 1 (inicial)."));
                }
            } else if (pos2 == null) {
                state = MineState.SELECTING_POS2;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("§e[Auto Mine] Clique no Ponto 2 (final) para delimitar a área."));
                }
            } else {
                state = MineState.MINING;
                recalculateTotalBlocks();
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal(
                                    "§a[Auto Mine] Escavação retomada (" + (totalBlocksInArea - countRemainingBlocks())
                                            + "/" + totalBlocksInArea + " blocos)."));
                }
            }
        } else {
            if (state == MineState.MINING) {
                state = MineState.PAUSED;
            } else if (state == MineState.SELECTING_POS1 || state == MineState.SELECTING_POS2) {
                state = MineState.IDLE;
            }
            if (client.gameMode != null) {
                client.gameMode.stopDestroyBlock();
            }
            currentMiningPos = null;
        }
    }

    @Override
    public String getDisplayName() {
        if (!isEnabled()) {
            return "Auto Mine";
        }
        return switch (state) {
            case SELECTING_POS1 -> "Auto Mine [sel 1/2]";
            case SELECTING_POS2 -> "Auto Mine [sel 2/2]";
            case MINING -> {
                int remaining = countRemainingBlocks();
                int mined = Math.max(0, totalBlocksInArea - remaining);
                yield "Auto Mine [" + mined + "/" + totalBlocksInArea + "]";
            }
            case PAUSED -> "Auto Mine [pausado]";
            case COMPLETED -> "Auto Mine [concluído]";
            case IDLE -> "Auto Mine";
        };
    }

    public InteractionResult onAttackBlock(Player player, Level level, InteractionHand hand, BlockPos pos,
            Direction direction) {
        return handleBlockSelection(player, level, pos);
    }

    public InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        return handleBlockSelection(player, level, hitResult.getBlockPos());
    }

    private InteractionResult handleBlockSelection(Player player, Level level, BlockPos pos) {
        if (!isEnabled() || !level.isClientSide()) {
            return InteractionResult.PASS;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player != player) {
            return InteractionResult.PASS;
        }

        if (state == MineState.SELECTING_POS1) {
            pos1 = pos;
            state = MineState.SELECTING_POS2;
            player.sendSystemMessage(
                    Component.literal("§a[Auto Mine] Ponto 1 definido em " + pos.toShortString()
                            + ". Agora clique no Ponto 2 (final)!"));
            return InteractionResult.SUCCESS;
        } else if (state == MineState.SELECTING_POS2) {
            pos2 = pos;
            state = MineState.MINING;
            recalculateTotalBlocks();
            player.sendSystemMessage(
                    Component.literal("§a[Auto Mine] Ponto 2 definido em " + pos.toShortString()
                            + ". Iniciando escavação (" + totalBlocksInArea + " blocos no total)!"));
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        MultiPlayerGameMode gameMode = client.gameMode;
        if (player == null || level == null || gameMode == null) {
            return;
        }

        if (state != MineState.MINING || pos1 == null || pos2 == null) {
            return;
        }

        if (delayCooldown > 0) {
            delayCooldown--;
            return;
        }

        ModConfig config = ConfigManager.getConfig();

        BlockPos currentPos = this.currentMiningPos;
        if (currentPos != null) {
            BlockState currentState = level.getBlockState(currentPos);
            if (currentState.isAir()) {
                this.currentMiningPos = null;
                minedBlocksCount++;
                delayCooldown = config.autoMineDelayTicks;
                gameMode.stopDestroyBlock();
            } else {
                if (isBlockWithinReach(player, currentPos, config.autoMineRange)) {
                    if (config.autoMineAutoTool) {
                        int bestSlot = AutoToolModule.findBestToolSlot(player, currentState, config);
                        if (bestSlot != -1 && bestSlot != player.getInventory().getSelectedSlot()) {
                            player.getInventory().setSelectedSlot(bestSlot);
                        }
                    }
                    gameMode.continueDestroyBlock(currentPos, Direction.UP);
                    player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getAttackAnimation(), false);
                    return;
                } else {
                    gameMode.stopDestroyBlock();
                    this.currentMiningPos = null;
                }
            }
        }

        BlockPos nextTarget = findNextBlockToMine(player, level, config);
        if (nextTarget != null) {
            BlockState targetState = level.getBlockState(nextTarget);
            if (config.autoMineAutoTool) {
                int bestSlot = AutoToolModule.findBestToolSlot(player, targetState, config);
                if (bestSlot != -1 && bestSlot != player.getInventory().getSelectedSlot()) {
                    player.getInventory().setSelectedSlot(bestSlot);
                }
            }

            currentMiningPos = nextTarget;
            gameMode.startDestroyBlock(nextTarget, Direction.UP);
            player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getAttackAnimation(), false);
        } else {
            if (countRemainingBlocks() == 0) {
                int finishedBlocks = minedBlocksCount;
                pos1 = null;
                pos2 = null;
                currentMiningPos = null;
                state = MineState.COMPLETED;
                setEnabled(false);
                player.sendSystemMessage(
                        Component.literal("§a[Auto Mine] Escavação finalizada com sucesso! (" + finishedBlocks
                                + " blocos minerados)."));
            }
        }
    }

    public void renderOverlay(LevelRenderContext context) {
        if (!isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        HitResult hit = client.hitResult;

        BlockPos p1 = this.pos1;
        BlockPos p2 = this.pos2;

        if (p1 != null && p2 == null) {
            Gizmos.cuboid(new AABB(p1), GizmoStyle.strokeAndFill(0xFF00FF88, 2.0f, 0x3000FF88), false);
            if (hit instanceof BlockHitResult blockHit && !blockHit.getBlockPos().equals(p1)) {
                AABB previewBox = createAABB(p1, blockHit.getBlockPos());
                Gizmos.cuboid(previewBox, GizmoStyle.stroke(0x80FFFFFF, 1.5f), false);
            }
        }

        if (p1 != null && p2 != null) {
            AABB areaBox = createAABB(p1, p2);
            Gizmos.cuboid(areaBox, GizmoStyle.strokeAndFill(0xCC3388FF, 2.0f, 0x183388FF), false);

            BlockPos currentPos = this.currentMiningPos;
            if (currentPos != null) {
                Gizmos.cuboid(new AABB(currentPos), GizmoStyle.strokeAndFill(0xFFFFBB00, 2.5f, 0x55FFBB00),
                        false);
            }
        }
    }

    public static AABB createAABB(BlockPos p1, BlockPos p2) {
        int minX = Math.min(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxX = Math.max(p1.getX(), p2.getX()) + 1;
        int maxY = Math.max(p1.getY(), p2.getY()) + 1;
        int maxZ = Math.max(p1.getZ(), p2.getZ()) + 1;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private void recalculateTotalBlocks() {
        BlockPos p1 = this.pos1;
        BlockPos p2 = this.pos2;
        if (p1 == null || p2 == null) {
            totalBlocksInArea = 0;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }

        int count = 0;
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isBreakableBlock(s, level, p, ConfigManager.getConfig())) {
                        count++;
                    }
                }
            }
        }
        totalBlocksInArea = count;
    }

    private int countRemainingBlocks() {
        BlockPos p1 = this.pos1;
        BlockPos p2 = this.pos2;
        if (p1 == null || p2 == null) {
            return 0;
        }
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            return 0;
        }

        int count = 0;
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isBreakableBlock(s, level, p, ConfigManager.getConfig())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @SuppressWarnings("null")
    private @Nullable BlockPos findNextBlockToMine(LocalPlayer player, ClientLevel level, ModConfig config) {
        BlockPos p1 = this.pos1;
        BlockPos p2 = this.pos2;
        if (p1 == null || p2 == null) {
            return null;
        }

        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        for (int y = maxY; y >= minY; y--) {
            List<BlockPos> layerCandidates = new ArrayList<>();
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (isBreakableBlock(s, level, p, config) && isBlockWithinReach(player, p, config.autoMineRange)) {
                        layerCandidates.add(p);
                    }
                }
            }

            if (!layerCandidates.isEmpty()) {
                Vec3 eyePos = player.getEyePosition();
                layerCandidates.sort(Comparator.comparingDouble(p -> eyePos.distanceToSqr(Vec3.atCenterOf(p))));
                return layerCandidates.get(0);
            }
        }

        return null;
    }

    private boolean isBreakableBlock(BlockState state, ClientLevel level, BlockPos pos, ModConfig config) {
        if (state.isAir() || state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.BARRIER) {
            return false;
        }

        if (state.getDestroySpeed(level, pos) < 0) {
            return false;
        }

        if (!state.getFluidState().isEmpty()) {
            return false;
        }

        if (config.autoMineAvoidLava) {
            for (Direction dir : Direction.values()) {
                BlockState neighbor = level.getBlockState(pos.relative(dir));
                if (neighbor.getBlock() == Blocks.LAVA) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBlockWithinReach(LocalPlayer player, BlockPos pos, double maxReach) {
        Vec3 center = Vec3.atCenterOf(pos);
        return player.getEyePosition().distanceTo(center) <= maxReach;
    }
}
