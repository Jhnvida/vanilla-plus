package com.vanillaplus.mixin;

import com.vanillaplus.modules.movement.FreecamModule;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void onRenderItemInHand(CallbackInfo ci) {
        FreecamModule freecam = FreecamModule.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            ci.cancel();
        }
    }
}
