package com.lazrproductions.cuffed.client;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.entity.base.IDetainableEntity;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Restraint information copied from a player onto its render state during extraction,
 * since 26.x models and render layers only see render states, not entities.
 */
public record CuffedRenderData(Identifier headRestraint, Identifier armRestraint, Identifier legRestraint,
        boolean headEnchanted, boolean armsEnchanted, boolean legsEnchanted, int detained, float detainedRotation) {

    public static final RenderStateDataKey<CuffedRenderData> KEY = RenderStateDataKey.create(() -> "cuffed:restraints");

    public static void extract(Entity entity, EntityRenderState state) {
        if (entity instanceof IRestrainableEntity r && entity instanceof IDetainableEntity d) {
            state.setData(KEY, new CuffedRenderData(r.getHeadRestraintId(), r.getArmRestraintId(),
                    r.getLegRestraintId(), r.getHeadIsEnchanted(), r.getArmsAreEnchanted(), r.getLegsAreEnchanted(),
                    d.getDetained(), d.getDetainedRotation()));
        } else {
            state.setData(KEY, null);
        }
    }

    @Nullable
    public static CuffedRenderData get(EntityRenderState state) {
        return state.getData(KEY);
    }
}
