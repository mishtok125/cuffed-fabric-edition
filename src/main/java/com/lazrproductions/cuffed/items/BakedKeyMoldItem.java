package com.lazrproductions.cuffed.items;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.List;
import java.util.Random;

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

public class BakedKeyMoldItem extends Item {
    
    public static final String TAG_QUALITY = "Quality";

    public BakedKeyMoldItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createFromRawMold(ItemStack oldMold) {
        ItemStack newMold = new ItemStack(ModItems.BAKED_KEY_MOLD, 1);
        
        CompoundTag oldTag = StackTag.get(oldMold).getCompoundOrEmpty(KeyMoldItem.TAG_COPIED_KEY);
        CompoundTag tag = new CompoundTag();
        oldTag.read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).ifPresent(id -> tag.store(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC, id));
        tag.putString(KeyMoldItem.TAG_NAME, oldTag.getStringOr(KeyMoldItem.TAG_NAME, ""));
        
        StackTag.update(newMold, t -> t.put(KeyMoldItem.TAG_COPIED_KEY, tag));
        Random r = new Random();
        StackTag.update(newMold, t -> t.putInt(TAG_QUALITY, r.nextInt(1) + 4));

        return newMold;
    }

    public static ItemStack createKeyFrom(ItemStack moldStack, int amount) {
        ItemStack newKey = new ItemStack(ModItems.KEY, amount);

        if(!StackTag.get(moldStack).contains(KeyMoldItem.TAG_COPIED_KEY))
            return newKey;

        CompoundTag moldTag = StackTag.get(moldStack).getCompoundOrEmpty(KeyMoldItem.TAG_COPIED_KEY);
        moldTag.read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).ifPresent(id ->
                StackTag.update(newKey, t -> t.store(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC, id)));

        if (moldTag.contains(KeyMoldItem.TAG_NAME)) {
            Component name = com.lazrproductions.cuffed.utils.NicknameUtil.fromJson(moldTag.getStringOr(KeyMoldItem.TAG_NAME, ""));
            if (name != null)
                newKey.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, name);
        }

        return newKey;
    }


    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemstack = new ItemStack(this);
        StackTag.update(itemstack, t -> t.putInt(TAG_QUALITY, 5));
        return itemstack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(stack, context, display, pTooltipComponents, pIsAdvanced);

        int quality = StackTag.get(stack).getIntOr(TAG_QUALITY, 0);
        pTooltipComponents.accept(Component.translatable("item.cuffed.baked_key_mold.description.quality_"+quality).withStyle(ChatFormatting.DARK_GRAY));
    }
}
