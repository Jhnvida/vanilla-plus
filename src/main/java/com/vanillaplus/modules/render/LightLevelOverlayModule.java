package com.vanillaplus.modules.render;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LightLevelOverlayModule extends Module {
    private static LightLevelOverlayModule instance;

    public LightLevelOverlayModule() {
        super("light_level_overlay", "Light Level",
                "Exibe indicadores visuais dos níveis de luz no chão para spawn de mobs", ModuleCategory.RENDER);
        instance = this;
    }

    public static LightLevelOverlayModule getInstance() {
        return instance;
    }

    public void renderOverlay(LevelRenderContext context) {
        if (!isEnabled())
            return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return;

        ModConfig config = ConfigManager.getConfig();
        int radius = Math.clamp(config.lightLevelOverlayRadius, 4, 24);
        BlockPos playerPos = client.player.blockPosition();

        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > radius * radius)
                    continue;

                for (int y = -4; y <= 4; y++) {
                    candidate.set(playerPos.getX() + x, playerPos.getY() + y, playerPos.getZ() + z);
                    BlockState airState = client.level.getBlockState(candidate);

                    if (!airState.isAir())
                        continue;

                    BlockPos belowPos = candidate.below();
                    BlockState groundState = client.level.getBlockState(belowPos);

                    if (!groundState.isFaceSturdy(client.level, belowPos, Direction.UP))
                        continue;

                    int blockLight = client.level.getBrightness(LightLayer.BLOCK, candidate);
                    int skyLight = client.level.getBrightness(LightLayer.SKY, candidate);

                    if (blockLight == 0) {
                        int color;
                        if (skyLight > 0) {
                            color = 0xFFEEDD00;
                        } else {
                            color = 0xFFFF2222;
                        }

                        double topY = candidate.getY() + 0.02;
                        Vec3 p1 = new Vec3(candidate.getX() + 0.1, topY, candidate.getZ() + 0.1);
                        Vec3 p2 = new Vec3(candidate.getX() + 0.9, topY, candidate.getZ() + 0.9);
                        Vec3 p3 = new Vec3(candidate.getX() + 0.1, topY, candidate.getZ() + 0.9);
                        Vec3 p4 = new Vec3(candidate.getX() + 0.9, topY, candidate.getZ() + 0.1);

                        Gizmos.line(p1, p2, color, 2.5f);
                        Gizmos.line(p3, p4, color, 2.5f);
                    } else if (config.lightLevelShowSafe) {
                        int color = 0xFF22FF44;
                        double topY = candidate.getY() + 0.02;
                        Vec3 p1 = new Vec3(candidate.getX() + 0.3, topY, candidate.getZ() + 0.5);
                        Vec3 p2 = new Vec3(candidate.getX() + 0.7, topY, candidate.getZ() + 0.5);
                        Gizmos.line(p1, p2, color, 2.0f);
                    }
                }
            }
        }
    }
}
