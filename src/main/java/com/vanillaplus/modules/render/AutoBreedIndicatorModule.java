package com.vanillaplus.modules.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import com.vanillaplus.modules.automation.AutoBreedModule;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class AutoBreedIndicatorModule extends Module {
    private final ItemStackRenderState itemRenderState = new ItemStackRenderState();

    public AutoBreedIndicatorModule() {
        super("breed_indicator", "Breed Indicator",
                "Destaca visualmente animais próximos elegíveis para reprodução com o alimento 3D flutuante",
                ModuleCategory.RENDER);
    }

    public void renderOverlay(LevelRenderContext context) {
        if (!isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        int range = config.autoBreedIndicatorRange;
        Vec3 playerPos = player.position();
        AABB box = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range);

        var nearbyAnimals = level.getEntitiesOfClass(Animal.class, box,
                animal -> AutoBreedModule.isReadyForBreeding(animal, config, player,
                        config.autoBreedIndicatorRequireFood));

        BreedIndicatorStyle style = config.breedIndicatorStyle;
        if (style == null) {
            style = BreedIndicatorStyle.ITEM_3D;
        }

        long now = System.currentTimeMillis();
        double bobOffset = Math.sin((now % 2400) / 2400.0 * Math.PI * 2) * 0.08;
        float spinDegrees = (float) ((now % 3600) / 3600.0 * 360.0);

        float partialTick = context.levelState() != null ? context.levelState().worldPartialTicks
                : client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;

        ItemModelResolver resolver = client.getItemModelResolver();
        SubmitNodeCollector submitNodes = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();

        for (Animal animal : nearbyAnimals) {
            if (animal.distanceToSqr(player) > range * range) {
                continue;
            }

            double animalX = Mth.lerp(partialTick, animal.xOld, animal.getX());
            double animalY = Mth.lerp(partialTick, animal.yOld, animal.getY());
            double animalZ = Mth.lerp(partialTick, animal.zOld, animal.getZ());
            Vec3 animalPos = new Vec3(animalX, animalY, animalZ);
            double targetY = animalPos.y + animal.getBbHeight() + 0.65 + bobOffset;

            if (style == BreedIndicatorStyle.ITEM_3D) {
                ItemStack foodStack = getFoodItemForAnimal(animal, player);
                if (!foodStack.isEmpty()) {
                    itemRenderState.clear();
                    resolver.updateForLiving(itemRenderState, foodStack, ItemDisplayContext.GROUND, animal);

                    if (!itemRenderState.isEmpty()) {
                        double relX = animalPos.x - cameraPos.x;
                        double relY = targetY - cameraPos.y;
                        double relZ = animalPos.z - cameraPos.z;

                        poseStack.pushPose();
                        poseStack.translate(relX, relY, relZ);
                        poseStack.rotateDegrees(Axis.YP, spinDegrees);
                        poseStack.scale(0.70f, 0.70f, 0.70f);

                        itemRenderState.submit(poseStack, submitNodes, 0x00F000F0, OverlayTexture.NO_OVERLAY, 0);
                        poseStack.popPose();
                    }
                }
            } else if (style == BreedIndicatorStyle.OUTLINE) {
                double dx = animalPos.x - animal.getX();
                double dy = animalPos.y - animal.getY();
                double dz = animalPos.z - animal.getZ();
                AABB expandedBox = animal.getBoundingBox().move(dx, dy, dz).inflate(0.12, 0.08, 0.12);
                Gizmos.cuboid(expandedBox, GizmoStyle.stroke(0x80FF2255, 1.5f), false);
            }
        }
    }

    public static ItemStack getFoodItemForAnimal(Animal animal, @Nullable LocalPlayer player) {
        if (player != null) {
            ItemStack main = player.getMainHandItem();
            if (AutoBreedModule.isBreedingFood(animal, main)) {
                return main;
            }
            ItemStack off = player.getOffhandItem();
            if (AutoBreedModule.isBreedingFood(animal, off)) {
                return off;
            }
        }

        if (animal instanceof Cow || animal instanceof Sheep) {
            return new ItemStack(Items.WHEAT);
        } else if (animal instanceof Pig) {
            return new ItemStack(Items.CARROT);
        } else if (animal instanceof Chicken) {
            return new ItemStack(Items.WHEAT_SEEDS);
        } else {
            String name = animal.getType().toString().toLowerCase();
            if (name.contains("rabbit") || name.contains("coelho")) {
                return new ItemStack(Items.CARROT);
            } else if (name.contains("horse") || name.contains("donkey") || name.contains("mule")) {
                return new ItemStack(Items.GOLDEN_CARROT);
            } else if (name.contains("cat") || name.contains("ocelot")) {
                return new ItemStack(Items.COD);
            } else if (name.contains("wolf")) {
                return new ItemStack(Items.BEEF);
            } else if (name.contains("turtle")) {
                return new ItemStack(Items.SEAGRASS);
            } else if (name.contains("frog")) {
                return new ItemStack(Items.MAGMA_CREAM);
            }
            return new ItemStack(Items.WHEAT);
        }
    }
}
