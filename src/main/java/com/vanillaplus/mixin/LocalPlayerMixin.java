package com.vanillaplus.mixin;

import com.vanillaplus.modules.movement.FreecamModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void onIsControlledCamera(CallbackInfoReturnable<Boolean> cir) {
        FreecamModule freecam = FreecamModule.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "applyInput", at = @At("HEAD"), cancellable = true)
    private void onApplyInput(CallbackInfo ci) {
        FreecamModule freecam = FreecamModule.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            LocalPlayer self = (LocalPlayer) (Object) this;
            self.xxa = 0.0f;
            self.zza = 0.0f;
            self.setDeltaMovement(new Vec3(0, self.getDeltaMovement().y, 0));
            ci.cancel();
        }
    }

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void onSendPosition(CallbackInfo ci) {
        FreecamModule freecam = FreecamModule.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            ci.cancel();
        }
    }
}
