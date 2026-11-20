package com.lazrproductions.cuffed.restraints.client.layer;

import java.util.HashMap;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.client.CuffedRenderData;
import com.lazrproductions.cuffed.restraints.RestraintAPI;
import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;
import com.lazrproductions.cuffed.restraints.client.RestraintModelInterface;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Renders the models of all restraints a player wears. */
public class RestraintEntityLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final HashMap<String, HumanoidModel<AvatarRenderState>> restraintModels = new HashMap<>();
    private final HashMap<String, RestraintModelInterface> restraintModelInterfaces = new HashMap<>();

    @SuppressWarnings("unchecked")
    public RestraintEntityLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent,
            EntityRendererProvider.Context context) {
        super(parent);
        for (AbstractRestraint res : RestraintAPI.Registries.getAllRestraints()) {
            try {
                RestraintModelInterface in = res.getModelInterface();
                if (in != null) {
                    if (in.getRenderedModel() != null)
                        restraintModels.put(res.getId().toString(), (HumanoidModel<AvatarRenderState>) in
                                .getRenderedModel().getConstructor(ModelPart.class)
                                .newInstance(context.bakeLayer(in.getRenderedModelLayer())));
                    restraintModelInterfaces.put(res.getId().toString(), in);
                }
            } catch (Exception e) {
                CuffedMod.LOGGER.error("The restraint " + res.getId() + " uses a model that is not valid!", e);
            }
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state,
            float yRot, float xRot) {
        CuffedRenderData data = CuffedRenderData.get(state);
        if (data == null || state.isInvisible)
            return;
        submitRestraint(poseStack, collector, lightCoords, state, data.headRestraint(), data.headEnchanted());
        submitRestraint(poseStack, collector, lightCoords, state, data.armRestraint(), data.armsEnchanted());
        submitRestraint(poseStack, collector, lightCoords, state, data.legRestraint(), data.legsEnchanted());
    }

    private void submitRestraint(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
            AvatarRenderState state, Identifier id, boolean enchanted) {
        if (id == null)
            return;
        String key = id.toString();
        HumanoidModel<AvatarRenderState> model = restraintModels.get(key);
        RestraintModelInterface in = restraintModelInterfaces.get(key);
        if (model == null || in == null)
            return;

        model.setupAnim(state);
        model.body.visible = true;
        collector.submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(in.getRenderedModelTexture()),
                lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        if (enchanted)
            collector.submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(), lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
