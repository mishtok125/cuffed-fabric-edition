package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.client.CuffedRenderData;
import com.lazrproductions.cuffed.entity.animation.ArmRestraintAnimationFlags;
import com.lazrproductions.cuffed.entity.animation.HumanoidAnimationHelper;
import com.lazrproductions.cuffed.restraints.RestraintAPI;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Applies restraint poses after the vanilla humanoid animation. Restraint models are HumanoidModels
 * too, so they receive the same pose and stay attached to the player's limbs.
 */
@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends HumanoidRenderState> {
    @SuppressWarnings("unchecked")
    @Inject(at = @At("TAIL"), method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V")
    private void cuffed$setupAnim(T state, CallbackInfo callback) {
        CuffedRenderData data = CuffedRenderData.get(state);
        if (data == null)
            return;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        if (data.detained() == 0) {
            HumanoidAnimationHelper.animatePilloryDetainedAnimation(state, model);
            return;
        }

        ArmRestraintAnimationFlags armAnimationFlags = RestraintAPI.getArmAnimationFlagByKey(data.armRestraint());
        if (armAnimationFlags == null)
            return;
        switch (armAnimationFlags) {
            case ARMS_TIED_FRONT:
                HumanoidAnimationHelper.animateArmsTiedFront(state, model);
                break;
            case ARMS_TIED_BEHIND:
                HumanoidAnimationHelper.animateArmsTiedBack(state, model);
                break;
            default:
                break;
        }
    }
}
