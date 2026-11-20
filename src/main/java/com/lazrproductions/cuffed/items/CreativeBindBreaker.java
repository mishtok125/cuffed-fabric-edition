package com.lazrproductions.cuffed.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CreativeBindBreaker extends Item {
    public CreativeBindBreaker(Properties p) {
        super(p);
    }

    public boolean isFoil(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> lore, TooltipFlag flag) {
        lore.accept(Component.translatable(getDescriptionId()+".lore").withStyle(ChatFormatting.GRAY));
    }
}
