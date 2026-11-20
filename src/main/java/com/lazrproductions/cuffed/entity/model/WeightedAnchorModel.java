package com.lazrproductions.cuffed.entity.model;


import net.minecraft.client.renderer.entity.state.EntityRenderState;
import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.entity.WeightedAnchorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class WeightedAnchorModel extends EntityModel<EntityRenderState> {
	private final ModelPart ball;

	public WeightedAnchorModel(ModelPart root) {
		super(root);
		this.ball = root.getChild("ball");
	}

	public static LayerDefinition getModelData() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("ball", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
			.texOffs(0, 0).addBox(-2.0F, -10.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0, 0, 180*Mth.DEG_TO_RAD));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}
}