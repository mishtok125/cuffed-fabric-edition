package com.lazrproductions.cuffed.entity.renderer;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.CrumblingBlockEntity;
import com.lazrproductions.cuffed.entity.model.CrumblingBlockModel;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class CrumblingBlockRenderer extends EntityRenderer<CrumblingBlockEntity, CrumblingBlockRenderer.State> {
    public static Identifier TEXTURE_LOCATION_1 = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/crumbling_block_1.png");
    public static Identifier TEXTURE_LOCATION_2 = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/crumbling_block_2.png");
    public static Identifier TEXTURE_LOCATION_3 = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/crumbling_block_3.png");
    public static Identifier TEXTURE_LOCATION_4 = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/crumbling_block_4.png");

    public final CrumblingBlockModel model;

    public static class State extends EntityRenderState {
        public int progress;
    }

    public CrumblingBlockRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        model = new CrumblingBlockModel(ctx.bakeLayer(ModModelLayers.CRUMBLING_BLOCK_LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrumblingBlockEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.progress = entity.getCrumbleProgress();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(getTexture(state.progress)), 15,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        super.submit(state, poseStack, collector, camera);
    }

    public static Identifier getTexture(int progress) {
        switch (progress) {
            case 2:
                return TEXTURE_LOCATION_2;
            case 3:
                return TEXTURE_LOCATION_3;
            case 4:
            case 5:
                return TEXTURE_LOCATION_4;
            default:
                return TEXTURE_LOCATION_1;
        }
    }
}
