package com.lazrproductions.cuffed.items;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModMenuTypes;
import com.lazrproductions.cuffed.inventory.FriskingContainer;
import com.lazrproductions.cuffed.inventory.FriskingMenu;
import com.lazrproductions.cuffed.inventory.tooltip.PossessionsBoxTooltip;
import com.lazrproductions.cuffed.utils.RegistryUtil;
import com.lazrproductions.cuffed.utils.StackTag;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** A box holding a captive's confiscated items. Items are stored, newest first, in the stack's custom data. */
public class PossessionsBox extends Item {

    public final static String TAG_ITEMS = "Items";

    public PossessionsBox(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(@Nonnull ItemStack thisStack, @Nonnull Slot slot, @Nonnull ClickAction click,
            @Nonnull Player player) {
        if (thisStack.getCount() != 1 || click != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack otherStack = slot.getItem();
            if (otherStack.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(thisStack).ifPresent(removed -> add(thisStack, slot.safeInsert(removed)));
            }
            return true;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (dropContents(itemstack, player) && player.isCrouching()) {
            this.playDropContentsSound(player);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public static ItemStack add(ItemStack stack, ItemStack stackToAdd) {
        if (!stackToAdd.isEmpty() && stackToAdd.getCount() > 0) {
            CompoundTag compoundtag = StackTag.get(stack);
            ListTag listtag = compoundtag.getListOrEmpty(TAG_ITEMS);
            listtag.add(0, RegistryUtil.saveStack(stackToAdd.copy()));
            compoundtag.put(TAG_ITEMS, listtag);
            StackTag.set(stack, compoundtag);
        }
        return stackToAdd;
    }

    private static Optional<ItemStack> removeOne(ItemStack stack) {
        CompoundTag compoundtag = StackTag.get(stack);
        ListTag listtag = compoundtag.getListOrEmpty(TAG_ITEMS);
        if (listtag.isEmpty())
            return Optional.empty();

        ItemStack itemstack = RegistryUtil.loadStack(listtag.get(0));
        listtag.remove(0);
        if (listtag.isEmpty())
            compoundtag.remove(TAG_ITEMS);
        else
            compoundtag.put(TAG_ITEMS, listtag);
        StackTag.set(stack, compoundtag);
        return Optional.of(itemstack);
    }

    private static boolean dropContents(ItemStack stack, Player player) {
        if (!player.isCrouching())
            return false;
        CompoundTag compoundtag = StackTag.get(stack);
        if (!compoundtag.contains(TAG_ITEMS))
            return false;

        if (player instanceof ServerPlayer) {
            ListTag listtag = compoundtag.getListOrEmpty(TAG_ITEMS);
            for (Tag t : listtag) {
                ItemStack itemstack = RegistryUtil.loadStack(t);
                if (!itemstack.isEmpty())
                    player.drop(itemstack, true);
            }
        }
        StackTag.update(stack, t -> t.remove(TAG_ITEMS));
        return true;
    }

    public static Stream<ItemStack> getContents(ItemStack stack) {
        ListTag listtag = StackTag.get(stack).getListOrEmpty(TAG_ITEMS);
        return listtag.stream().map(RegistryUtil::loadStack).filter(s -> !s.isEmpty());
    }

    @Override
    public Component getName(@Nonnull ItemStack stack) {
        ListTag listtag = StackTag.get(stack).getListOrEmpty(TAG_ITEMS);
        if (!listtag.isEmpty())
            return Component.translatable(this.getDescriptionId() + ".full");
        return Component.translatable(this.getDescriptionId() + ".empty");
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        getContents(stack).forEach(nonnulllist::add);
        if (nonnulllist.size() > 0)
            return Optional.of(new PossessionsBoxTooltip.Data(nonnulllist));
        return Optional.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> component, TooltipFlag flag) {
        ListTag listtag = StackTag.get(stack).getListOrEmpty(TAG_ITEMS);
        if (listtag.isEmpty()) {
            component.accept(Component.translatable("item.cuffed.possessions_box.lore.empty").withStyle(ChatFormatting.GRAY));
        } else {
            component.accept(Component.translatable("item.cuffed.possessions_box.lore.full", listtag.size())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void onDestroyed(@Nonnull ItemEntity entity) {
        ItemUtils.onContainerDestroyed(entity, getContents(entity.getItem()));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    public static void frisk(@Nonnull ServerPlayer frisker, @Nonnull ServerPlayer player, @Nonnull ItemStack boxStack) {
        frisker.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return player.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, @Nonnull Inventory playerInventory, @Nonnull Player p) {
                return new FriskingMenu(ModMenuTypes.FRISKING_MENU, id, playerInventory, player.getId(),
                        new FriskingContainer(player, boxStack), 5);
            }
        });
    }
}
