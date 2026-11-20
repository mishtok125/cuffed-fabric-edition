package com.lazrproductions.cuffed.items.base;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AbstractRestraintKeyItem extends Item {
    public AbstractRestraintKeyItem(Properties p) {
        super(p);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> components, TooltipFlag tooltipFlag) {

        components.accept(Component.translatable("info.cuffed.restraint_type.key").withStyle(ChatFormatting.GRAY));
            
        super.appendHoverText(stack, context, display, components, tooltipFlag);
    }
}
