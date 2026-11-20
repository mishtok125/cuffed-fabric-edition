package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.recipes.BakedKeyMoldCopyRecipe;
import com.lazrproductions.cuffed.recipes.KeyMoldBakeRecipe;
import com.lazrproductions.cuffed.recipes.KeyMoldCopyRecipe;
import com.lazrproductions.cuffed.recipes.KeyRingAddRecipe;
import com.lazrproductions.cuffed.recipes.KeyRingCreateRecipe;
import com.lazrproductions.cuffed.recipes.KeyRingDisassembleRecipe;
import com.lazrproductions.cuffed.recipes.PosterChangeRecipe;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {
    public static final RecipeSerializer<KeyRingCreateRecipe> KEY_RING_CREATE = special("key_ring_create", new KeyRingCreateRecipe());
    public static final RecipeSerializer<KeyRingAddRecipe> KEY_RING_ADD = special("key_ring_add", new KeyRingAddRecipe());
    public static final RecipeSerializer<KeyRingDisassembleRecipe> KEY_RING_DISASSEMBLE = special("key_ring_disassemble", new KeyRingDisassembleRecipe());
    public static final RecipeSerializer<KeyMoldCopyRecipe> KEY_MOLD_COPY = special("key_mold_copy", new KeyMoldCopyRecipe());
    public static final RecipeSerializer<BakedKeyMoldCopyRecipe> BAKED_KEY_MOLD_COPY = special("baked_key_mold_copy", new BakedKeyMoldCopyRecipe());
    public static final RecipeSerializer<PosterChangeRecipe> POSTER_CHANGE = special("poster_change", new PosterChangeRecipe());

    public static final RecipeSerializer<KeyMoldBakeRecipe> KEY_MOLD_BAKE = register("key_mold_bake",
            new RecipeSerializer<>(AbstractCookingRecipe.cookingMapCodec(KeyMoldBakeRecipe::new, 200),
                    AbstractCookingRecipe.cookingStreamCodec(KeyMoldBakeRecipe::new)));

    /** Special crafting recipes have no data, so one shared instance is enough. */
    static <T extends CustomRecipe> RecipeSerializer<T> special(String name, T instance) {
        return register(name, new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance)));
    }

    static <T extends RecipeSerializer<?>> T register(String name, T serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(CuffedMod.MODID, name), serializer);
    }

    public static void register() {
    }
}
