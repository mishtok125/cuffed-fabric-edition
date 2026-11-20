package com.lazrproductions.cuffed.event;

import java.util.Random;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.cap.base.IRestrainableCapability;
import com.lazrproductions.cuffed.client.gui.screen.GenericScreen;
import com.lazrproductions.cuffed.effect.RestrainedEffectInstance;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.init.ModTags;
import com.lazrproductions.cuffed.restraints.custom.DuckTapeHeadRestraint;
import com.lazrproductions.lazrslib.client.screen.ScreenUtilities;

import com.lazrproductions.cuffed.items.base.AbstractRestraintItem;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Fabric port of the Forge client event subscriber. */
public class ModClientEvents {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(inst -> {
            if (inst.gui.screen() instanceof GenericScreen sc)
                sc.tick();
            if (inst.player != null) {
                IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(inst.player);
                cap.tickClient(inst.player);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null)
                CuffedAPI.Capabilities.getRestrainableCapability(client.player).onLoginClient(client.player);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client.player != null)
                CuffedAPI.Capabilities.getRestrainableCapability(client.player).onLogoutClient(client.player);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(CuffedMod.MODID, "restraints"),
                (graphics, deltaTracker) -> {
                    Minecraft inst = Minecraft.getInstance();
                    LocalPlayer p = inst.player;
                    if (p != null) {
                        IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(p);
                        cap.renderOverlay(p, graphics, deltaTracker.getGameTimeDeltaPartialTick(false),
                                inst.getWindow());
                        ScreenUtilities.resetColor();
                    }
                });

        // Former ItemMixin / BundleItemMixin tooltip additions.
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock().defaultBlockState().is(ModTags.Blocks.REINFORCED_BLOCKS))
                lines.add(Component.translatable("info.cuffed.reinforced_item").withStyle(ChatFormatting.GRAY));

            if (stack.is(Items.BUNDLE) && BundleItem.getFullnessDisplay(stack) <= 0) {
                lines.add(Component.translatable("info.cuffed.restraint_type.head").withStyle(ChatFormatting.GRAY));
                lines.add(Component.empty());
                lines.add(Component.translatable("info.cuffed.restraint_type.my_key").withStyle(ChatFormatting.GRAY)
                        .append(" ")
                        .append(Component.translatable("info.cuffed.empty_hand").withStyle(ChatFormatting.WHITE)));
                AbstractRestraintItem.Client.ShowExtendedInfo(lines::add);
            }
        });

        ClientSendMessageEvents.MODIFY_CHAT.register(message -> {
            Minecraft instance = Minecraft.getInstance();
            if (instance.player instanceof IRestrainableEntity e)
                if (e.getHeadRestraintId().equals(DuckTapeHeadRestraint.ID))
                    return mufflifyPhrase(message);
            return message;
        });
    }

    static final String[] variants = new String[] { "mph", "mhm", "hmm", "fmp", "mpr", "mrp" };

    static String mufflifyPhrase(String message) {
        String[] words = message.split(" ");
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            output.append(mufflifyWord(words[i]));
            if (i < words.length - 1)
                output.append(" ");
        }
        return output.toString();
    }

    static String mufflifyWord(String word) {
        String myVariant = variants[new Random().nextInt(3)];
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (i == word.length() - 1)
                    output.append(myVariant.charAt(2));
                else if (i == word.length() - 2)
                    output.append(myVariant.charAt(1));
                else
                    output.append(myVariant.charAt(0));
            } else
                output.append(c);
        }
        return output.toString();
    }

    ///////// MISC CLIENT HOOKS FOR HANDCUFFED PLAYERS (called from mixins) //////////

    /** Former ComputeFovModifierEvent handler. */
    public static boolean shouldLockFov(Player player) {
        IRestrainableEntity e = (IRestrainableEntity) player;
        return RestrainedEffectInstance.decodeNoMovement(e.getRestraintCode());
    }

    /** Former ScreenEvent.Opening handler: restrained players cannot open their inventory. */
    public static boolean shouldBlockScreen(Object newScreen) {
        if (newScreen instanceof InventoryScreen) {
            Player player = Minecraft.getInstance().player;
            if (player instanceof IRestrainableEntity restrainable)
                return RestrainedEffectInstance.decodeNoItemUse(restrainable.getRestraintCode());
        }
        return false;
    }

    /** Former InteractionKeyMappingTriggered handler. */
    public static boolean shouldBlockInteraction(boolean isAttack) {
        Minecraft inst = Minecraft.getInstance();
        Player player = inst.player;
        if (player == null)
            return false;
        IRestrainableEntity restrainable = (IRestrainableEntity) player;
        if (restrainable.isRestrained()) {
            if (!isAttack) {
                if (RestrainedEffectInstance.decodeNoItemUse(restrainable.getRestraintCode()))
                    return true;
            } else if (RestrainedEffectInstance.decodeNoMining(restrainable.getRestraintCode()))
                return true;
        }

        ItemStack handItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!player.isCreative() && !handItem.is(ItemTags.PICKAXES) && isAttack) {
            BlockState pickresult = GetSelectedBlock(player, false);
            if (pickresult != null && pickresult.is(ModTags.Blocks.REINFORCED_BLOCKS)
                    && !(inst.hitResult instanceof EntityHitResult))
                return true;
        }
        return false;
    }

    ////////// UTILITY FUNCTIONS FOR GENERAL USE /////////////

    public static BlockState GetSelectedBlock(Player player, boolean isFluid) {
        HitResult block = player.pick(20.0D, 0.0F, isFluid);
        if (block.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) block).getBlockPos();
            return player.level().getBlockState(blockpos);
        }
        return null;
    }

    public static BlockPos GetSelectedBlockPos(Player player, boolean isFluid) {
        HitResult block = player.pick(20.0D, 0.0F, isFluid);
        if (block.getType() == HitResult.Type.BLOCK)
            return ((BlockHitResult) block).getBlockPos();
        return null;
    }

    public static boolean isTargettingEntity(Player player, boolean isFluid) {
        HitResult block = player.pick(20.0D, 0.0F, isFluid);
        return block.getType() == HitResult.Type.ENTITY;
    }

    public static boolean isLocalPlayer(Player player) {
        Minecraft inst = Minecraft.getInstance();
        if (player == null)
            return false;
        LocalPlayer localPlayer = inst.player;
        if (localPlayer == null)
            return false;
        return player.getUUID().equals(localPlayer.getUUID());
    }
}
