package com.lazrproductions.cuffed.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RestrainedEffect extends MobEffect {
    public RestrainedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, amplifier);
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (RestrainedEffectInstance.decodeNoMining(amplifier) && attackSpeed != null) {
            attackSpeed.removeModifier(RestrainedEffectInstance.NEGATIVE_MINE_SPEED_ID);
            attackSpeed.addTransientModifier(new AttributeModifier(RestrainedEffectInstance.NEGATIVE_MINE_SPEED_ID,
                    -9.0, Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (RestrainedEffectInstance.decodeNoMovement(amplifier) && moveSpeed != null) {
            moveSpeed.removeModifier(RestrainedEffectInstance.NEGATIVE_MOVE_SPEED_ID);
            moveSpeed.addTransientModifier(new AttributeModifier(RestrainedEffectInstance.NEGATIVE_MOVE_SPEED_ID,
                    -9.0, Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (attackSpeed != null)
            attackSpeed.removeModifier(RestrainedEffectInstance.NEGATIVE_MINE_SPEED_ID);
        if (moveSpeed != null)
            moveSpeed.removeModifier(RestrainedEffectInstance.NEGATIVE_MOVE_SPEED_ID);
    }
}
