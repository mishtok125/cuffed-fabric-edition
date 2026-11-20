package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.ChainKnotEntity;
import com.lazrproductions.cuffed.entity.CrumblingBlockEntity;
import com.lazrproductions.cuffed.entity.PadlockEntity;
import com.lazrproductions.cuffed.entity.WeightedAnchorEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {
    public static final EntityType<ChainKnotEntity> CHAIN_KNOT = register("chain_knot",
            EntityType.Builder.<ChainKnotEntity>of(ChainKnotEntity::new, MobCategory.MISC)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .sized(6 / 16f, 0.5f).canSpawnFarFromPlayer().fireImmune());
    public static final EntityType<PadlockEntity> PADLOCK = register("padlock",
            EntityType.Builder.<PadlockEntity>of(PadlockEntity::new, MobCategory.MISC)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .sized(6 / 16f, 0.1f).canSpawnFarFromPlayer().fireImmune());
    public static final EntityType<WeightedAnchorEntity> WEIGHTED_ANCHOR = register("weighted_anchor",
            EntityType.Builder.<WeightedAnchorEntity>of(WeightedAnchorEntity::new, MobCategory.MISC)
                    .sized(8 / 16f, 0.5f).canSpawnFarFromPlayer().fireImmune()
                    .clientTrackingRange(10));
    public static final EntityType<CrumblingBlockEntity> CRUMBLING_BLOCK = register("crumbling_block",
            EntityType.Builder.<CrumblingBlockEntity>of(CrumblingBlockEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F));

    static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(CuffedMod.MODID, name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(WEIGHTED_ANCHOR, WeightedAnchorEntity.createAttributes());
    }
}
