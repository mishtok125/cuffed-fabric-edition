package com.lazrproductions.cuffed.blocks.entity.renderer;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.blocks.GuillotineBlock;
import com.lazrproductions.cuffed.blocks.entity.GuillotineBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.model.GuillotineBlockEntityModel;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class GuillotineBlockEntityRenderer
        implements BlockEntityRenderer<GuillotineBlockEntity, GuillotineBlockEntityRenderer.State> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/block/guillotine.png");

    private final GuillotineBlockEntityModel model;

    public static class State extends BlockEntityRenderState {
        public float yRot;
        public float dropOffset;
        public boolean bloody;
    }

    public GuillotineBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new GuillotineBlockEntityModel(ctx.bakeLayer(ModModelLayers.GUILLOTINE_LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GuillotineBlockEntity entity, State state, float partialTick, Vec3 cameraPosition,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, cameraPosition, breakProgress);
        state.yRot = entity.getBlockState().getValue(GuillotineBlock.FACING).toYRot();
        state.bloody = entity.isBloody;

        // Blade drop animation (kept per block entity; the original stored it in the shared model).
        float progress = entity.clientDropProgress;
        if (entity.isDown)
            progress += (float) Math.pow(progress + 0.08d, 2f) / 1.32f;
        else
            progress = Mth.lerp(partialTick * 0.1f, progress, 0);
        progress = Mth.clamp(progress, 0, 1);
        entity.clientDropProgress = progress;
        state.dropOffset = progress * 12;
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitModel(model, state, stack, RenderTypes.entityCutout(TEXTURE_LOCATION), state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
    }
}
