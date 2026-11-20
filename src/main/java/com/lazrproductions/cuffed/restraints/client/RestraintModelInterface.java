package com.lazrproductions.cuffed.restraints.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public abstract class RestraintModelInterface {
    public abstract Class<? extends HumanoidModel<?>> getRenderedModel();
    public abstract ModelLayerLocation getRenderedModelLayer();
    public abstract Identifier getRenderedModelTexture();
}
