package com.lazrproductions.cuffed.recipes;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.items.PosterBlockItem;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PosterChangeRecipe extends CustomRecipe {
    public PosterChangeRecipe() {
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        return isGridValid(inv);
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingInput inv) {

        if (matches(inv, null)) {
            ItemStack posterStack = getPosterFromGrid(inv);
            return PosterBlockItem.newItemFromType(PosterBlockItem.getPosterType(posterStack).next());
        }

        return ItemStack.EMPTY;
    }

    public ArrayList<ItemStack> getAllValidItemsInGrid(@Nonnull CraftingInput inv) {
        ArrayList<ItemStack> validInGrid = new ArrayList<ItemStack>(0);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack checkingStack = inv.getItem(i);
            if (!checkingStack.isEmpty())
                if (checkingStack.is(ModItems.POSTER_ITEM))
                    validInGrid.add(checkingStack);
        }

        return validInGrid;
    }

    public ItemStack getPosterFromGrid(@Nonnull CraftingInput inv) {
        ArrayList<ItemStack> moldsInGrid = getAllValidItemsInGrid(inv);
        return moldsInGrid.get(0);
    }

    public boolean isGridValid(@Nonnull CraftingInput inv) {
        int numOfPosters = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack checkingStack = inv.getItem(i);
            if (!checkingStack.isEmpty())
                if (checkingStack.is(ModItems.POSTER_ITEM))
                    numOfPosters++;
                else
                    return false;
        }

        // there must be only 1 poster
        return numOfPosters == 1;
    }

    @Override
    public RecipeSerializer<PosterChangeRecipe> getSerializer() {
        return ModRecipes.POSTER_CHANGE;
    }
}
