package com.vanillaplus.mixin;

import com.vanillaplus.modules.movement.FreecamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onTurn(double yRot, double xRot, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if ((Object) this == client.player) {
            FreecamModule freecam = FreecamModule.getInstance();
            if (freecam != null && freecam.isEnabled()) {
                freecam.addCameraRotation((float) yRot, (float) xRot);
                ci.cancel();
            }
        }
    }
}
