package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.restraints.RestraintAPI;
import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(at = @At("RETURN"), method = "canStartSprinting", cancellable = true)
    private void cuffed$canStartSprinting(CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof IRestrainableEntity entity) {
            // The original checked the leg slot three times; all three slots are checked here.
            for (Identifier key : new Identifier[] { entity.getHeadRestraintId(), entity.getArmRestraintId(),
                    entity.getLegRestraintId() }) {
                if (key == null)
                    continue;
                AbstractRestraint restraint = RestraintAPI.Registries.get(key);
                if (restraint != null && !restraint.AllowSprinting()) {
                    callback.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
