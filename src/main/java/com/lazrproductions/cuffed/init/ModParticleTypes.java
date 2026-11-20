package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class ModParticleTypes {
    public static final SimpleParticleType BLOOD_DRIP_FALL_PARTICLE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(CuffedMod.MODID, "blood_drip"),
            FabricParticleTypes.simple(true));

    public static void register() {
    }
}
