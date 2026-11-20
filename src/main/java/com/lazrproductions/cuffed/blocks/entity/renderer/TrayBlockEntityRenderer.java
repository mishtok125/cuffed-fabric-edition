package com.lazrproductions.cuffed.blocks.entity.renderer;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.blocks.TrayBlock;
import com.lazrproductions.cuffed.blocks.entity.TrayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Renders the food and cutlery lying on a placed tray. */
public class TrayBlockEntityRenderer implements BlockEntityRenderer<TrayBlockEntity, TrayBlockEntityRenderer.State> {
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public final ItemStackRenderState food = new ItemStackRenderState();
        public final ItemStackRenderState fork = new ItemStackRenderState();
        public final ItemStackRenderState spoon = new ItemStackRenderState();
        public final ItemStackRenderState knife = new ItemStackRenderState();
    }

    public TrayBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TrayBlockEntity entity, State state, float partialTicks, Vec3 cameraPosition,
            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(TrayBlock.FACING);
        update(state.food, entity.getFoodStack(), entity);
        update(state.fork, entity.getForkStack(), entity);
        update(state.spoon, entity.getSpoonStack(), entity);
        update(state.knife, entity.getKnifeStack(), entity);
    }

    private void update(ItemStackRenderState out, ItemStack stack, TrayBlockEntity entity) {
        itemModelResolver.updateForTopItem(out, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, 1);
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        Direction facing = state.facing;
        if (!state.food.isEmpty()) {
            stack.pushPose();
            switch (facing) {
                case NORTH -> stack.translate(0.66f, 0.05f, 0.52f);
                case SOUTH -> stack.translate(0.34f, 0.05f, 0.48f);
                case WEST -> stack.translate(0.52f, 0.05f, 0.34f);
                default -> stack.translate(0.48f, 0.05f, 0.66f);
            }
            stack.scale(0.35f, 0.35f, 0.35f);
            stack.mulPose(Axis.YN.rotationDegrees(facing.getOpposite().toYRot()));
            stack.mulPose(Axis.XP.rotationDegrees(90));
            state.food.submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            stack.popPose();
        }

        submitCutlery(state.fork, state, stack, collector, new float[] { 0.44f, 0.56f, 0.56f, 0.44f });
        submitCutlery(state.spoon, state, stack, collector, new float[] { 0.365f, 0.635f, 0.635f, 0.365f });
        submitCutlery(state.knife, state, stack, collector, new float[] { 0.27f, 0.73f, 0.73f, 0.27f });
    }

    /** offsets: x for north, x for south, z for west, z for east. */
    private void submitCutlery(ItemStackRenderState item, State state, PoseStack stack, SubmitNodeCollector collector,
            float[] offsets) {
        if (item.isEmpty())
            return;
        Direction facing = state.facing;
        stack.pushPose();
        switch (facing) {
            case NORTH -> stack.translate(offsets[0], 0.05f, 0.52f);
            case SOUTH -> stack.translate(offsets[1], 0.05f, 0.48f);
            case WEST -> stack.translate(0.52f, 0.05f, offsets[2]);
            default -> stack.translate(0.48f, 0.05f, offsets[3]);
        }
        stack.scale(0.28f, 0.28f, 0.28f);
        stack.mulPose(Axis.YN.rotationDegrees(facing.getOpposite().toYRot() - 45));
        stack.mulPose(Axis.XP.rotationDegrees(90));
        item.submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        stack.popPose();
    }
}
