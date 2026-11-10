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
        if (!ConfigManager.getConfig().autoBreedIndicatorEnabled) {
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
        }

        return ItemStack.EMPTY;
    }
}
