package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.event.ModClientEvents;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.restraints.base.RestraintType;
import com.lazrproductions.cuffed.restraints.custom.BundleRestraint;
import net.minecraft.world.phys.HitResult;

import net.minecraft.client.Minecraft;

/** Replacement for Forge's InteractionKeyMappingTriggered event. */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void cuffed$startAttack(CallbackInfoReturnable<Boolean> callback) {
        if (ModClientEvents.shouldBlockInteraction(true))
            callback.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void cuffed$continueAttack(boolean down, CallbackInfo callback) {
        if (down && ModClientEvents.shouldBlockInteraction(true))
            callback.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void cuffed$startUseItem(CallbackInfo callback) {
        Minecraft minecraft = (Minecraft) (Object) this;
        var player = minecraft.player;
        if (player != null && minecraft.options.keyShift.isDown() && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty()
                && !CuffedAPI.Capabilities.getRestrainableCapability(player).armsRestrained()
                && (minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS)
                && BundleRestraint.ID.equals(((IRestrainableEntity) player).getHeadRestraintId())) {
            CuffedAPI.Networking.sendRestraintUtilityPacketToServer(
                    RestraintType.Head, BundleRestraint.REMOVE_SELF, 0, false, 0, "");
            callback.cancel();
            return;
        }
        if (ModClientEvents.shouldBlockInteraction(false))
            callback.cancel();
    }
}
