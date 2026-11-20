package com.lazrproductions.cuffed.entity.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

/**
 * Restraint poses, applied on top of the vanilla humanoid animation (see HumanoidModelMixin).
 * Angles are the same as in the Forge version.
 */
public class HumanoidAnimationHelper {

    public static void animateArmsTiedBack(HumanoidRenderState state, HumanoidModel<?> model) {
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;
        if (!state.isCrouching) {
            rightArm.yRot = 40 * Mth.DEG_TO_RAD;
            rightArm.xRot = 40 * Mth.DEG_TO_RAD;
            leftArm.yRot = -40 * Mth.DEG_TO_RAD;
            leftArm.xRot = 40 * Mth.DEG_TO_RAD;
        } else {
            rightArm.yRot = 20 * Mth.DEG_TO_RAD;
            rightArm.xRot = 73 * Mth.DEG_TO_RAD;
            leftArm.yRot = -20 * Mth.DEG_TO_RAD;
            leftArm.xRot = 73 * Mth.DEG_TO_RAD;
        }
        rightArm.zRot = 0;
        leftArm.zRot = 0;
    }

    public static void animateArmsTiedFront(HumanoidRenderState state, HumanoidModel<?> model) {
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;
        if (!state.isCrouching) {
            rightArm.yRot = -30 * Mth.DEG_TO_RAD;
            rightArm.xRot = -40 * Mth.DEG_TO_RAD;
            leftArm.yRot = 30 * Mth.DEG_TO_RAD;
            leftArm.xRot = -40 * Mth.DEG_TO_RAD;
        } else {
            rightArm.yRot = -90 * Mth.DEG_TO_RAD;
            rightArm.xRot = -17 * Mth.DEG_TO_RAD;
            leftArm.yRot = 90 * Mth.DEG_TO_RAD;
            leftArm.xRot = -17 * Mth.DEG_TO_RAD;
        }
        rightArm.zRot = 0;
        leftArm.zRot = 0;
    }

    public static void animatePilloryDetainedAnimation(HumanoidRenderState state, HumanoidModel<?> model) {
        model.resetPose();
        ModelPart head = model.head;
        ModelPart body = model.body;
        ModelPart leftLeg = model.leftLeg;
        ModelPart rightLeg = model.rightLeg;
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;

        rightLeg.z = -2.0F;
        rightLeg.y = 14.2F;
        rightLeg.xRot = 10 * Mth.DEG_TO_RAD;
        rightLeg.yRot = 5 * Mth.DEG_TO_RAD;
        rightLeg.zRot = 10 * Mth.DEG_TO_RAD;

        leftLeg.y = 16.2F;
        leftLeg.xRot = 40 * Mth.DEG_TO_RAD;
        leftLeg.yRot = 5 * Mth.DEG_TO_RAD;
        leftLeg.zRot = -10 * Mth.DEG_TO_RAD;

        head.z = -7F;
        head.y = 9.6F;
        head.xRot = 40F * Mth.DEG_TO_RAD;
        head.yRot = 0;

        body.y = 7.5F;
        body.z = -5.2F;
        body.xRot = 50 * Mth.DEG_TO_RAD;

        leftArm.y = 8.3F;
        leftArm.z = -3F;
        leftArm.xRot = -70 * Mth.DEG_TO_RAD;
        leftArm.zRot = -10 * Mth.DEG_TO_RAD;

        rightArm.y = 8.3F;
        rightArm.z = -3F;
        rightArm.xRot = -70 * Mth.DEG_TO_RAD;
        rightArm.zRot = 10 * Mth.DEG_TO_RAD;
    }
}
