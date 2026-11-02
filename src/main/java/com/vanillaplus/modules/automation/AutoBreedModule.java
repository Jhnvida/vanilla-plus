package com.vanillaplus.modules.automation;

import com.vanillaplus.config.ConfigManager;
import com.vanillaplus.config.ModConfig;
import com.vanillaplus.core.Module;
import com.vanillaplus.core.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public class AutoBreedModule extends Module {
    private long lastBreedTime = 0;
    private static final Map<Integer, Long> globalCooldowns = new ConcurrentHashMap<>();

    public AutoBreedModule() {
        super("auto_breed", "Auto Breed", "Alimenta automaticamente animais elegíveis para reprodução nas proximidades",
                ModuleCategory.AUTOMATION);
    }

    public static void recordCooldown(int animalId, long durationMs) {
        globalCooldowns.put(animalId, System.currentTimeMillis() + durationMs);
    }

    public static boolean isEntityCoolingDown(int animalId) {
        Long expiry = globalCooldowns.get(animalId);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            globalCooldowns.remove(animalId);
            return false;
        }
        return true;
    }

    @Override
    public void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        MultiPlayerGameMode gameMode = client.gameMode;
        if (player == null || level == null || gameMode == null) {
            return;
        }

        ModConfig config = ConfigManager.getConfig();
        long now = System.currentTimeMillis();

        globalCooldowns.entrySet().removeIf(entry -> now > entry.getValue());

        if (now - lastBreedTime < config.autoBreedIntervalMs) {
            return;
        }

        double range = config.autoBreedRange;
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range);

        var nearbyAnimals = level.getEntitiesOfClass(Animal.class, searchBox,
                animal -> isReadyForBreeding(animal, config, player, true));

        for (Animal animal : nearbyAnimals) {
            if (animal.distanceToSqr(player) <= range * range
                    && player.isWithinEntityInteractionRange(animal, 0.5)) {
                InteractionHand feedHand = getFoodHand(animal, player);
                if (feedHand != null) {
                    ItemStack foodStack = (feedHand == InteractionHand.MAIN_HAND)
                            ? player.getMainHandItem()
                            : player.getOffhandItem();

                    Vec3 hitPos = animal.position().add(0, animal.getBbHeight() * 0.5, 0);
                    gameMode.interact(player, animal, new EntityHitResult(animal, hitPos), feedHand);
                    player.swing(feedHand, foodStack.getInteractAnimation(), true);

                    recordCooldown(animal.getId(), 20000L);
                    lastBreedTime = now;
                    break;
                }
            }
        }
    }

    public boolean isEligible(Animal animal, ModConfig config, Minecraft client, long now) {
        return isReadyForBreeding(animal, config, client.player, true);
    }

    public boolean isEligible(Animal animal, ModConfig config, @Nullable LocalPlayer player, long now) {
        return isReadyForBreeding(animal, config, player, true);
    }

    public static boolean isReadyForBreeding(Animal animal, ModConfig config, @Nullable LocalPlayer player,
            boolean requireFoodHeld) {
        if (animal.isBaby()) {
            return false;
        }

        if (isEntityCoolingDown(animal.getId())) {
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

        if (requireFoodHeld) {
            if (player == null) {
                return false;
            }
            return getFoodHand(animal, player) != null;
        }

        return true;
    }

    public static @Nullable InteractionHand getFoodHand(Animal animal, @Nullable LocalPlayer player) {
        if (player == null) {
            return null;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (isBreedingFood(animal, mainHand)) {
            return InteractionHand.MAIN_HAND;
        }
        if (isBreedingFood(animal, offHand)) {
            return InteractionHand.OFF_HAND;
        }

        return null;
    }

    public static boolean isBreedingFood(@Nullable Animal animal, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (animal != null) {
            return animal.isFood(stack);
        }

        return stack.getItem() == Items.WHEAT
                || stack.getItem() == Items.CARROT
                || stack.getItem() == Items.POTATO
                || stack.getItem() == Items.BEETROOT
                || stack.getItem() == Items.WHEAT_SEEDS
                || stack.getItem() == Items.BEETROOT_SEEDS
                || stack.getItem() == Items.MELON_SEEDS
                || stack.getItem() == Items.PUMPKIN_SEEDS;
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        globalCooldowns.clear();
    }
}
