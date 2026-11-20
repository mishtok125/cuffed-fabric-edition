package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.blocks.base.PosterType;
import com.lazrproductions.cuffed.items.PosterBlockItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ModCreativeTabs {
    public static final CreativeModeTab CUFFED_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(CuffedMod.MODID, "cuffed_tab"),
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cuffed"))
                    .icon(() -> ModItems.HANDCUFFS.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.INFORMATION_BOOKLET);
                        output.accept(ModItems.HANDCUFFS);
                        output.accept(ModItems.SHACKLES);
                        output.accept(ModItems.FUZZY_HANDCUFFS);

                        output.accept(ModItems.HANDCUFFS_KEY);
                        output.accept(ModItems.SHACKLES_KEY);

                        output.accept(Items.BUNDLE);

                        output.accept(ModItems.POSSESSIONSBOX);
                        output.accept(ModItems.PRISONER_TAG);

                        output.accept(ModItems.DUCK_TAPE);
                        output.accept(ModItems.BANDAGE);
                        
                        output.accept(ModItems.TRAY);
                        output.accept(ModItems.FORK);
                        output.accept(ModItems.SPOON);
                        output.accept(ModItems.KNIFE);

                        output.accept(ModItems.PADLOCK);
                        output.accept(ModItems.KEY);
                        output.accept(ModItems.KEY_RING);
                        output.accept(ModItems.KEY_MOLD);
                        output.accept(ModItems.BAKED_KEY_MOLD);
                        output.accept(ModItems.LOCKPICK);

                        
                        output.accept(ModItems.WEIGHTED_ANCHOR_ITEM);


                        output.accept(ModItems.CELL_DOOR_ITEM);
                        output.accept(ModItems.REINFORCED_BARS_ITEM);
                        output.accept(ModItems.REINFORCED_BARS_GAPPED_ITEM);
                        output.accept(ModItems.REINFORCED_SMOOTH_STONE_ITEM);
                        output.accept(ModItems.REINFORCED_STONE_ITEM);
                        output.accept(ModItems.REINFORCED_STONE_SLAB_ITEM);
                        output.accept(ModItems.REINFORCED_STONE_STAIRS_ITEM);
                        output.accept(ModItems.REINFORCED_STONE_CHISELED_ITEM);
                        output.accept(ModItems.REINFORCED_LAMP_ITEM);


                        output.accept(ModItems.PILLORY_ITEM);
                        output.accept(ModItems.GUILLOTINE_ITEM);

                        
                        output.accept(ModItems.BUNK_ITEM);


                        output.accept(ModItems.SAFE_ITEM);


                        output.accept(PosterBlockItem.newItemFromType(PosterType.NONE));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.SERENITY));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.SKELETON));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.IMPUNITY));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.ZOOOM));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.A_SHADOW_LOCKED_AWAY));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.PRISONER));
                        output.accept(PosterBlockItem.newItemFromType(PosterType.LANTERN));



                        output.accept(ModItems.CREATIVE_RESTRAINT_CUTTER);
                        output.accept(ModItems.CREATIVE_KEY);
                        output.accept(ModItems.CREATIVE_BIND_BREAKER);
                        
                        parameters.holders().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                                .filter(enchantment -> enchantment.key().identifier().getNamespace().equals(CuffedMod.MODID))
                                .filter(enchantment -> !enchantment.is(ModEnchantments.SILENCE))
                                .sorted(java.util.Comparator.comparing(enchantment -> enchantment.key().identifier()))
                                .forEach(enchantment -> {
                                    for (int level = enchantment.value().getMinLevel();
                                            level <= enchantment.value().getMaxLevel(); level++) {
                                        output.accept(EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level)),
                                                CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                                    }
                                });
                    }).build());

    public static void register() {
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> {
            output.getDisplayStacks().removeIf(ModCreativeTabs::isSilenceBook);
            output.getSearchTabStacks().removeIf(ModCreativeTabs::isSilenceBook);
        });
    }

    private static boolean isSilenceBook(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK)
                && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                        .keySet().stream().anyMatch(enchantment -> enchantment.is(ModEnchantments.SILENCE));
    }
}
