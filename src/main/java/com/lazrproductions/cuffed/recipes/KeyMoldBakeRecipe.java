package com.lazrproductions.cuffed.recipes;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.items.BakedKeyMoldItem;
import com.lazrproductions.cuffed.items.KeyMoldItem;
import com.lazrproductions.cuffed.utils.StackTag;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/** Smelting a key mold that has a copied key produces a baked key mold carrying that key. */
public class KeyMoldBakeRecipe extends SmeltingRecipe {
    public KeyMoldBakeRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
            Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public boolean matches(SingleRecipeInput inv, Level level) {
        return inv.getItem(0).is(ModItems.KEY_MOLD) && StackTag.get(inv.getItem(0)).contains(KeyMoldItem.TAG_COPIED_KEY);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput inv) {
        return BakedKeyMoldItem.createFromRawMold(inv.getItem(0));
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public RecipeSerializer getSerializer() {
        return ModRecipes.KEY_MOLD_BAKE;
    }
}
