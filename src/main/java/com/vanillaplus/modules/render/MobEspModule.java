package com.vanillaplus.modules.render;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public class MobEspModule extends Module {
    private static @Nullable MobEspModule instance;

    public MobEspModule() {
        super("mob_esp", "Mob ESP",
                "Destaca entidades inimigas no mundo com caixas 3D e nomes visíveis através de blocos",
                ModuleCategory.RENDER);
        instance = this;
    }

    public static @Nullable MobEspModule getInstance() {
        return instance;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ModConfig config = ConfigManager.getConfig();
        if (config.mobEspEnabled != enabled) {
            config.mobEspEnabled = enabled;
            ConfigManager.save();
        }
    }

    @SuppressWarnings("null")
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
        int range = Math.clamp(config.mobEspRange, 4, 128);
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range);

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive() && shouldRenderEntity(entity, config));

        if (entities.isEmpty()) {
            return;
        }

        float partialTick = context.levelState() != null
                ? context.levelState().worldPartialTicks
                : client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        double rangeSq = (double) range * range;

        for (LivingEntity entity : entities) {
            double distSq = entity.distanceToSqr(player);
            if (distSq > rangeSq) {
                continue;
            }

            double interpX = Mth.lerp(partialTick, entity.xOld, entity.getX());
            double interpY = Mth.lerp(partialTick, entity.yOld, entity.getY());
            double interpZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

            double dx = interpX - entity.getX();
            double dy = interpY - entity.getY();
            double dz = interpZ - entity.getZ();

            AABB box = entity.getBoundingBox().move(dx, dy, dz);
            int color = getEntityColor(entity, config);

            Gizmos.cuboid(box, GizmoStyle.stroke(color, 1.5f)).setAlwaysOnTop();

            double distance = Math.sqrt(distSq);
            String label = formatEntityLabel(entity, distance, config);

            if (!label.isEmpty()) {
                float textScale = calculateLabelScale(distance, config);
                Vec3 textPos = new Vec3(interpX, box.maxY + 0.15 + (textScale * 0.35), interpZ);
                Gizmos.billboardText(
                        label,
                        textPos,
                        TextGizmo.Style.forColorAndCentered(color).withScale(textScale))
                        .setAlwaysOnTop();
            }
        }
    }

    public static float calculateLabelScale(double distance, ModConfig config) {
        float baseMultiplier = Math.clamp(config.mobEspTextScale, 0.4f, 2.5f);
        if (!config.mobEspResponsiveText) {
            return 0.30f * baseMultiplier;
        }

        float factor = 0.026f;
        float responsive = (float) (distance * factor);
        float clamped = Math.clamp(responsive, 0.07f, 0.70f);
        return clamped * baseMultiplier;
    }

    public static boolean shouldRenderEntity(LivingEntity entity, ModConfig config) {
        if (entity instanceof Player player) {
            if (player.isLocalPlayer()) {
                return false;
            }
            return config.mobEspPlayers;
        }

        if (entity instanceof Creeper) {
            return config.mobEspCreeper;
        }
        if (entity instanceof AbstractSkeleton) {
            return config.mobEspSkeleton;
        }
        if (entity instanceof Zombie) {
            return config.mobEspZombie;
        }
        if (entity instanceof Spider) {
            return config.mobEspSpider;
        }
        if (entity instanceof Witch) {
            return config.mobEspWitch;
        }
        if (entity instanceof Enderman) {
            return config.mobEspEnderman;
        }
        if (entity instanceof AbstractCubeMob) {
            return config.mobEspSlime;
        }
        if (entity instanceof Phantom) {
            return config.mobEspPhantom;
        }
        if (entity instanceof Blaze || entity instanceof Ghast || entity instanceof PiglinBrute
                || entity instanceof Hoglin || entity instanceof Zoglin) {
            return config.mobEspNether;
        }
        if (entity instanceof AbstractIllager || entity instanceof Ravager || entity instanceof Vex) {
            return config.mobEspIllager;
        }
        if (entity instanceof Warden || entity instanceof WitherBoss || entity instanceof EnderDragon) {
            return config.mobEspBoss;
        }

        if (entity instanceof Enemy) {
            return config.mobEspOther;
        }

        return false;
    }

    public static int getEntityColor(LivingEntity entity, ModConfig config) {
        if (config.mobEspColorMode == MobEspColorMode.WHITE) {
            return 0xFFFFFFFF;
        }

        if (entity instanceof Creeper) {
            return 0xFF50E36B;
        }
        if (entity instanceof AbstractSkeleton) {
            return 0xFFE0E0E0;
        }
        if (entity instanceof Zombie) {
            return 0xFF2CC990;
        }
        if (entity instanceof Spider) {
            return 0xFFD9534F;
        }
        if (entity instanceof Witch) {
            return 0xFFB660CD;
        }
        if (entity instanceof Enderman) {
            return 0xFFCC44FF;
        }
        if (entity instanceof AbstractCubeMob) {
            return 0xFF8AE85B;
        }
        if (entity instanceof Phantom) {
            return 0xFF4D9DE0;
        }
        if (entity instanceof Blaze || entity instanceof Ghast || entity instanceof PiglinBrute
                || entity instanceof Hoglin || entity instanceof Zoglin) {
            return 0xFFFF7700;
        }
        if (entity instanceof AbstractIllager || entity instanceof Ravager || entity instanceof Vex) {
            return 0xFF9E9E9E;
        }
        if (entity instanceof Warden || entity instanceof WitherBoss || entity instanceof EnderDragon) {
            return 0xFFFF2222;
        }
        if (entity instanceof Player) {
            return 0xFF38BDF8;
        }

        return 0xFFFFCC00;
    }

    public static String formatEntityLabel(LivingEntity entity, double distance, ModConfig config) {
        StringBuilder sb = new StringBuilder();

        if (config.mobEspShowName) {
            net.minecraft.network.chat.Component customName = entity.getCustomName();
            if (customName != null) {
                sb.append(customName.getString());
            } else {
                sb.append(entity.getName().getString());
            }
        }

        if (config.mobEspShowDistance) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append("[").append((int) Math.round(distance)).append("m]");
        }

        if (config.mobEspShowHealth) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            int hp = (int) Math.ceil(entity.getHealth());
            int maxHp = (int) Math.ceil(entity.getMaxHealth());
            sb.append("(").append(hp).append("/").append(maxHp).append(")");
        }

        return Objects.requireNonNull(sb.toString());
    }
}
