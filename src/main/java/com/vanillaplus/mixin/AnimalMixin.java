package com.vanillaplus.mixin;

import com.vanillaplus.modules.automation.AutoBreedModule;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin {

    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void vanillaplus$onHandleEntityEvent(byte eventId, CallbackInfo ci) {
        if (eventId == 18) {
            Animal animal = (Animal) (Object) this;
            AutoBreedModule.onAnimalLoveEvent(animal);
        }
    }
}
