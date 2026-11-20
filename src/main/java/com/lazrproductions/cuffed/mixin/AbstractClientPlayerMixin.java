package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.event.ModClientEvents;

import net.minecraft.client.player.AbstractClientPlayer;

/** Replacement for Forge's ComputeFovModifierEvent: no FOV change while movement is restrained. */
@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void cuffed$getFieldOfViewModifier(boolean firstPerson, float effectScale,
            CallbackInfoReturnable<Float> callback) {
        if (ModClientEvents.shouldLockFov((AbstractClientPlayer) (Object) this))
            callback.setReturnValue(1.0F);
    }
}
