package com.lazrproductions.cuffed.entity.renderer;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.ChainKnotEntity;
import com.lazrproductions.cuffed.entity.model.ChainKnotEntityModel;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class ChainKnotEntityRenderer extends EntityRenderer<ChainKnotEntity, ChainKnotEntityRenderer.State> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/chain_knot.png");
    private final ChainKnotEntityModel model;

    public static class State extends EntityRenderState {
        public boolean onFence;
    }

    public ChainKnotEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ChainKnotEntityModel(context.bakeLayer(ModModelLayers.CHAIN_KNOT_LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ChainKnotEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // ChainKnotEntity#isOnFence is inverted (true when NOT on a fence), same as in the original.
        state.onFence = !entity.isOnFence();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitModel(this.model, state, poseStack, this.model.renderType(TEXTURE_LOCATION),
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        super.submit(state, poseStack, collector, camera);
    }
}
