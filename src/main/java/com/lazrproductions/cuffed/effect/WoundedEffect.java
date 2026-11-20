package com.lazrproductions.cuffed.effect;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.init.ModEffects;
import com.lazrproductions.cuffed.init.ModParticleTypes;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WoundedEffect extends MobEffect {
    static final Identifier ATTRIBUTE_HEALTH_ID = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "wounded_health_reduction");
    static final Identifier ATTRIBUTE_SLOWNESS_ID = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "wounded_speed_reduction");

    public WoundedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity.getRandom().nextFloat() < ((double) amplifier / 100D)) {
            double x = entity.position().x() + (entity.getRandom().nextFloat() * 0.8D) - 0.4D;
            double y = entity.position().y() + 1 + (entity.getRandom().nextFloat() * 0.8D) - 0.4D;
            double z = entity.position().z() + (entity.getRandom().nextFloat() * 0.8D) - 0.4D;
            level.sendParticles(ModParticleTypes.BLOOD_DRIP_FALL_PARTICLE, x, y, z, 1, 0, 0, 0, 0);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        AttributeInstance health = attributes.getInstance(Attributes.MAX_HEALTH);
        AttributeInstance slowness = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (health != null)
            health.removeModifier(ATTRIBUTE_HEALTH_ID);
        if (slowness != null)
            slowness.removeModifier(ATTRIBUTE_SLOWNESS_ID);
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, amplifier);
        double reductionAmount = -(double) amplifier / 100D;
        AttributeInstance health = attributes.getInstance(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(ATTRIBUTE_HEALTH_ID);
            health.addTransientModifier(new AttributeModifier(ATTRIBUTE_HEALTH_ID, reductionAmount, Operation.ADD_MULTIPLIED_BASE));
        }
        AttributeInstance slowness = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (slowness != null) {
            slowness.removeModifier(ATTRIBUTE_SLOWNESS_ID);
            slowness.addTransientModifier(new AttributeModifier(ATTRIBUTE_SLOWNESS_ID, reductionAmount * 0.8D, Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void woundEntity(@Nonnull LivingEntity entity, int percentage, boolean overwrite) {
        MobEffectInstance inst = entity.getEffect(ModEffects.WOUNDED_EFFECT);
        int amplifier = percentage;
        if (inst != null && !overwrite) {
            int oldPercentage = inst.getAmplifier();
            amplifier = Mth.clamp(oldPercentage + percentage, 0, 100);
        }
        MobEffectInstance newInst = new MobEffectInstance(ModEffects.WOUNDED_EFFECT, -1, amplifier);
        entity.removeEffect(ModEffects.WOUNDED_EFFECT);
        entity.addEffect(newInst);
        if (entity.level() instanceof ServerLevel level)
            entity.hurtServer(level, entity.damageSources().magic(), 1);
    }

    public static void woundEntity(@Nonnull LivingEntity entity, int percentage) {
        woundEntity(entity, percentage, false);
    }

    public static void treatEntity(@Nonnull LivingEntity entity) {
        if (entity.hasEffect(ModEffects.WOUNDED_EFFECT))
            entity.removeEffect(ModEffects.WOUNDED_EFFECT);
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 2));
    }
}
