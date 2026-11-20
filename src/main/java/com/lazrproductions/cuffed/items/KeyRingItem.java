package com.lazrproductions.cuffed.items;


import com.lazrproductions.cuffed.utils.StackTag;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.blocks.CellDoor;
import com.lazrproductions.cuffed.blocks.SafeBlock;
import com.lazrproductions.cuffed.blocks.entity.LockableBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.SafeBlockEntity;
import com.lazrproductions.cuffed.init.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class KeyRingItem extends Item {

    public static final String TAG_BOUND_LOCKS = "BoundLocks";
    public static final String TAG_KEYS = "Keys";

    public KeyRingItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.FAIL;

        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (player != null) {
            ItemStack stack = player.getItemInHand(context.getHand());
            if (!level.isClientSide() && context.getHand() == InteractionHand.MAIN_HAND) {
                if (canBindLock(stack)) {
                    if (state.getBlock() instanceof CellDoor) {
                        BlockPos bottomPos = context.getClickedPos();
                        if (state.getValue(CellDoor.HALF) == DoubleBlockHalf.UPPER) {
                            bottomPos = bottomPos.below();
                            state = level.getBlockState(bottomPos);
                        }

                        if (level.getBlockEntity(bottomPos) instanceof LockableBlockEntity lockable) {
                            if(!lockable.hasBeenBound()) {
                                if (tryToAddBoundId(player, stack, lockable.getLockId(), "block.cuffed.cell_door")) {
                                    lockable.bind();
                                    player.awardStat(Stats.ITEM_USED.get(ModItems.KEY), 1);
                                    return InteractionResult.SUCCESS;
                                } else
                                    return InteractionResult.FAIL;
                            }
                        }
                    } else if (level.getBlockEntity(context.getClickedPos()) instanceof LockableBlockEntity lockable) {
                        if(!lockable.hasBeenBound()) {
                            if(tryToAddBoundId(player, stack, lockable.getLockId(), lockable.getLockName())) {
                                lockable.bind();
                                player.awardStat(Stats.ITEM_USED.get(ModItems.KEY_RING), 1);
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }else if (state.getBlock() instanceof SafeBlock) {
                        if (level.getBlockEntity(context.getClickedPos()) instanceof SafeBlockEntity safe) {
                            if(!safe.hasBeenBound()) {
                                if(tryToAddBoundId(player, stack, safe.getLockId(), "block.cuffed.safe")) {
                                    safe.bind();
                                    player.awardStat(Stats.ITEM_USED.get(ModItems.KEY_RING), 1);
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                }
            }
        }

        return InteractionResult.FAIL;
    }

    public static void addBoundId(ItemStack stack, UUID id) {
        CompoundTag compoundtag = StackTag.get(stack);
        ListTag listtag;
        if (compoundtag.contains(TAG_BOUND_LOCKS))
            listtag = compoundtag.getListOrEmpty(TAG_BOUND_LOCKS);
        else
            listtag = new ListTag();

        CompoundTag compoundtag1 = new CompoundTag();
        compoundtag1.store(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC, id);
        listtag.add(compoundtag1);
        compoundtag.put(TAG_BOUND_LOCKS, listtag);
        StackTag.set(stack, compoundtag);    }

    public static void addKey(ItemStack stack, ItemStack key) {
        CompoundTag compoundtag = StackTag.get(stack);
        ListTag listtag;
        if (compoundtag.contains(TAG_BOUND_LOCKS))
            listtag = compoundtag.getListOrEmpty(TAG_BOUND_LOCKS);
        else
            listtag = new ListTag();

        CompoundTag compoundtag1 = new CompoundTag();
        compoundtag1.store(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC, StackTag.get(key).read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).orElse(null));
        if (key.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
            compoundtag1.putString(KeyMoldItem.TAG_NAME, com.lazrproductions.cuffed.utils.NicknameUtil.toJson(key.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME)));
        listtag.add(compoundtag1);
        compoundtag.put(TAG_BOUND_LOCKS, listtag);
        StackTag.set(stack, compoundtag);    }

    public static boolean tryToAddBoundId(Player player, ItemStack stack, UUID id, String lockName) {
        if (canBindLock(stack)) {
            if (!hasBoundId(stack, id)) {
                addBoundId(stack, id);
                if ((player.level() instanceof net.minecraft.server.level.ServerLevel cuffedSl && cuffedSl.getGameRules().get(GameRules.REDUCED_DEBUG_INFO)))
                    player.sendSystemMessage(
                            Component.translatable("item.cuffed.key.info.bound").append(Component.literal(""+id)));
                else
                    player.sendSystemMessage(Component.translatable("item.cuffed.key.info.bound").append(Component.translatable(lockName)));
                player.playSound(SoundEvents.CHAIN_FALL, 1.0F, 1.0F);
                return true;
            }
        }
        
        return false;
    }

    public static void removeBoundId(ItemStack stack, UUID id) {
        CompoundTag compoundtag = StackTag.get(stack);
        ListTag listtag;
        if (compoundtag.contains(TAG_BOUND_LOCKS)) {
            listtag = compoundtag.getListOrEmpty(TAG_BOUND_LOCKS);
        } else {
            listtag = new ListTag();
        }

        int index = getBoundIdIndex(stack, id);
        if (index >= 0)
            listtag.remove(index);
        compoundtag.put(TAG_BOUND_LOCKS, listtag);
        StackTag.set(stack, compoundtag);    }

    public static boolean hasBoundId(ItemStack stack, UUID id) {
        CompoundTag compoundTag = StackTag.get(stack);
        if (compoundTag == null)
            return false;

        if (compoundTag.contains(TAG_BOUND_LOCKS)) {
            ListTag boundPos = compoundTag.getListOrEmpty(TAG_BOUND_LOCKS);
            for (int i = 0; i < boundPos.size(); i++)
                if (id.equals(boundPos.getCompoundOrEmpty(i).read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).orElse(null)))
                    return true;
        }
        return false;
    }

    public static int getBoundIdIndex(ItemStack stack, UUID id) {
        CompoundTag compoundTag = StackTag.get(stack);
        if (compoundTag == null)
            return -1;

        if (compoundTag.contains(TAG_BOUND_LOCKS)) {
            ListTag boundPos = compoundTag.getListOrEmpty(TAG_BOUND_LOCKS);
            for (int i = 0; i < boundPos.size(); i++)
                if (id.equals(boundPos.getCompoundOrEmpty(i).read(KeyItem.TAG_ID, net.minecraft.core.UUIDUtil.CODEC).orElse(null)))
                    return i;
        }
        return -1;
    }

    public static boolean canBindLock(ItemStack stack) {
        CompoundTag compoundTag = StackTag.get(stack);
        if (compoundTag == null)
            return true;

        int bindings = 0;
        int keys = 0;
        if (compoundTag.contains(TAG_BOUND_LOCKS)) {
            ListTag boundPos = compoundTag.getListOrEmpty(TAG_BOUND_LOCKS);

            bindings = boundPos.size();

            var tag = StackTag.get(stack);
            if (tag != null && tag.contains(TAG_KEYS))
                keys = tag.getIntOr(TAG_KEYS, 0);
        } else {
            return true;
        }

        if (bindings < keys)
            return true;
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(stack, context, display, pTooltipComponents, pIsAdvanced);

        int amount = 0;
        var tag = StackTag.get(stack);
        if (tag != null && tag.contains(TAG_KEYS))
            amount = tag.getIntOr(TAG_KEYS, 0);

        pTooltipComponents.accept(Component.translatable("item.cuffed.key_ring.description.amount", amount)
                .withStyle(ChatFormatting.GRAY));

        int bindings = 0;
        CompoundTag compoundTag = StackTag.get(stack);
        if (compoundTag != null) {
            if (compoundTag.contains(TAG_BOUND_LOCKS)) {
                ListTag boundPos = compoundTag.getListOrEmpty(TAG_BOUND_LOCKS);
                bindings = boundPos.size();
            }
        }
        if (bindings == amount)
            pTooltipComponents.accept(Component.translatable("item.cuffed.key_ring.description.amount", bindings)
                    .withStyle(ChatFormatting.GRAY));
        else
            pTooltipComponents.accept(Component.translatable("item.cuffed.key_ring.description.amount", bindings)
                    .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack itemstack = new ItemStack(this);
        StackTag.update(itemstack, t -> t.putInt(TAG_KEYS, 2));
        return itemstack;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity,
            @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (!StackTag.has(stack))
            StackTag.update(stack, t -> t.putInt(TAG_KEYS, 1));
        super.inventoryTick(stack, level, entity, slot);
    }
}
