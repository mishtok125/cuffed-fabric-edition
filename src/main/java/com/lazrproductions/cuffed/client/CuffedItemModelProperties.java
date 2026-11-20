package com.lazrproductions.cuffed.client;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.blocks.base.PosterType;
import com.lazrproductions.cuffed.items.KeyRingItem;
import com.lazrproductions.cuffed.items.PossessionsBox;
import com.lazrproductions.cuffed.items.TrayItem;
import com.lazrproductions.cuffed.utils.StackTag;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;

/**
 * Replacement for the old ItemProperties overrides ("cuffed:keys", "cuffed:filled", "cuffed:poster"),
 * used by the item model definitions in assets/cuffed/items.
 */
public final class CuffedItemModelProperties {
    private CuffedItemModelProperties() {
    }

    public record KeyCount() implements RangeSelectItemModelProperty {
        public static final MapCodec<KeyCount> MAP_CODEC = MapCodec.unit(new KeyCount());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return StackTag.get(stack).getIntOr(KeyRingItem.TAG_KEYS, 0);
        }

        @Override
        public MapCodec<KeyCount> type() {
            return MAP_CODEC;
        }
    }

    public record PossessionsFilled() implements RangeSelectItemModelProperty {
        public static final MapCodec<PossessionsFilled> MAP_CODEC = MapCodec.unit(new PossessionsFilled());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return StackTag.get(stack).getListOrEmpty(PossessionsBox.TAG_ITEMS).isEmpty() ? 0 : 1;
        }

        @Override
        public MapCodec<PossessionsFilled> type() {
            return MAP_CODEC;
        }
    }

    public record TrayFilled() implements RangeSelectItemModelProperty {
        public static final MapCodec<TrayFilled> MAP_CODEC = MapCodec.unit(new TrayFilled());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return TrayItem.trayHasFoodItem(stack) || TrayItem.trayHasSpoon(stack)
                    || TrayItem.trayHasFork(stack) || TrayItem.trayHasKnife(stack) ? 1 : 0;
        }

        @Override
        public MapCodec<TrayFilled> type() {
            return MAP_CODEC;
        }
    }

    public record Poster() implements RangeSelectItemModelProperty {
        public static final MapCodec<Poster> MAP_CODEC = MapCodec.unit(new Poster());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return PosterType.getfromItem(stack).toInt();
        }

        @Override
        public MapCodec<Poster> type() {
            return MAP_CODEC;
        }
    }

    public static void register() {
        RangeSelectItemModelProperties.ID_MAPPER.put(id("keys"), KeyCount.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(id("possessions_filled"), PossessionsFilled.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(id("tray_filled"), TrayFilled.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(id("poster"), Poster.MAP_CODEC);
    }

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CuffedMod.MODID, path);
    }
}
