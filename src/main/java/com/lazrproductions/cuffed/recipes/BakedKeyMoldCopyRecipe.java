package com.lazrproductions.cuffed.recipes;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.items.BakedKeyMoldItem;
import com.lazrproductions.cuffed.items.KeyMoldItem;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BakedKeyMoldCopyRecipe extends CustomRecipe {
    public BakedKeyMoldCopyRecipe() {
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        return isGridValid(inv);
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack assemble(CraftingInput inv) {

        if (matches(inv, null)) {
            ItemStack moldStack = getMoldFromGrid(inv);
            return StackTag.get(moldStack).contains(KeyMoldItem.TAG_COPIED_KEY)
                    ? BakedKeyMoldItem.createKeyFrom(moldStack, 1)
                    : ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    public ArrayList<ItemStack> getAllMoldsInGrid(@Nonnull CraftingInput inv) {
        ArrayList<ItemStack> moldsInGrid = new ArrayList<ItemStack>(0);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack checkingStack = inv.getItem(i);
            if (!checkingStack.isEmpty())
                if (checkingStack.is(ModItems.BAKED_KEY_MOLD) || checkingStack.is(Items.IRON_INGOT)) {
                    Item item = checkingStack.getItem();
                    if (item == ModItems.BAKED_KEY_MOLD
                            && StackTag.get(checkingStack).contains(KeyMoldItem.TAG_COPIED_KEY))
                        moldsInGrid.add(checkingStack);
                }
        }

        return moldsInGrid;
    }

    public ArrayList<ItemStack> getAllValidItemsInGrid(@Nonnull CraftingInput inv) {
        ArrayList<ItemStack> validInGrid = new ArrayList<ItemStack>(0);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack checkingStack = inv.getItem(i);
            if (!checkingStack.isEmpty())
                if (checkingStack.is(ModItems.BAKED_KEY_MOLD) || checkingStack.is(Items.IRON_INGOT))
                    validInGrid.add(checkingStack);
        }

        return validInGrid;
    }

    public ItemStack getMoldFromGrid(@Nonnull CraftingInput inv) {
        ArrayList<ItemStack> moldsInGrid = getAllMoldsInGrid(inv);
        return moldsInGrid.get(0);
    }

    public boolean isGridValid(@Nonnull CraftingInput inv) {
        int numofMolds = 0;
        int numOfIngots = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack checkingStack = inv.getItem(i);
            if (!checkingStack.isEmpty())
                if (checkingStack.is(ModItems.BAKED_KEY_MOLD)
                        && StackTag.get(checkingStack).contains(KeyMoldItem.TAG_COPIED_KEY))
                    numofMolds++;
                else if (checkingStack.is(Items.IRON_INGOT))
                    numOfIngots++;
                else
                    return false;
        }

        // there must be only 1 ingot and only 1 mold
        return numOfIngots == 1 && numofMolds == 1;
    }

    @Override
    public RecipeSerializer<BakedKeyMoldCopyRecipe> getSerializer() {
        return ModRecipes.BAKED_KEY_MOLD_COPY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.size(), ItemStack.EMPTY);
    
        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack item = container.getItem(i);
            if (item.is(ModItems.BAKED_KEY_MOLD)) {
                int oldQuality = StackTag.get(item).getIntOr(BakedKeyMoldItem.TAG_QUALITY, 0);
                if(oldQuality > 1) {
                    StackTag.update(item, t -> t.putInt("Quality", oldQuality - 1));
                    nonnulllist.set(i, item.copy());
                }
            }
        }
        return nonnulllist;
    }
}
