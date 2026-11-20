package com.lazrproductions.cuffed.items;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.blocks.PosterBlock;
import com.lazrproductions.cuffed.blocks.base.PosterType;
import com.lazrproductions.cuffed.init.ModBlocks;
import com.lazrproductions.cuffed.init.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PosterBlockItem extends BlockItem {
    public static final String POSTER_TYPE_TAG = "Poster"; 
    
    public PosterBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
     }

    @Override
    @Nullable
    protected BlockState getPlacementState(@Nonnull BlockPlaceContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if(state != null && state.is(ModBlocks.POSTER))
            state = state.setValue(PosterBlock.POSTER_TYPE, getPosterType(ctx.getItemInHand()));
        return state;
    }

    public static PosterType getPosterType(@Nonnull ItemStack stack) {
        CompoundTag tag = StackTag.get(stack);
        if(tag.contains(POSTER_TYPE_TAG))
            return PosterType.fromString(tag.getStringOr(POSTER_TYPE_TAG, ""));
        return PosterType.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> lines, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, lines, tooltipFlag);

        lines.accept(Component.translatable(getDescriptionId() +"."+getPosterType(stack).getSerializedName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    public static ItemStack newItemFromType(PosterType type) {
        ItemStack stack = ModItems.POSTER_ITEM.getDefaultInstance().copyWithCount(1);
        if (type != PosterType.NONE)
            StackTag.update(stack, tag -> tag.putString(POSTER_TYPE_TAG, type.getSerializedName()));
        return stack;
    }
}
