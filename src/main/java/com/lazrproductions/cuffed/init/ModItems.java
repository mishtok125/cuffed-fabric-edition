package com.lazrproductions.cuffed.init;

import java.util.function.Function;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.items.BakedKeyMoldItem;
import com.lazrproductions.cuffed.items.BandageItem;
import com.lazrproductions.cuffed.items.CreativeKey;
import com.lazrproductions.cuffed.items.CreativeRestraintCutter;
import com.lazrproductions.cuffed.items.DuckTapeItem;
import com.lazrproductions.cuffed.items.FuzzyHandcuffsItem;
import com.lazrproductions.cuffed.items.HandcuffsItem;
import com.lazrproductions.cuffed.items.KeyItem;
import com.lazrproductions.cuffed.items.KeyMoldItem;
import com.lazrproductions.cuffed.items.KeyRingItem;
import com.lazrproductions.cuffed.items.KnifeItem;
import com.lazrproductions.cuffed.items.Padlock;
import com.lazrproductions.cuffed.items.PossessionsBox;
import com.lazrproductions.cuffed.items.PosterBlockItem;
import com.lazrproductions.cuffed.items.PrisonerTagItem;
import com.lazrproductions.cuffed.items.ShacklesItem;
import com.lazrproductions.cuffed.items.TrayItem;
import com.lazrproductions.cuffed.items.WeightedAnchorItem;
import com.lazrproductions.cuffed.items.base.AbstractRestraintKeyItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public class ModItems {
    // Normal Items
    public static final Item INFORMATION_BOOKLET = register("information_booklet", Item::new,
            new Item.Properties().stacksTo(1));
    public static final Item KEY = register("key", KeyItem::new, new Item.Properties().stacksTo(1));
    public static final Item KEY_RING = register("key_ring", KeyRingItem::new, new Item.Properties().stacksTo(1));
    public static final Item KEY_MOLD = register("key_mold", KeyMoldItem::new, new Item.Properties().stacksTo(1));
    public static final Item BAKED_KEY_MOLD = register("baked_key_mold", BakedKeyMoldItem::new, new Item.Properties().stacksTo(1));
    public static final Item HANDCUFFS_KEY = register("handcuffs_key", AbstractRestraintKeyItem::new, new Item.Properties().stacksTo(1));
    public static final Item SHACKLES_KEY = register("shackles_key", AbstractRestraintKeyItem::new, new Item.Properties().stacksTo(1));
    public static final Item HANDCUFFS = register("handcuffs", HandcuffsItem::new, new Item.Properties().durability(CuffedMod.SERVER_CONFIG.RESTRAINT_DURABILITY_HANDCUFFS.get()).enchantable(1));
    public static final Item FUZZY_HANDCUFFS = register("fuzzy_handcuffs", FuzzyHandcuffsItem::new, new Item.Properties().durability(CuffedMod.SERVER_CONFIG.RESTRAINT_DURABILITY_FUZZY_HANDCUFFS.get()).enchantable(1));
    public static final Item SHACKLES = register("shackles", ShacklesItem::new, new Item.Properties().durability(CuffedMod.SERVER_CONFIG.RESTRAINT_DURABILITY_SHACKLES.get()).enchantable(1));
    public static final Item WEIGHTED_ANCHOR_ITEM = register("weighted_anchor", WeightedAnchorItem::new, new Item.Properties().stacksTo(1));
    public static final Item POSSESSIONSBOX = register("possessions_box", PossessionsBox::new, new Item.Properties().stacksTo(1));
    public static final Item PADLOCK = register("padlock", Padlock::new, new Item.Properties().stacksTo(16));
    public static final Item LOCKPICK = register("lockpick", Item::new, new Item.Properties().durability(3));
    public static final Item PRISONER_TAG = register("prisoner_tag", PrisonerTagItem::new, new Item.Properties().stacksTo(1));
    public static final Item FORK = register("fork", Item::new, new Item.Properties().durability(5));
    public static final Item SPOON = register("spoon", Item::new, new Item.Properties().durability(5));
    public static final Item KNIFE = register("knife", KnifeItem::new, new Item.Properties().durability(5));
    public static final Item DUCK_TAPE = register("duck_tape", DuckTapeItem::new, new Item.Properties());
    public static final Item BANDAGE = register("bandage", BandageItem::new, new Item.Properties());

    // Block Items
    public static final Item CELL_DOOR_ITEM = registerBlock("cell_door", ModBlocks.CELL_DOOR, new Item.Properties());
    public static final Item REINFORCED_STONE_ITEM = registerBlock("reinforced_stone", ModBlocks.REINFORCED_STONE, new Item.Properties());
    public static final Item REINFORCED_SMOOTH_STONE_ITEM = registerBlock("reinforced_smooth_stone", ModBlocks.REINFORCED_SMOOTH_STONE, new Item.Properties());
    public static final Item REINFORCED_LAMP_ITEM = registerBlock("reinforced_lamp", ModBlocks.REINFORCED_LAMP, new Item.Properties());
    public static final Item REINFORCED_STONE_CHISELED_ITEM = registerBlock("chiseled_reinforced_stone", ModBlocks.REINFORCED_STONE_CHISELED, new Item.Properties());
    public static final Item REINFORCED_STONE_SLAB_ITEM = registerBlock("reinforced_stone_slab", ModBlocks.REINFORCED_STONE_SLAB, new Item.Properties());
    public static final Item REINFORCED_STONE_STAIRS_ITEM = registerBlock("reinforced_stone_stairs", ModBlocks.REINFORCED_STONE_STAIRS, new Item.Properties());
    public static final Item REINFORCED_BARS_ITEM = registerBlock("reinforced_bars", ModBlocks.REINFORCED_BARS, new Item.Properties());
    public static final Item REINFORCED_BARS_GAPPED_ITEM = registerBlock("reinforced_bars_gap", ModBlocks.REINFORCED_BARS_GAPPED, new Item.Properties());
    public static final Item PILLORY_ITEM = registerBlock("pillory", ModBlocks.PILLORY, new Item.Properties());
    public static final Item GUILLOTINE_ITEM = registerBlock("guillotine", ModBlocks.GUILLOTINE, new Item.Properties());
    public static final Item SAFE_ITEM = registerBlock("safe", ModBlocks.SAFE, new Item.Properties());
    public static final Item BUNK_ITEM = registerBlock("bunk", ModBlocks.BUNK, new Item.Properties().stacksTo(1));
    public static final Item POSTER_ITEM = register("poster", p -> new PosterBlockItem(ModBlocks.POSTER, p),
            new Item.Properties().stacksTo(1).useBlockDescriptionPrefix());
    public static final Item TRAY = register("tray", p -> new TrayItem(ModBlocks.TRAY, p),
            new Item.Properties().stacksTo(1).useBlockDescriptionPrefix());

    // Creative Items
    public static final Item CREATIVE_RESTRAINT_CUTTER = register("creative_restraint_cutter", CreativeRestraintCutter::new, new Item.Properties());
    public static final Item CREATIVE_KEY = register("creative_key", CreativeKey::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final Item CREATIVE_BIND_BREAKER = register("creative_bind_breaker", CreativeKey::new, new Item.Properties().rarity(Rarity.EPIC));

    static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CuffedMod.MODID, name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    static Item registerBlock(String name, Block block, Item.Properties properties) {
        return register(name, p -> new BlockItem(block, p), properties.useBlockDescriptionPrefix());
    }

    public static void register() {
        CuffedMod.LOGGER.info("Registered items");
    }
}
