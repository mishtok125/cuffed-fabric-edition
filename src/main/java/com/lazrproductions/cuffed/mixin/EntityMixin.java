package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.api.CuffedAPI;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public class EntityMixin {
    // 26.2: isControlledByLocalInstance() was replaced by isLocalInstanceAuthoritative(), whose
    // client-side branch delegates to isLocalClientAuthoritative() (not overridden by vehicles).
    @Inject(method = "isLocalClientAuthoritative", at = @At("HEAD"), cancellable = true)
    public void cuffed$isLocalClientAuthoritative(CallbackInfoReturnable<Boolean> callback) {
        Entity thisEntity = (Entity) (Object) this;
        LivingEntity livingentity = thisEntity.getControllingPassenger();
        if (livingentity instanceof Player player)
            if (CuffedAPI.Capabilities.getRestrainableCapability(player).restraintsDisabledMovement())
                callback.setReturnValue(false);
    }
}
