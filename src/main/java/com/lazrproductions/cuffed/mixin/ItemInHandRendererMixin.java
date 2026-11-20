package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.entity.base.IDetainableEntity;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.restraints.custom.PilloryRestraint;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void cuffed$submitArmWithItem(AbstractClientPlayer player, float frameInterp, float xRot,
            InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo callback) {
        if (player.isLocalPlayer()) {
            if (player instanceof IDetainableEntity det && det.getDetained() > -1) {
                player.setYBodyRot(det.getDetainedRotation());
                player.setYHeadRot(det.getDetainedRotation());
                player.setYRot(det.getDetainedRotation());
                player.setXRot(45);
                boolean isMainHand = hand == InteractionHand.MAIN_HAND;
                HumanoidArm humanoidarm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
                poseStack.pushPose();
                this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attack, humanoidarm);
                poseStack.popPose();
                callback.cancel();
                return;
            }
            if (player instanceof IRestrainableEntity res && PilloryRestraint.ID.equals(res.getHeadRestraintId())) {
                player.setXRot(10);
            }
        }
    }

    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);
}
