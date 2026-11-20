package com.lazrproductions.cuffed.entity.renderer;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.PadlockEntity;
import com.lazrproductions.cuffed.entity.model.PadlockEntityModel;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class PadlockEntityRenderer extends EntityRenderer<PadlockEntity, PadlockEntityRenderer.State> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/padlock.png");
    private static final Identifier TEXTURE_REINFORCED_LOCATION = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/entity/reinforced_padlock.png");
    private final PadlockEntityModel model;

    public static class State extends EntityRenderState {
        public float yRot;
        public boolean reinforced;
    }

    public PadlockEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PadlockEntityModel(context.bakeLayer(ModModelLayers.PADLOCK_LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PadlockEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot();
        state.reinforced = entity.isReinforced();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        // The model is already upright (its part is rotated 180 degrees) but is built 24px above its origin
        // in Blockbench entity space, so without this shift the lock floats ~1.5 blocks above the door.
        poseStack.translate(0.0F, -1.5F, 0.0F);
        Identifier texture = state.reinforced ? TEXTURE_REINFORCED_LOCATION : TEXTURE_LOCATION;
        collector.submitModel(this.model, state, poseStack, this.model.renderType(texture), state.lightCoords,
                OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
