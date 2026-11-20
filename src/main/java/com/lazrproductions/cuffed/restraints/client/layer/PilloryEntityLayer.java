package com.lazrproductions.cuffed.restraints.client.layer;

import com.lazrproductions.cuffed.client.CuffedRenderData;
import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.restraints.custom.PilloryRestraint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the pillory around the head of a player wearing the pillory restraint. */
public class PilloryEntityLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    public PilloryEntityLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state,
            float yRot, float xRot) {
        CuffedRenderData data = CuffedRenderData.get(state);
        if (data == null || !PilloryRestraint.ID.equals(data.headRestraint()))
            return;

        ItemStack itemStack = new ItemStack(ModItems.PILLORY_ITEM);
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState, itemStack,
                ItemDisplayContext.HEAD, null, null, 0);

        poseStack.pushPose();
        translateToHead(poseStack);
        if (state.isCrouching)
            poseStack.translate(0, -0.5d, 0);
        itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    public static void translateToHead(PoseStack poseStack) {
        poseStack.translate(0.0F, -0.25F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.625F, -0.625F, -0.625F);
    }
}
