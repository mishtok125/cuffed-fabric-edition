package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.entity.base.IAnchorableEntity;
import com.lazrproductions.cuffed.utils.ChainUtils;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;

/** Draws the chain from anchored entities to their anchor. */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Unique
    private static final RenderStateDataKey<ChainUtils.ChainData> CUFFED_CHAIN = RenderStateDataKey
            .create(() -> "cuffed:chain");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void cuffed$extractRenderState(T entity, S state, float partialTicks, CallbackInfo callback) {
        if (entity instanceof IAnchorableEntity anchorable && anchorable.getAnchorClientSide() != null)
            state.setData(CUFFED_CHAIN, ChainUtils.compute(entity, partialTicks, anchorable.getAnchorClientSide()));
        else
            state.setData(CUFFED_CHAIN, null);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void cuffed$submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera,
            CallbackInfo callback) {
        ChainUtils.ChainData data = state.getData(CUFFED_CHAIN);
        if (data != null)
            ChainUtils.submit(poseStack, collector, data);
    }
}
