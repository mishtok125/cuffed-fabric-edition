package com.lazrproductions.cuffed.items;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.inventory.tooltip.TrayTooltip;
import com.lazrproductions.cuffed.utils.RegistryUtil;
import com.lazrproductions.cuffed.utils.StackTag;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * A food tray with four fixed slots: food, fork, spoon, knife.
 * The slots are stored as a 4-element list in the stack's custom data.
 */
public class TrayItem extends BlockItem {
    public static final String TAG_ITEMS = "Items";
    public static final int MAX_WEIGHT = 4;

    public TrayItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean overrideStackedOnOther(@Nonnull ItemStack stack, @Nonnull Slot slot,
            @Nonnull ClickAction clickAction, @Nonnull Player player) {
        if (stack.getCount() != 1 || clickAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = slot.getItem();
            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(stack).ifPresent(stackInSlot -> add(stack, slot.safeInsert(stackInSlot)));
            } else if (canFitInTray(stack, itemstack)) {
                int j = add(stack, slot.safeTake(itemstack.getCount(), 1, player));
                if (j > 0)
                    this.playInsertSound(player);
            }
            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(@Nonnull ItemStack stack, @Nonnull ItemStack otherStack,
            @Nonnull Slot slot, @Nonnull ClickAction clickAction, @Nonnull Player player,
            @Nonnull SlotAccess slotAccess) {
        if (stack.getCount() != 1)
            return false;
        if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                removeOne(stack).ifPresent(s -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(s);
                });
            } else {
                int i = add(stack, otherStack);
                if (i > 0) {
                    this.playInsertSound(player);
                    otherStack.shrink(i);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isCrouching() && dropContents(itemstack, player)) {
            this.playDropContentsSound(player);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    private static int add(@Nonnull ItemStack stack, @Nonnull ItemStack stackToAdd) {
        if (!stackToAdd.isEmpty() && canFitInTray(stack, stackToAdd)) {
            int slotToAddTo = -1;
            if (itemIsFood(stackToAdd))
                slotToAddTo = 0;
            if (itemIsFork(stackToAdd))
                slotToAddTo = 1;
            if (itemIsSpoon(stackToAdd))
                slotToAddTo = 2;
            if (itemIsKnife(stackToAdd))
                slotToAddTo = 3;

            if (slotToAddTo > -1) {
                NonNullList<ItemStack> contents = getContents(stack);
                if (contents.get(slotToAddTo).isEmpty()) {
                    contents.set(slotToAddTo, stackToAdd.copyWithCount(1));
                    setContents(stack, contents);
                    return 1;
                }
            }
        }
        return 0;
    }

    private static Optional<ItemStack> removeOne(ItemStack stack) {
        int toRemove = getNextStackIndex(stack);
        if (toRemove > -1) {
            NonNullList<ItemStack> contents = getContents(stack);
            ItemStack itemstack = contents.get(toRemove);
            contents.set(toRemove, ItemStack.EMPTY);
            setContents(stack, contents);
            return Optional.of(itemstack);
        }
        return Optional.empty();
    }

    private static boolean dropContents(ItemStack stack, Player player) {
        if (StackTag.get(stack).contains(TAG_ITEMS) && player instanceof ServerPlayer) {
            for (ItemStack itemstack : getContents(stack))
                if (!itemstack.isEmpty())
                    player.drop(itemstack, true);
            setContents(stack, NonNullList.withSize(MAX_WEIGHT, ItemStack.EMPTY));
            return true;
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        getContents(stack).forEach(nonnulllist::add);
        return Optional.of(new TrayTooltip.Data(nonnulllist));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.LANTERN_STEP, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.LANTERN_HIT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static NonNullList<ItemStack> getContents(@Nonnull ItemStack stack) {
        return getContents(StackTag.get(stack).getListOrEmpty(TAG_ITEMS));
    }

    public static NonNullList<ItemStack> getContents(@Nonnull ListTag listtag) {
        NonNullList<ItemStack> list = NonNullList.withSize(MAX_WEIGHT, ItemStack.EMPTY);
        for (int i = 0; i < MAX_WEIGHT && i < listtag.size(); i++) {
            Tag t = listtag.get(i);
            if (!(t instanceof CompoundTag c && c.isEmpty()))
                list.set(i, RegistryUtil.loadStack(t));
        }
        return list;
    }

    public static void setContents(@Nonnull ItemStack stack, NonNullList<ItemStack> items) {
        StackTag.update(stack, t -> t.put(TAG_ITEMS, saveItemToTagList(items)));
    }

    public static int getNextStackIndex(@Nonnull ItemStack stack) {
        var list = getContents(stack);
        for (int i = 0; i < list.size(); i++)
            if (!list.get(i).isEmpty())
                return i;
        return -1;
    }

    public static int getNextAvailableIndex(@Nonnull ItemStack stack) {
        var list = getContents(stack);
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).isEmpty())
                return i;
        return -1;
    }

    public static ListTag saveItemToTagList(NonNullList<ItemStack> items) {
        ListTag listtag = new ListTag();
        for (int i = 0; i < MAX_WEIGHT; i++) {
            ItemStack s = i < items.size() ? items.get(i) : ItemStack.EMPTY;
            listtag.add(s.isEmpty() ? new CompoundTag() : RegistryUtil.saveStack(s));
        }
        return listtag;
    }

    public static ItemStack createTrayFrom(NonNullList<ItemStack> stacks) {
        ItemStack stack = ModItems.TRAY.getDefaultInstance();
        setContents(stack, stacks);
        return stack;
    }

    private static boolean canFitInTray(@Nonnull ItemStack tray, @Nonnull ItemStack stack) {
        if (itemIsFood(stack))
            return !trayHasFoodItem(tray);
        else if (stack.is(ModItems.SPOON))
            return !trayHasSpoon(tray);
        else if (stack.is(ModItems.FORK))
            return !trayHasFork(tray);
        else if (stack.is(ModItems.KNIFE))
            return !trayHasKnife(tray);
        return false;
    }

    public static boolean trayHasFoodItem(@Nonnull ItemStack stack) {
        for (ItemStack itemStack : getContents(stack))
            if (itemIsFood(itemStack))
                return true;
        return false;
    }

    public static boolean trayHasSpoon(@Nonnull ItemStack stack) {
        for (ItemStack itemStack : getContents(stack))
            if (itemStack.is(ModItems.SPOON))
                return true;
        return false;
    }

    public static boolean trayHasFork(@Nonnull ItemStack stack) {
        for (ItemStack itemStack : getContents(stack))
            if (itemStack.is(ModItems.FORK))
                return true;
        return false;
    }

    public static boolean trayHasKnife(@Nonnull ItemStack stack) {
        for (ItemStack itemStack : getContents(stack))
            if (itemStack.is(ModItems.KNIFE))
                return true;
        return false;
    }

    public static boolean itemIsFood(@Nonnull ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    public static boolean itemIsSpoon(@Nonnull ItemStack stack) {
        return stack.is(ModItems.SPOON);
    }

    public static boolean itemIsFork(@Nonnull ItemStack stack) {
        return stack.is(ModItems.FORK);
    }

    public static boolean itemIsKnife(@Nonnull ItemStack stack) {
        return stack.is(ModItems.KNIFE);
    }
}
