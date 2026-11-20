package com.lazrproductions.cuffed.restraints.base;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.entity.animation.ArmRestraintAnimationFlags;
import com.lazrproductions.cuffed.entity.animation.LegRestraintAnimationFlags;
import com.lazrproductions.cuffed.init.ModEnchantments;
import com.lazrproductions.cuffed.init.ModStatistics;
import com.lazrproductions.cuffed.restraints.client.RestraintModelInterface;
import com.lazrproductions.cuffed.utils.RegistryUtil;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class AbstractRestraint {

    private ServerPlayer s_player;
    private Player c_player;
    private Tag itemData = new CompoundTag();
    /** Enchantment id -> level, see {@link IEnchantableRestraint}. */
    protected CompoundTag enchantments = new CompoundTag();
    @Nonnull
    private UUID captor;

    public AbstractRestraint() {
        captor = UUID.randomUUID();
    }

    public AbstractRestraint(ItemStack stack, ServerPlayer player, @Nullable ServerPlayer captor) {
        this.enchantments = writeEnchantments(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        ItemStack copy = stack.copyWithCount(1);
        this.itemData = RegistryUtil.saveStack(copy);
        this.captor = captor != null ? captor.getUUID() : player.getUUID();
    }

    static CompoundTag writeEnchantments(ItemEnchantments enchants) {
        CompoundTag tag = new CompoundTag();
        for (var entry : enchants.entrySet())
            entry.getKey().unwrapKey().ifPresent(k -> tag.putInt(k.identifier().toString(), entry.getIntValue()));
        return tag;
    }

    // #region Restraint Properties

    public abstract Identifier getId();

    public abstract RestraintType getType();

    public abstract String getActionBarLabel();

    public abstract String getName();

    public abstract Item getItem();

    public abstract Item getKeyItem();

    public abstract boolean AllowItemUse();

    public abstract boolean AllowBreakingBlocks();

    public abstract boolean AllowMovement();

    public abstract boolean AllowJumping();

    public abstract boolean AllowSprinting();

    public abstract SoundEvent getEquipSound();

    public abstract SoundEvent getUnequipSound();

    public abstract boolean getLockpickable();

    public abstract int getLockpickingSpeedIncreasePerPick();

    public abstract int getLockpickingProgressPerPick();

    public abstract ArrayList<Integer> getBlockedKeyCodes();

    public abstract ArmRestraintAnimationFlags getArmAnimationFlags();

    public abstract LegRestraintAnimationFlags getLegAnimationFlags();

    public boolean canEquipRestraintItem(ItemStack stack, ServerPlayer player, ServerPlayer captor) {
        return true;
    }

    // #endregion

    // #region Getters

    public Player getPlayer() {
        if (isClientSide())
            return c_player;
        return s_player;
    }

    @Nullable
    public Player getCaptor(@Nonnull ServerLevel level) {
        return level.getPlayerByUUID(captor);
    }

    public boolean isClientSide() {
        return s_player == null;
    }

    // #endregion

    // #region Enchantments (used by IEnchantableRestraint implementors)

    public CompoundTag getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(CompoundTag tag) {
        enchantments = tag != null ? tag : new CompoundTag();
    }

    int enchantLevel(ResourceKey<Enchantment> key) {
        if (this instanceof IEnchantableRestraint e)
            return e.getEnchantmentLevel(key);
        return 0;
    }

    // #endregion

    // #region Event Handling

    public void onTickServer(ServerPlayer player) {
        s_player = player;
        ModStatistics.awardTimeSpentRestrained(player, this);

        if (this instanceof IEnchantableRestraint) {
            if (enchantLevel(ModEnchantments.FAMINE) >= 1)
                if (!player.hasEffect(MobEffects.HUNGER))
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1));
            if (enchantLevel(ModEnchantments.SHROUD) >= 1)
                if (!player.hasEffect(MobEffects.BLINDNESS))
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1));
            if (enchantLevel(ModEnchantments.EXHAUST) >= 1) {
                if (!player.hasEffect(MobEffects.MINING_FATIGUE))
                    player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 1));
                if (!player.hasEffect(MobEffects.WEAKNESS))
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }
            // Silence only drained mana from Forge magic mods (Ars Nouveau, Iron's Spells, Mana and Artifice),
            // which have no Fabric 26.2 versions; the enchantment is kept so existing items still load.
        }
    }

    public void onTickClient(Player player) {
        c_player = player;
    }

    public void onEquippedServer(ServerPlayer player, @Nullable ServerPlayer captor) {
        s_player = player;
        if (captor != null)
            this.captor = captor.getUUID();
        ModStatistics.awardRestrained(player, this);

        Random random = new Random();
        player.level().playSound(null, player.blockPosition(), getEquipSound(), SoundSource.PLAYERS, 0.8f,
                (random.nextFloat() * 0.2f) + 0.9f);
    }

    public void onEquippedClient(Player player, Player captor) {
        c_player = player;
    }

    public void onUnequippedServer(ServerPlayer player) {
        Random random = new Random();
        player.level().playSound(null, player.blockPosition(), getUnequipSound(), SoundSource.PLAYERS, 0.8f,
                (random.nextFloat() * 0.2f) + 0.9f);
    }

    public void onUnequippedClient(Player player) {
    }

    public abstract void onLoginServer(ServerPlayer player);

    public abstract void onLoginClient(Player player);

    public abstract void onLogoutServer(ServerPlayer player);

    public abstract void onLogoutClient(Player player);

    public abstract void onDeathServer(ServerPlayer player);

    public abstract void onDeathClient(Player player);

    public abstract void onJumpServer(ServerPlayer player);

    public abstract void onJumpClient(Player player);

    public abstract float onLandServer(ServerPlayer player, float distance, float damageMultiplier);

    public abstract void onLandClient(Player player, float distance, float damageMultiplier);

    // #endregion

    // #region Client-Side Handles

    public abstract void renderOverlay(Player player, GuiGraphicsExtractor graphics, float partialTick, Window window);

    protected int clientSidedDurability = 100;

    public void onKeyInput(Player player, int keyCode, int action) {
        Minecraft instance = Minecraft.getInstance();
        if (action == 1) {
            if (this instanceof IBreakableRestraint breakable && breakable.canBeBrokenOutOf()) {
                breakable.attemptToBreak(player, keyCode, action, instance.options);
            }
        }
    }

    public void onMouseInput(Player player, int keyCode, int action) {
        Minecraft instance = Minecraft.getInstance();
        if (action == 1)
            if (this instanceof IBreakableRestraint breakable && breakable.canBeBrokenOutOf())
                breakable.attemptToBreak(player, keyCode, action, instance.options);
    }

    @Nonnull
    public abstract RestraintModelInterface getModelInterface();

    // #endregion

    // #region Server-Side Handles

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", getId().toString());
        tag.putString("Type", getType().toString());
        if (this instanceof IEnchantableRestraint)
            tag.put("Enchantments", enchantments.copy());
        tag.store("Captor", UUIDUtil.CODEC, captor);
        tag.put("ItemData", itemData.copy());
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        if (this instanceof IEnchantableRestraint)
            this.enchantments = nbt.getCompoundOrEmpty("Enchantments").copy();
        this.captor = nbt.read("Captor", UUIDUtil.CODEC).orElse(this.captor);
        Tag data = nbt.get("ItemData");
        this.itemData = data != null ? data.copy() : new CompoundTag();
    }

    public ItemStack saveToItemStack() {
        ItemStack stack = RegistryUtil.loadStack(itemData);
        if (stack.isEmpty())
            stack = new ItemStack(getItem());
        stack.setCount(1);
        if (this instanceof IBreakableRestraint breakable && stack.isDamageableItem())
            stack.setDamageValue(breakable.getMaxDurability() - breakable.getDurability());
        if (this instanceof IEnchantableRestraint) {
            HolderLookup.Provider registries = RegistryUtil.get();
            if (registries != null) {
                var lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
                ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                for (String key : enchantments.keySet()) {
                    Identifier id = Identifier.tryParse(key);
                    if (id == null)
                        continue;
                    var holder = lookup.get(ResourceKey.create(Registries.ENCHANTMENT, id));
                    holder.ifPresent((Holder<Enchantment> h) -> mutable.set(h, enchantments.getIntOr(key, 1)));
                }
                EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
            }
        }
        return stack;
    }

    /*
     * UTILITY PACKET CODEX
     * 101 -> set clientside durability (to client)
     * 102 -> increment durability (to server)
     * 103 -> on broken (to client)
     */

    public void receiveUtilityPacketServer(ServerPlayer player, int utiltiyCode, int integerArg, boolean booleanArg,
            double doubleArg, String stringArg) {
        if (utiltiyCode == 102) {
            if (this instanceof IBreakableRestraint breakable) {
                breakable.incrementDurability(player, integerArg);
            }
        }
    }

    public void receiveUtilityPacketClient(Player player, int utiltiyCode, int integerArg, boolean booleanArg,
            double doubleArg, String stringArg) {
        if (utiltiyCode == 101) {
            this.clientSidedDurability = integerArg;
        } else if (utiltiyCode == 103) {
            if (this instanceof IBreakableRestraint breakable)
                breakable.onBrokenClient(player);
        }
    }

    // #endregion
}
