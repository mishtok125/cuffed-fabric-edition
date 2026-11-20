package com.lazrproductions.cuffed.recipes;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.items.KeyItem;
import com.lazrproductions.cuffed.items.KeyRingItem;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class KeyRingAddRecipe extends CustomRecipe {
    public KeyRingAddRecipe() {
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        ItemStack ringStack = null;
        ArrayList<ItemStack> keyStack = new ArrayList<ItemStack>(0);

        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getItem(i).isEmpty())
                if (inv.getItem(i).is(ModItems.KEY) || inv.getItem(i).is(ModItems.KEY_RING)) {

                    ItemStack stack = inv.getItem(i);
                    Item item = stack.getItem();
                    if (ringStack == null && item == ModItems.KEY_RING) {
                        ringStack = stack;
                    }

                    if (item == ModItems.KEY) {
                        keyStack.add(stack);
                    }

                } else
                    return false;

        }

        if (ringStack != null)
            if ((StackTag.get(ringStack).getIntOr("Keys", 0)) + keyStack.size() > CuffedMod.SERVER_CONFIG.MAX_KEYS_PER_RING.get())
                return false;

        if (ringStack == null || keyStack.size() == 0)
            return false;

        return ringStack != null && keyStack.size() > 0;
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingInput inv) {

        ItemStack ringStack = null;
        ArrayList<ItemStack> keyStacks = new ArrayList<ItemStack>(0);

        if (matches(inv, null)) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getItem(i);
                Item item = stack.getItem();

                if (ringStack == null) {
                    if (item == ModItems.KEY_RING) {
                        ringStack = stack;
                    }
                }

                if (item == ModItems.KEY) {
                    keyStacks.add(stack);
                }
            }

            if (ringStack != null) {
                ItemStack newStack = ringStack.copy();
                newStack.setCount(1);
                int keys = 2;
                if (StackTag.has(newStack))
                    keys = StackTag.get(newStack).getIntOr("Keys", 0);

                for (ItemStack stack : keyStacks) {
                    if(StackTag.get(stack).contains(KeyItem.TAG_ID)) {
                        KeyRingItem.addKey(newStack, stack);
                    }
                }
                
                final int newKeyCount = keys + keyStacks.size();
                StackTag.update(newStack, t -> t.putInt("Keys", newKeyCount));

                return newStack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<KeyRingAddRecipe> getSerializer() {
        return ModRecipes.KEY_RING_ADD;
    }

}
