package com.lazrproductions.cuffed.init;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> HANG = register("hang");

    public static ResourceKey<DamageType> register(String key) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(CuffedMod.MODID, key));
    }

    public static DamageSource GetModSource(Entity entity, ResourceKey<DamageType> type, @Nullable Entity other) {
        return new DamageSource(entity.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type),
                entity, other == null ? entity : other);
    }
}
