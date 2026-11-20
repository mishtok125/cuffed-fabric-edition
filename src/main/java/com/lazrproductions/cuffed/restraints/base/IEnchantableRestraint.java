package com.lazrproductions.cuffed.restraints.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantments are data-driven since 1.21, so restraints keep a plain id -> level map
 * that can be read without registry access (on either side).
 */
public interface IEnchantableRestraint {
    /** Map of enchantment id (as string) to level. */
    CompoundTag getEnchantments();

    void setEnchantments(CompoundTag tag);

    default boolean hasEnchantment(ResourceKey<Enchantment> enchantment) {
        return getEnchantmentLevel(enchantment) > 0;
    }

    default int getEnchantmentLevel(ResourceKey<Enchantment> enchantment) {
        CompoundTag tag = getEnchantments();
        if (tag == null)
            return 0;
        return tag.getIntOr(enchantment.identifier().toString(), 0);
    }

    default void enchant(ResourceKey<Enchantment> enchantment, int value) {
        CompoundTag tag = getEnchantments();
        if (tag == null) {
            tag = new CompoundTag();
            setEnchantments(tag);
        }
        tag.putInt(enchantment.identifier().toString(), value);
    }

    default boolean isEnchanted() {
        CompoundTag tag = getEnchantments();
        return tag != null && !tag.isEmpty();
    }
}
