package com.vanillaplus.mixin;

import com.vanillaplus.modules.render.FullbrightModule;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {

    @Shadow
    private boolean needsUpdate;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        FullbrightModule fullbright = FullbrightModule.getInstance();
        if (fullbright != null && fullbright.isEnabled()) {
            this.needsUpdate = true;
        }
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void onExtract(LightmapRenderState state, float partialTicks, CallbackInfo ci) {
        FullbrightModule fullbright = FullbrightModule.getInstance();
        if (fullbright != null && fullbright.isEnabled()) {
            state.needsUpdate = true;
            state.nightVisionEffectIntensity = 1.0f;
            state.brightness = 1.0f;
        }
    }
}
