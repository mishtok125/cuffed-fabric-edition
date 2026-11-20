package com.lazrproductions.cuffed.utils;

import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Item stacks no longer carry a free-form NBT tag. Cuffed's item data is kept in the
 * {@code minecraft:custom_data} component instead, using the same keys as before.
 */
public final class StackTag {
    private StackTag() {
    }

    /** A copy of the stack's custom data (empty if it has none). Changes to it are NOT written back. */
    public static CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    public static boolean has(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty();
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        if (tag == null || tag.isEmpty())
            stack.remove(DataComponents.CUSTOM_DATA);
        else
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /** Modify the stack's custom data in place. */
    public static void update(ItemStack stack, Consumer<CompoundTag> modifier) {
        CompoundTag tag = get(stack);
        modifier.accept(tag);
        set(stack, tag);
    }
}
