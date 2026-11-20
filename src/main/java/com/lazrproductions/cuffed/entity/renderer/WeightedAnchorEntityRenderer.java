package com.lazrproductions.cuffed.entity.renderer;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.WeightedAnchorEntity;
import com.lazrproductions.cuffed.entity.model.WeightedAnchorModel;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class WeightedAnchorEntityRenderer extends EntityRenderer<WeightedAnchorEntity, WeightedAnchorEntityRenderer.State> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/weighted_anchor.png");
    private final WeightedAnchorModel model;

    public static class State extends EntityRenderState {
        public float yRot;
        public boolean enchanted;
    }

    public WeightedAnchorEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new WeightedAnchorModel(context.bakeLayer(ModModelLayers.WEIGHTED_ANCHOR_LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WeightedAnchorEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot();
        state.enchanted = entity.getIsEnchanted();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        collector.submitModel(this.model, state, poseStack, this.model.renderType(TEXTURE_LOCATION), state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        if (state.enchanted)
            collector.submitModel(this.model, state, poseStack, RenderTypes.entityGlint(), state.lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
