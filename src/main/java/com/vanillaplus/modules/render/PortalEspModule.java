package com.vanillaplus.modules.render;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class PortalEspModule extends Module {
    public enum PortalType {
        NETHER("Portal do Nether", 0xFFD036FF, 0x25D036FF),
        END("Portal do End", 0xFF00FFCC, 0x2500FFCC);

        private final String displayName;
        private final int strokeColor;
        private final int fillColor;

        PortalType(String displayName, int strokeColor, int fillColor) {
            this.displayName = displayName;
            this.strokeColor = strokeColor;
            this.fillColor = fillColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getStrokeColor() {
            return strokeColor;
        }

        public int getFillColor() {
            return fillColor;
        }
    }

    public record PortalCluster(AABB bounds, PortalType type, int blockCount, Vec3 center) {
    }

    private static @Nullable PortalEspModule instance;
    private int tickCounter = 0;
    private List<PortalCluster> cachedClusters = new ArrayList<>();

    public PortalEspModule() {
        super("portal_esp", "Portal ESP",
                "Destaca portais do Nether e do End com caixas 3D, feixe vertical e distância através de blocos",
                ModuleCategory.RENDER);
        instance = this;
    }

    public static @Nullable PortalEspModule getInstance() {
        return instance;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ModConfig config = ConfigManager.getConfig();
        if (config.portalEspEnabled != enabled) {
            config.portalEspEnabled = enabled;
            ConfigManager.save();
        }
        if (!enabled) {
            cachedClusters.clear();
        }
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (!isEnabled()) {
            return;
        }

        tickCounter++;
        if (tickCounter % 15 != 0) {
            return;
        }

        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        updatePortalClusters(player, level, config);
    }

    @SuppressWarnings("null")
    private void updatePortalClusters(LocalPlayer player, ClientLevel level, ModConfig config) {
        int radius = Math.clamp(config.portalEspRadius, 16, 160);
        int chunkRadius = (radius >> 4) + 1;
        ChunkPos playerChunk = player.chunkPosition();

        Map<BlockPos, PortalType> foundBlocks = new HashMap<>();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                int cx = playerChunk.x() + dx;
                int cz = playerChunk.z() + dz;

                LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }

                LevelChunkSection[] sections = chunk.getSections();
                for (int sIdx = 0; sIdx < sections.length; sIdx++) {
                    LevelChunkSection section = sections[sIdx];
                    if (section == null || section.hasOnlyAir()) {
                        continue;
                    }

                    boolean hasNether = config.portalEspNether && section.maybeHas(s -> s.is(Blocks.NETHER_PORTAL));
                    boolean hasEnd = config.portalEspEnd && section.maybeHas(s -> s.is(Blocks.END_PORTAL));

                    if (!hasNether && !hasEnd) {
                        continue;
                    }

                    int baseY = chunk.getSectionYFromSectionIndex(sIdx) << 4;
                    int minX = chunk.getPos().getMinBlockX();
                    int minZ = chunk.getPos().getMinBlockZ();

                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (hasNether && state.is(Blocks.NETHER_PORTAL)) {
                                    foundBlocks.put(new BlockPos(minX + x, baseY + y, minZ + z), PortalType.NETHER);
                                } else if (hasEnd && state.is(Blocks.END_PORTAL)) {
                                    foundBlocks.put(new BlockPos(minX + x, baseY + y, minZ + z), PortalType.END);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.cachedClusters = clusterPortalBlocks(foundBlocks);
    }

    public static List<PortalCluster> clusterPortalBlocks(Map<BlockPos, PortalType> portalBlocks) {
        if (portalBlocks.isEmpty()) {
            return Collections.emptyList();
        }

        List<PortalCluster> clusters = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (Map.Entry<BlockPos, PortalType> entry : portalBlocks.entrySet()) {
            BlockPos startPos = entry.getKey();
            if (visited.contains(startPos)) {
                continue;
            }

            PortalType type = entry.getValue();
            Queue<BlockPos> queue = new ArrayDeque<>();
            queue.add(startPos);
            visited.add(startPos);

            int minX = startPos.getX(), maxX = startPos.getX();
            int minY = startPos.getY(), maxY = startPos.getY();
            int minZ = startPos.getZ(), maxZ = startPos.getZ();
            int count = 0;

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                count++;

                minX = Math.min(minX, current.getX());
                maxX = Math.max(maxX, current.getX());
                minY = Math.min(minY, current.getY());
                maxY = Math.max(maxY, current.getY());
                minZ = Math.min(minZ, current.getZ());
                maxZ = Math.max(maxZ, current.getZ());

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor) && portalBlocks.get(neighbor) == type) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            AABB bounds = new AABB(minX, minY, minZ, maxX + 1.0, maxY + 1.0, maxZ + 1.0);
            Vec3 center = new Vec3((minX + maxX + 1.0) / 2.0, (minY + maxY + 1.0) / 2.0, (minZ + maxZ + 1.0) / 2.0);
            clusters.add(new PortalCluster(bounds, type, count, center));
        }

        return clusters;
    }

    @SuppressWarnings("null")
    public void renderOverlay(LevelRenderContext context) {
        if (!isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || cachedClusters.isEmpty()) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        double maxDistSq = (double) config.portalEspRadius * config.portalEspRadius;

        for (PortalCluster cluster : cachedClusters) {
            double distSq = player.distanceToSqr(cluster.center());
            if (distSq > maxDistSq) {
                continue;
            }

            AABB box = cluster.bounds();
            int strokeColor = cluster.type().getStrokeColor();
            int fillColor = cluster.type().getFillColor();

            if (config.portalEspBoxFill) {
                Gizmos.cuboid(box, GizmoStyle.strokeAndFill(strokeColor, 2.0f, fillColor)).setAlwaysOnTop();
            } else {
                Gizmos.cuboid(box, GizmoStyle.stroke(strokeColor, 2.0f)).setAlwaysOnTop();
            }

            double distance = Math.sqrt(distSq);
            String label = cluster.type().getDisplayName();
            if (config.portalEspShowDistance) {
                label += " [" + (int) Math.round(distance) + "m]";
            }

            float textScale = MobEspModule.calculateLabelScale(distance, config);
            Vec3 textPos = new Vec3(cluster.center().x, box.maxY + 0.22 + (textScale * 0.25), cluster.center().z);

            Gizmos.billboardText(
                    label,
                    textPos,
                    TextGizmo.Style.forColorAndCentered(strokeColor).withScale(textScale))
                    .setAlwaysOnTop();
        }
    }

    public List<PortalCluster> getCachedClusters() {
        return Collections.unmodifiableList(cachedClusters);
    }
}
