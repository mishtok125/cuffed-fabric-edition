package com.lazrproductions.cuffed.items;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.init.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class KeyMoldItem extends Item {
    
    public static final String TAG_COPIED_KEY = "CopiedKey";
    public static final String TAG_NAME = "Name";

    public KeyMoldItem(Properties properties) {
        super(properties);
    }


    public static ItemStack createFromKey(ItemStack keyStack) {
        ItemStack newMold = new ItemStack(ModItems.KEY_MOLD, 1);
        
        if(!StackTag.get(keyStack).contains(KeyItem.TAG_ID))
            return newMold;

        CompoundTag tag = new CompoundTag();
        if(StackTag.get(keyStack).contains(KeyItem.TAG_ID))
            tag.store(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC, StackTag.get(keyStack).read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).orElse(null));
        if (keyStack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
            tag.putString(TAG_NAME, com.lazrproductions.cuffed.utils.NicknameUtil.toJson(keyStack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME)));
        StackTag.update(newMold, t -> t.put(TAG_COPIED_KEY, tag));

        return newMold;
    }


    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemstack = new ItemStack(this);
        return itemstack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(stack, context, display, pTooltipComponents, pIsAdvanced);

        CompoundTag tag = StackTag.get(stack).getCompoundOrEmpty(TAG_COPIED_KEY);
        if (!tag.isEmpty()) {
            pTooltipComponents.accept(Component.translatable("item.cuffed.key_mold.description.bound").withStyle(ChatFormatting.GRAY));
        } else {
            pTooltipComponents.accept(Component.translatable("item.cuffed.key_mold.description.unbound").withStyle(ChatFormatting.GRAY));
        }
    }
}
