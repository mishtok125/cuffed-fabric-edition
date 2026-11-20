package com.lazrproductions.cuffed.recipes;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.ArrayList;

import javax.annotation.Nonnull;

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

public class KeyRingCreateRecipe extends CustomRecipe {
    public KeyRingCreateRecipe() {
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        ArrayList<ItemStack> keyStack = new ArrayList<ItemStack>(0);

        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getItem(i).isEmpty())
                if (inv.getItem(i).is(ModItems.KEY)) {
                    ItemStack stack = inv.getItem(i);
                    Item item = stack.getItem();
                    if (item == ModItems.KEY) {
                        keyStack.add(stack);
                    }

                } else
                    return false;

        }

        return keyStack.size() > 0;
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingInput inv) {

        ArrayList<ItemStack> keyStacks = new ArrayList<ItemStack>(0);

        if (matches(inv, null)) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getItem(i);
                Item item = stack.getItem();

                if (item == ModItems.KEY) {
                    keyStacks.add(stack);
                }
            }


            ItemStack newStack = new ItemStack(ModItems.KEY_RING);
            newStack.setCount(1);
            //int keys = 2;
                
            for (ItemStack stack : keyStacks) {
                if(StackTag.get(stack).contains(KeyItem.TAG_ID)) {
                    KeyRingItem.addKey(newStack, stack);
                }
            }
                        
            StackTag.update(newStack, t -> t.putInt("Keys", keyStacks.size()));

            return newStack;
            
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<KeyRingCreateRecipe> getSerializer() {
        return ModRecipes.KEY_RING_CREATE;
    }

}
