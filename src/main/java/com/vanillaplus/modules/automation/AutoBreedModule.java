package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AutoBreedModule extends Module {
    private long lastBreedTime = 0;

    public AutoBreedModule() {
        super("auto_breed", "Auto Breed", "Alimenta automaticamente animais elegíveis para reprodução nas proximidades",
                ModuleCategory.AUTOMATION);
    }

    @Override
    public void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        long now = System.currentTimeMillis();

        if (now - lastBreedTime < config.autoBreedIntervalMs) {
            return;
        }

        double range = config.autoBreedRange;
        Vec3 playerPos = client.player.position();
        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range);

        List<Animal> nearbyAnimals = client.level.getEntitiesOfClass(Animal.class, searchBox,
                animal -> isEligible(animal, config, client));

        for (Animal animal : nearbyAnimals) {
            if (animal.distanceToSqr(client.player) <= range * range) {
                InteractionHand feedHand = getFoodHand(animal, client);
                if (feedHand != null) {
                    client.gameMode.interact(client.player, animal, new EntityHitResult(animal), feedHand);
                    client.player.swing(feedHand, SwingAnimation.DEFAULT, true);
                    lastBreedTime = now;
                    break;
                }
            }
        }
    }

    public boolean isEligible(Animal animal, ModConfig config, Minecraft client) {
        if (animal == null || animal.isBaby() || animal.getAge() != 0 || animal.isInLove()) {
            return false;
        }

        if (animal instanceof Cow && !config.breedCows) {
            return false;
        }
        if (animal instanceof Sheep && !config.breedSheep) {
            return false;
        }
        if (animal instanceof Pig && !config.breedPigs) {
            return false;
        }
        if (animal instanceof Chicken && !config.breedChickens) {
            return false;
        }

        return getFoodHand(animal, client) != null;
    }

    private InteractionHand getFoodHand(Animal animal, Minecraft client) {
        if (client.player == null)
            return null;

        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();

        if (isBreedingFood(animal, mainHand)) {
            return InteractionHand.MAIN_HAND;
        }
        if (isBreedingFood(animal, offHand)) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }

    public static boolean isBreedingFood(Animal animal, ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;

        if (animal instanceof Cow || animal instanceof Sheep) {
            return stack.getItem() == Items.WHEAT;
        }

        if (animal instanceof Pig) {
            return stack.getItem() == Items.CARROT || stack.getItem() == Items.POTATO
                    || stack.getItem() == Items.BEETROOT;
        }

        if (animal instanceof Chicken) {
            return stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() == Items.BEETROOT_SEEDS ||
                    stack.getItem() == Items.MELON_SEEDS || stack.getItem() == Items.PUMPKIN_SEEDS;
        }

        return false;
    }
}
