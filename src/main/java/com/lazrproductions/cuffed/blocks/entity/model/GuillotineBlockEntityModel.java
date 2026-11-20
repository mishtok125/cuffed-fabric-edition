package com.lazrproductions.cuffed.blocks.entity.model;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.blocks.GuillotineBlock;
import com.lazrproductions.cuffed.blocks.entity.GuillotineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.lazrproductions.cuffed.blocks.entity.renderer.GuillotineBlockEntityRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class GuillotineBlockEntityModel extends net.minecraft.client.model.Model<GuillotineBlockEntityRenderer.State> {
	private final ModelPart guillotine;
	//private final ModelPart blade;
	//private final ModelPart dry_blade;
	//private final ModelPart bloody_blade;

	public GuillotineBlockEntityModel(ModelPart root) {
		super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
		this.guillotine = root.getChild("guillotine");
		//this.blade = root.getChild("blade");
		//this.dry_blade = root.getChild("dry_blade");
		//this.bloody_blade = root.getChild("bloody_blade");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition guillotine = partdefinition.addOrReplaceChild("guillotine", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition blade = guillotine.addOrReplaceChild("blade", CubeListBuilder.create().texOffs(26, 25).addBox(-5.0F, -2.0F, -3.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 25).addBox(3.0F, -2.0F, -3.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 7).addBox(-6.0F, -4.0F, -3.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 7).mirror().addBox(6.0F, -4.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 7).addBox(-10.0F, -4.0F, -3.0F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		blade.addOrReplaceChild("dry_blade", CubeListBuilder.create().texOffs(0, 21).addBox(-6.0F, 4.0F, -3.0F, 12.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		blade.addOrReplaceChild("bloody_blade", CubeListBuilder.create().texOffs(0, 10).addBox(-6.0F, 4.0F, -3.0F, 12.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		blade.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(GuillotineBlockEntityRenderer.State state) {
		super.setupAnim(state);
		guillotine.setRotation(0, (state.yRot - 180) * Mth.DEG_TO_RAD, Mth.PI);
		guillotine.setPos(8, 8 - state.dropOffset, 8);
		guillotine.getChild("blade").getChild("dry_blade").visible = !state.bloody;
		guillotine.getChild("blade").getChild("bloody_blade").visible = state.bloody;
	}
}
