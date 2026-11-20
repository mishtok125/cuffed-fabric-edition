package com.lazrproductions.cuffed.recipes;


import com.lazrproductions.cuffed.utils.StackTag;
import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.items.KeyItem;
import com.lazrproductions.cuffed.items.KeyMoldItem;
import com.lazrproductions.cuffed.items.KeyRingItem;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class KeyRingDisassembleRecipe extends CustomRecipe {
    public KeyRingDisassembleRecipe() {
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        ItemStack ringStack = null;

        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getItem(i).isEmpty())
                if (inv.getItem(i).is(ModItems.KEY_RING))
                    ringStack = inv.getItem(i);
                else
                    return false;

        }

        if (ringStack != null) {
            CompoundTag tag = StackTag.get(ringStack);
            if (tag != null && tag.getIntOr("Keys", 0) > 0)
                return true;
        }

        return false;
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingInput inv) {

        ItemStack ringStack = null;

        if (matches(inv, null)) {
            for (int i = 0; i < inv.size(); i++) {
                if (!inv.getItem(i).isEmpty())
                    if (inv.getItem(i).is(ModItems.KEY_RING))
                        ringStack = inv.getItem(i);
            }

            if (ringStack != null) {
                CompoundTag tag = StackTag.get(ringStack);
                if (tag.contains(KeyRingItem.TAG_BOUND_LOCKS)) {
                    ListTag boundKeysTag = tag.getListOrEmpty(KeyRingItem.TAG_BOUND_LOCKS);
                    if (boundKeysTag.size() > 0) {
                        ItemStack stack = ModItems.KEY.getDefaultInstance();
                        stack.setCount(1);

                        CompoundTag keyOnRingTag = boundKeysTag.getCompoundOrEmpty(boundKeysTag.size() - 1);

                        keyOnRingTag.read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).ifPresent(id -> KeyItem.setBoundId(stack, id));
                        if(keyOnRingTag.contains(KeyMoldItem.TAG_NAME))
                        {
                            net.minecraft.network.chat.Component name = com.lazrproductions.cuffed.utils.NicknameUtil.fromJson(keyOnRingTag.getStringOr(KeyMoldItem.TAG_NAME, ""));
                            if (name != null)
                                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, name);
                        }
                        return stack;
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<KeyRingDisassembleRecipe> getSerializer() {
        return ModRecipes.KEY_RING_DISASSEMBLE;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.size(), ItemStack.EMPTY);



        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack stack = container.getItem(i);
            CompoundTag tag = StackTag.get(stack);
            
            if (stack.is(ModItems.KEY_RING) 
                    && tag.contains(KeyRingItem.TAG_KEYS) 
                    && tag.getIntOr(KeyRingItem.TAG_KEYS, 0) > 1) {
                int numOfKeys = tag.getIntOr(KeyRingItem.TAG_KEYS, 0);
                if (tag.contains(KeyRingItem.TAG_BOUND_LOCKS)) {
                    ListTag boundKeysTag = tag.getListOrEmpty(KeyRingItem.TAG_BOUND_LOCKS);
                    if(boundKeysTag.size() > 0) {
                        boundKeysTag.remove(boundKeysTag.size() - 1);
                        tag.put(KeyRingItem.TAG_BOUND_LOCKS, boundKeysTag);
                        tag.putInt(KeyRingItem.TAG_KEYS, numOfKeys - 1);
                    }
                }

                ItemStack remaining = stack.copy();
                StackTag.set(remaining, tag);
                nonnulllist.set(i, remaining);
            }
        }

        return nonnulllist;
    }
}
