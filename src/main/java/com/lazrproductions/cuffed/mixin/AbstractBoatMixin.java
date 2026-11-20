package com.lazrproductions.cuffed.mixin;

import com.lazrproductions.cuffed.api.CuffedAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public class AbstractBoatMixin {
    @Inject(method = "getControllingPassenger", at = @At("RETURN"), cancellable = true)
    private void cuffed$restrictSteering(CallbackInfoReturnable<LivingEntity> callback) {
        if (callback.getReturnValue() instanceof Player player) {
            var cap = CuffedAPI.Capabilities.getRestrainableCapability(player);
            if (cap.armsRestrained() || cap.legsRestrained())
                callback.setReturnValue(null);
        }
    }
}
