package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantments are data-driven: their definitions live in data/cuffed/enchantment/*.json
 * and their effects are implemented in code by checking these keys.
 */
public class ModEnchantments {
    public static final ResourceKey<Enchantment> IMBUE = key("imbue");
    public static final ResourceKey<Enchantment> FAMINE = key("famine");
    public static final ResourceKey<Enchantment> SHROUD = key("shroud");
    public static final ResourceKey<Enchantment> EXHAUST = key("exhaust");
    public static final ResourceKey<Enchantment> SILENCE = key("silence");
    public static final ResourceKey<Enchantment> BUOYANT = key("buoyant");

    static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(CuffedMod.MODID, name));
    }
}
