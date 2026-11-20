package com.lazrproductions.cuffed.utils;

import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the chain between an anchored entity and its anchor, drawn like a leash with
 * alternating chain links. The geometry is computed while extracting the render state
 * and drawn later in the submit phase.
 */
public class ChainUtils {

    /** Everything needed to draw one chain, relative to the anchored entity's render origin. */
    public record ChainData(float offsetX, float offsetY, float offsetZ, float dx, float dy, float dz,
            int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight, float distance) {
    }

    @Nullable
    public static ChainData compute(Entity entityFrom, float partialTicks, @Nullable Entity entityTo) {
        if (entityTo == null)
            return null;

        float distance = Mth.clamp(entityFrom.distanceTo(entityTo) / 12.0F, 0, 1);

        Vec3 startPos = entityTo.getRopeHoldPosition(partialTicks);
        double d0 = (double) (entityFrom.yRotO * ((float) Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 endPosOffset = entityFrom instanceof Leashable leashable ? leashable.getLeashOffset(partialTicks)
                : new Vec3(0.0D, entityFrom.getEyeHeight(), entityFrom.getBbWidth() * 0.4F);

        double d1 = Math.cos(d0) * endPosOffset.z + Math.sin(d0) * endPosOffset.x;
        double d2 = Math.sin(d0) * endPosOffset.z - Math.cos(d0) * endPosOffset.x;

        double ePosX = Mth.lerp(partialTicks, entityFrom.xo, entityFrom.getX()) + d1;
        double ePosY = Mth.lerp(partialTicks, entityFrom.yo, entityFrom.getY()) + endPosOffset.y;
        double ePosZ = Mth.lerp(partialTicks, entityFrom.zo, entityFrom.getZ()) + d2;

        BlockPos mobEyePosition = BlockPos.containing(entityFrom.getEyePosition(partialTicks));
        BlockPos holderEyePos = BlockPos.containing(entityTo.getEyePosition(partialTicks));

        return new ChainData((float) d1, (float) endPosOffset.y, (float) d2,
                (float) (startPos.x - ePosX), (float) (startPos.y - ePosY), (float) (startPos.z - ePosZ),
                getBlockLightLevel(entityFrom, mobEyePosition), getBlockLightLevel(entityTo, holderEyePos),
                entityFrom.level().getBrightness(LightLayer.SKY, mobEyePosition),
                entityFrom.level().getBrightness(LightLayer.SKY, holderEyePos), distance);
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, ChainData data) {
        poseStack.pushPose();
        poseStack.translate(data.offsetX(), data.offsetY(), data.offsetZ());
        float horizontalLength = Mth.sqrt(data.dx() * data.dx() + data.dz() * data.dz());
        float xWidth = horizontalLength > 0.0001F ? data.dz() / horizontalLength * 0.025F : 0.025F;
        float zWidth = horizontalLength > 0.0001F ? -data.dx() / horizontalLength * 0.025F : 0;
        collector.submitCustomGeometry(poseStack, RenderTypes.leash(), (pose, buffer) -> {
            for (int i = 0; i < 24; ++i) {
                boolean horizontalLink = i % 2 == 0 || horizontalLength <= 0.0001F;
                addChainLink(buffer, pose, data, i, horizontalLink ? xWidth : 0,
                        horizontalLink ? 0 : 0.025F, horizontalLink ? zWidth : 0);
            }
        });
        poseStack.popPose();
    }

    protected static int getBlockLightLevel(Entity mob, BlockPos pos) {
        return mob.isOnFire() ? 15 : mob.level().getBrightness(LightLayer.BLOCK, pos);
    }

    private static void addChainLink(VertexConsumer buffer, PoseStack.Pose pose, ChainData data,
            int index, float xWidth, float yWidth, float zWidth) {
        float start = index / 24.0F;
        float end = (index + 1) / 24.0F;
        int blockLight = (int) Mth.lerp(start, data.startBlockLight(), data.endBlockLight());
        int skyLight = (int) Mth.lerp(start, data.startSkyLight(), data.endSkyLight());
        int light = LightCoordsUtil.pack(blockLight, skyLight);

        int shade = Math.round(new Random(index * 1000L).nextFloat() * 2.49F);
        int color = switch (shade) {
            case 0 -> 0xFF495065;
            case 1 -> 0xFF3E4453;
            default -> 0xFF252C3D;
        };

        // Duplicate the first and last vertices so the triangle strip cannot bridge adjacent links.
        addVertex(buffer, pose, data, start, -xWidth, -yWidth, -zWidth, color, light);
        addVertex(buffer, pose, data, start, -xWidth, -yWidth, -zWidth, color, light);
        addVertex(buffer, pose, data, start, xWidth, yWidth, zWidth, color, light);
        addVertex(buffer, pose, data, end, -xWidth, -yWidth, -zWidth, color, light);
        addVertex(buffer, pose, data, end, xWidth, yWidth, zWidth, color, light);
        addVertex(buffer, pose, data, end, xWidth, yWidth, zWidth, color, light);
    }

    private static void addVertex(VertexConsumer buffer, PoseStack.Pose pose, ChainData data,
            float progress, float xOffset, float yOffset, float zOffset, int color, int light) {
        float sag = (float) Math.sin(progress * Math.PI) * 0.6F * (1 - data.distance());
        buffer.addVertex(pose, data.dx() * progress + xOffset, data.dy() * progress - sag + yOffset,
                data.dz() * progress + zOffset).setColor(color).setLight(light);
    }
}
