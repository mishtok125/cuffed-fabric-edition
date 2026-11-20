package com.lazrproductions.cuffed.restraints;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.animation.ArmRestraintAnimationFlags;
import com.lazrproductions.cuffed.entity.animation.LegRestraintAnimationFlags;
import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;
import com.lazrproductions.cuffed.restraints.base.RestraintType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RestraintAPI {
    /**
     * Where all restraint registries found by Cuffed are kept and accessed.
     */
    public static final class Registries {
        static final LinkedHashMap<Identifier, AbstractRestraint> RESTRAINTS = new LinkedHashMap<>();

        /** Register a restraint. Other mods can call this from their initializer to add restraints. */
        public static void register(Identifier key, AbstractRestraint restraint) {
            if (containsKey(key)) {
                RestraintRegistryContainsKeyException ex = new RestraintRegistryContainsKeyException(key);
                CuffedMod.LOGGER.error("The Restraint Registry already contains the key " + key, ex);
                throw ex;
            }
            if (containsValue(restraint)) {
                RestraintRegistryContainsRestraintException ex = new RestraintRegistryContainsRestraintException(key);
                CuffedMod.LOGGER.error("The Restraint Registry already contains the restraint " + key, ex);
                throw ex;
            }
            AbstractRestraint otherWithSameItem = get(restraint.getItem(), restraint.getType());
            if (otherWithSameItem != null && otherWithSameItem.getType() == restraint.getType()) {
                ConflictingRestraintItemAndTypeException ex = new ConflictingRestraintItemAndTypeException(key, otherWithSameItem.getType());
                CuffedMod.LOGGER.error("The Restraint Registry already contains a restraint for " + key + " with the same restraint type of " + otherWithSameItem.getType(), ex);
                throw ex;
            }
            RESTRAINTS.put(key, restraint);
        }

        public static boolean containsKey(Identifier key) {
            return RESTRAINTS.containsKey(key);
        }

        public static boolean containsValue(AbstractRestraint restraint) {
            return RESTRAINTS.containsValue(restraint);
        }

        public static AbstractRestraint get(Identifier key) {
            return RESTRAINTS.get(key);
        }

        public static AbstractRestraint get(Item restraintItem, RestraintType type) {
            for (AbstractRestraint r : RESTRAINTS.values())
                if (r.getItem() == restraintItem && r.getType() == type)
                    return r;
            return null;
        }

        public static List<AbstractRestraint> getAllRestraints() {
            return new ArrayList<>(RESTRAINTS.values());
        }

        public static List<Item> getAllRestraintItems() {
            ArrayList<Item> res = new ArrayList<>();
            for (AbstractRestraint ent : RESTRAINTS.values())
                res.add(ent.getItem());
            return res;
        }

        public static List<Pair<Item, AbstractRestraint>> getAllRestraintItemsAndTheirRestraints() {
            ArrayList<Pair<Item, AbstractRestraint>> pairs = new ArrayList<>();
            for (AbstractRestraint ent : RESTRAINTS.values())
                pairs.add(new Pair<Item, AbstractRestraint>(ent.getItem(), ent));
            return pairs;
        }

        public static int size() {
            return 1;
        }

        public static int total() {
            return RESTRAINTS.size();
        }

        public static class RestraintRegistryContainsKeyException extends RuntimeException {
            public RestraintRegistryContainsKeyException(Identifier key) {
                super("The Restraint Registry already contains the key " + key);
            }
        }

        public static class RestraintRegistryContainsRestraintException extends RuntimeException {
            public RestraintRegistryContainsRestraintException(Identifier key) {
                super("The Restraint Registry already contains the restraint " + key);
            }
        }

        public static class ConflictingRestraintItemAndTypeException extends RuntimeException {
            public ConflictingRestraintItemAndTypeException(Identifier key, RestraintType type) {
                super("The Restraint Registry already contains a restraint for " + key + " with the same restraint type of " + type);
            }
        }
    }

    public static AbstractRestraint getRestraintFromTag(CompoundTag tag) {
        if (tag.contains("Id")) {
            AbstractRestraint r = getNewRestraintByKey(Identifier.parse(tag.getStringOr("Id", "")));
            if (r != null) {
                r.deserializeNBT(tag);
                return r;
            }
        }
        return null;
    }

    /**
     * Get the base class for the restraint with the given key
     * 
     * @param key The key of the restraint to get.
     * @return The base class for the restraint with the given, this is not to be
     *         assigned as the worn restraint of any player before the data is
     *         modified or deserialized.
     */
    public static AbstractRestraint getNewRestraintByKey(Identifier key) {
        AbstractRestraint base = Registries.get(key);
        if (base == null)
            return null;
        try {
            // A fresh instance per player; the original returned the shared registry instance.
            return base.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            CuffedMod.LOGGER.error("Could not create a new instance of restraint " + key, e);
            return null;
        }
    }

    /**
     * Get a new restraint instance from it's item.
     * 
     * @param stack  The itemstack to get a new restraint from
     * @param type   The RestraintType to get
     * @param player The player being restrained
     * @param captor The player who is applying the restraints
     * @return The new restraint instance from the item given
     */
    public static AbstractRestraint getRestraintFromStack(ItemStack stack, RestraintType type, ServerPlayer player, ServerPlayer captor) {
        if (stack.getItem() != null) {
            var restraintBase = Registries.get(stack.getItem(), type);
            if(restraintBase != null) {
                String className = restraintBase.getClass().getName();

                try {
                    Object o = Class.forName(className).getConstructor(ItemStack.class, ServerPlayer.class, ServerPlayer.class).newInstance(stack, player, captor);
                    if(o instanceof AbstractRestraint r)
                        return r;
                } catch (Exception e) {
                    CuffedMod.LOGGER.info("Error getting new instance of restraint for stack " + stack.getHoverName().getString());
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Get whether or not the given itemstack is a restraint item.
     * @param stack The stack to check
     */
    public static boolean isRestraintItem(ItemStack stack) {
        return IsHeadRestraintItem(stack) || IsArmRestraintItem(stack) || IsLegRestraintItem(stack);
    }
    
    /**
     * Get whether or not the given itemstack is a head restraint item.
     * @param stack The stack to check
     */
    public static boolean IsHeadRestraintItem(ItemStack stack) {
        AbstractRestraint r = Registries.get(stack.getItem(), RestraintType.Head);
        return r != null && r.getType() == RestraintType.Head;
    }

    /**
     * Get whether or not the given itemstack is a head restraint item.
     * @param stack The stack to check
     */
    public static boolean IsArmRestraintItem(ItemStack stack) {
        AbstractRestraint r = Registries.get(stack.getItem(),RestraintType.Arm);
        return r != null && r.getType() == RestraintType.Arm;
    }
    
    /**
     * Get whether or not the given itemstack is a leg restraint item.
     * @param stack The stack to check
     */
    public static boolean IsLegRestraintItem(ItemStack stack) {
        AbstractRestraint r = Registries.get(stack.getItem(), RestraintType.Leg);
        return r != null && r.getType() == RestraintType.Leg;
    }

    /**
     * Get whether or not the given item stack can be equipped as a restraint.
     * @param stack The item stack of the restraint to check
     * @param type The type of restraint to check for
     * @param player The player being restrained
     * @param captor The player restraining
     * @return Whether or not the restraint item can be equipped
     */
    public static boolean canEquipRestriantItem(ItemStack stack, RestraintType type, ServerPlayer player, ServerPlayer captor) {
        var r = Registries.get(stack.getItem(), type);
        return r != null ? r.canEquipRestraintItem(stack, player, captor) : false;
    }

    /**
     * Get the arm animation flags by its key.
     * @param key The key to check
     */
    public static ArmRestraintAnimationFlags getArmAnimationFlagByKey(Identifier key) {
        var r = Registries.get(key);
        return r != null ? r.getArmAnimationFlags() : ArmRestraintAnimationFlags.NONE;
    }
    
    /**
     * Get the leg animation flags by its key.
     * @param key The key to check
     */
    public static LegRestraintAnimationFlags getLegAnimationFlagByKey(Identifier key) {
        var r = Registries.get(key);
        return r != null ? r.getLegAnimationFlags() : LegRestraintAnimationFlags.NONE;
    }
}
