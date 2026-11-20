package com.lazrproductions.cuffed.items;


import net.minecraft.world.item.Item;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.items.base.AbstractRestraintItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class DuckTapeItem extends AbstractRestraintItem {

    public DuckTapeItem(Properties p) {
        super(p);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> components, TooltipFlag tooltipFlag) {
        components.accept(Component.translatable("info.cuffed.restraint_type.head").withStyle(ChatFormatting.GRAY));
        components.accept(Component.translatable("info.cuffed.restraint_type.arm").withStyle(ChatFormatting.GRAY));
        components.accept(Component.translatable("info.cuffed.restraint_type.leg").withStyle(ChatFormatting.GRAY));
        
        components.accept(Component.empty());
        components.accept(
            Component.translatable("info.cuffed.restraint_type.my_key")
            .withStyle(ChatFormatting.GRAY)
            .append(" ")
            .append(
                Component.translatable("info.cuffed.empty_hand")
                .withStyle(ChatFormatting.WHITE)));
        

        super.appendHoverText(stack, context, display, components, tooltipFlag);
    }
}
