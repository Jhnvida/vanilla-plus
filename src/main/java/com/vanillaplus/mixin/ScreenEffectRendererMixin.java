package com.vanillaplus.mixin;

import com.vanillaplus.modules.movement.FreecamModule;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void onSubmit(float f, SubmitNodeCollector submitNodeCollector, PlayerRenderState playerRenderState,
            CameraRenderState cameraRenderState, boolean bl, CallbackInfo ci) {
        FreecamModule freecam = FreecamModule.getInstance();
        if (freecam != null && freecam.isEnabled()) {
            ci.cancel();
        }
    }
}
