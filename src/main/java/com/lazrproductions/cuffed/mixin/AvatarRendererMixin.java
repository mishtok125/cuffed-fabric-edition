package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.client.CuffedRenderData;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void cuffed$extractRenderState(Avatar entity, AvatarRenderState state, float partialTicks,
            CallbackInfo callback) {
        CuffedRenderData.extract(entity, state);
        CuffedRenderData data = CuffedRenderData.get(state);
        if (data != null && data.detained() == 0) {
            // Detained in a pillory: face the pillory's direction, head locked forward.
            state.bodyRot = data.detainedRotation();
            state.yRot = 0;
        }
    }
}
