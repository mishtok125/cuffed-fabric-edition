package com.lazrproductions.cuffed.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.blocks.PilloryBlock;
import com.lazrproductions.cuffed.blocks.base.ILockableBlock;
import com.lazrproductions.cuffed.cap.RestrainableCapability;
import com.lazrproductions.cuffed.cap.base.IRestrainableCapability;
import com.lazrproductions.cuffed.cap.base.IRestrainableHolder;
import com.lazrproductions.cuffed.entity.ChainKnotEntity;
import com.lazrproductions.cuffed.entity.CrumblingBlockEntity;
import com.lazrproductions.cuffed.entity.base.IAnchorableEntity;
import com.lazrproductions.cuffed.entity.base.IDetainableEntity;
import com.lazrproductions.cuffed.entity.base.INicknamable;
import com.lazrproductions.cuffed.init.ModBlocks;
import com.lazrproductions.cuffed.init.ModEnchantments;
import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModTags;
import com.lazrproductions.cuffed.items.PossessionsBox;
import com.lazrproductions.cuffed.restraints.base.AbstractArmRestraint;
import com.lazrproductions.cuffed.restraints.base.AbstractLegRestraint;
import com.lazrproductions.cuffed.restraints.base.IEnchantableRestraint;

import com.lazrproductions.cuffed.blocks.BunkBlock;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.util.EventResult;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Fabric port of the Forge server event subscriber. */
public class ModServerEvents {

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register(ModServerEvents::onPlayerCloned);
        ServerTickEvents.END_SERVER_TICK.register(ModServerEvents::tickServer);
        PlayerBlockBreakEvents.BEFORE.register(ModServerEvents::playerMineBlock);
        UseBlockCallback.EVENT.register(ModServerEvents::playerInteractBlock);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> playerInteractEntity(player, level,
                hand, entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.getPlayer();
            CuffedAPI.Capabilities.getRestrainableCapability(p).onLoginServer(p);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer p = handler.getPlayer();
            CuffedAPI.Capabilities.getRestrainableCapability(p).onLogoutServer(p);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> onEntityDied(entity));

        // Bunks are beds (Forge used IForgeBlock#isBed for this).
        EntitySleepEvents.ALLOW_BED.register((entity, sleepingPos, state, vanillaResult) -> state
                .getBlock() instanceof BunkBlock ? EventResult.ALLOW : EventResult.PASS);
        EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.register((entity, sleepingPos, direction) -> {
            Direction bunk = BunkBlock.getBedOrientation(entity.level(), sleepingPos);
            return bunk != null ? bunk : direction;
        });
        EntitySleepEvents.SET_BED_OCCUPATION_STATE.register((entity, sleepingPos, bedState, occupied) -> {
            if (bedState.getBlock() instanceof BunkBlock) {
                entity.level().setBlock(sleepingPos, bedState.setValue(BunkBlock.OCCUPIED, occupied), 3);
                return true;
            }
            return false;
        });
    }

    static void onPlayerCloned(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        // Restraints survive respawning (minus whatever onDeathServer removed) and returning from the End.
        RestrainableCapability oldCap = ((IRestrainableHolder) oldPlayer).cuffed$getRestrainable();
        RestrainableCapability newCap = ((IRestrainableHolder) newPlayer).cuffed$getRestrainable();
        newCap.copyFrom(oldCap.serializeNBT(), (ServerLevel) newPlayer.level());

        if (alive || CuffedMod.SERVER_CONFIG.NICKNAME_PERSISTS_ON_DEATH.get()) {
            INicknamable oldNick = (INicknamable) oldPlayer;
            INicknamable newNick = (INicknamable) newPlayer;
            newNick.setNickname(oldNick.getNickname());
        }
    }

    static void tickServer(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(p);
            cap.tickServer(p);
        }
    }

    static boolean playerMineBlock(Level level, Player player, BlockPos pos, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (state.is(ModTags.Blocks.REINFORCED_BLOCKS))
            if (!player.isCreative() && !player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.PICKAXES))
                return false;

        IDetainableEntity detainableEntity = (IDetainableEntity) player;
        if (detainableEntity.getDetained() > -1)
            return false;

        return !CuffedAPI.Lockpicking.isLockedAt(level, state, pos);
    }

    static InteractionResult playerInteractBlock(Player interacting, Level level, InteractionHand hand,
            BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND || !(level instanceof ServerLevel server))
            return InteractionResult.PASS;

        IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(interacting);
        IDetainableEntity interactingAsDetainable = (IDetainableEntity) interacting;
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (interactingAsDetainable.getDetained() > -1)
            return InteractionResult.FAIL;

        ArrayList<IAnchorableEntity> entitiesAnchoredToInteractor = new ArrayList<IAnchorableEntity>(0);
        for (Entity e : server.getAllEntities()) {
            if (e instanceof IAnchorableEntity en)
                if (en.isAnchored() && en.getAnchor().getUUID().equals(interacting.getUUID()))
                    entitiesAnchoredToInteractor.add(en);
        }

        if (((state.is(BlockTags.FENCES) && CuffedMod.SERVER_CONFIG.ANCHORING_ALLOW_ANCHORING_TO_FENCES.get())
                || (state.is(Blocks.TRIPWIRE_HOOK)
                        && CuffedMod.SERVER_CONFIG.ANCHORING_ALLOW_ANCHORING_TO_TRIPWIRE_HOOKS.get()))
                && entitiesAnchoredToInteractor.size() > 0) {
            for (IAnchorableEntity anchored : entitiesAnchoredToInteractor)
                ChainKnotEntity.bindEntityToNewOrExistingKnot((LivingEntity) anchored, level, pos);
            return InteractionResult.SUCCESS;
        }

        if (state.is(ModBlocks.PILLORY)) {
            ServerPlayer whoIWasEscorting = cap.getWhoImEscorting();
            if (whoIWasEscorting != null) {
                if (level.getBlockState(pos.above()).is(ModBlocks.PILLORY)) {
                    pos = pos.above();
                    state = level.getBlockState(pos);
                }
                var p = PilloryBlock.getPositionOnPlayerSide(state, pos, whoIWasEscorting.position());
                if (!RestrainableCapability.canMoveEscortedPlayer(whoIWasEscorting, p))
                    return InteractionResult.FAIL;
                cap.stopEscortingPlayer();
                whoIWasEscorting.teleportTo(p.x(), p.y(), p.z());
                return InteractionResult.SUCCESS;
            }
        }

        if (state.is(ModBlocks.BUNK)) {
            if (cap.getWhoImEscorting() != null) {
                state.useWithoutItem(level, cap.getWhoImEscorting(), hitResult);
                return InteractionResult.SUCCESS;
            }
        }

        if (state.is(ModTags.Blocks.REINFORCED_BLOCKS) && Block.isShapeFullBlock(state.getShape(level, pos))) {
            ItemStack stack = interacting.getItemInHand(hand);
            if (stack.is(ModItems.FORK) || stack.is(ModItems.SPOON)) {
                Random r = new Random();
                if (r.nextFloat() < 0.25f)
                    CrumblingBlockEntity.crumbleBlock(level, pos, state, 1);

                level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 1f,
                        (r.nextFloat() * 0.2f) + 0.9f);
                level.levelEvent(null, 2001, pos, Block.getId(state));
                stack.hurtAndBreak(1, interacting, hand);
                return InteractionResult.SUCCESS;
            }
        }

        if (CuffedAPI.Lockpicking.isLockedAt(level, state, pos) && !(state.getBlock() instanceof ILockableBlock))
            return InteractionResult.FAIL;

        return InteractionResult.PASS;
    }

    static InteractionResult playerInteractEntity(Player p, Level level, InteractionHand hand, Entity targetEntity) {
        if (hand != InteractionHand.MAIN_HAND || level.isClientSide() || !(p instanceof ServerPlayer player))
            return InteractionResult.PASS;

        IRestrainableCapability myCap = CuffedAPI.Capabilities.getRestrainableCapability(player);
        IDetainableEntity detainableEntity = (IDetainableEntity) player;
        ItemStack itemStack = player.getItemInHand(hand);

        if (detainableEntity.getDetained() > -1)
            return InteractionResult.FAIL;

        if (targetEntity instanceof ServerPlayer target) {
            IRestrainableCapability targetCap = CuffedAPI.Capabilities.getRestrainableCapability(target);
            double maxDist = player.getEyePosition().distanceTo(target.position());
            Vec3 interactionPos = new Vec3(target.position().x,
                    player.getLookAngle().multiply(new Vec3(maxDist, maxDist, maxDist)).add(player.getEyePosition()).y,
                    target.position().z);

            if (itemStack.is(ModItems.POSSESSIONSBOX) && targetCap.armsRestrained()) {
                PossessionsBox.frisk(player, target, itemStack);
                return InteractionResult.SUCCESS;
            }

            if (targetCap != null) {
                // The Forge version did not cancel the event here; returning PASS keeps that behaviour.
                targetCap.onInteractedByOther(target, player, interactionPos.y - target.position().y, itemStack,
                        hand, false);
            }
        }

        if (targetEntity.getType().builtInRegistryHolder().is(ModTags.Entities.CHAINABLE_ENTITIES)
                && targetEntity instanceof IAnchorableEntity anchorableEntity) {
            boolean allowed = true;
            if (CuffedMod.SERVER_CONFIG.ANCHORING_ANCHOR_ONLY_WHEN_RESTRAINED.get()) {
                if (targetEntity instanceof Player tp) {
                    IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(tp);
                    if (!cap.isRestrained())
                        allowed = false;
                }
            }
            if (allowed) {
                if (anchorableEntity.isAnchored()) {
                    if (itemStack.isEmpty()) {
                        anchorableEntity.setAnchoredTo(null);
                        level.playSound(null, targetEntity.blockPosition(), SoundEvents.CHAIN_BREAK,
                                SoundSource.PLAYERS, 0.7f, 1);
                        return InteractionResult.SUCCESS;
                    }
                } else if (itemStack.is(Items.IRON_CHAIN)) {
                    anchorableEntity.setAnchoredTo(player);
                    itemStack.shrink(1);
                    level.playSound(null, targetEntity.blockPosition(), SoundEvents.CHAIN_PLACE,
                            SoundSource.PLAYERS, 0.7f, 1);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        if (!(targetEntity instanceof Player) && myCap != null && myCap.getWhoImEscorting() != null) {
            if (myCap.getWhoImEscorting().startRiding(targetEntity)) {
                level.playSound(null, targetEntity, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1, 1);
                myCap.stopEscortingPlayer();
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    static void onEntityDied(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(player);
            if (cap != null)
                cap.onDeathServer(player);
        }

        if (entity instanceof IAnchorableEntity anchorable) {
            if (anchorable.isAnchored())
                anchorable.setAnchoredTo(null);
        }
    }

    /**
     * Imbue: part of the damage a captor takes is redirected to the players they restrained.
     * Called from PlayerMixin#actuallyHurt; returns the reduced damage.
     */
    public static float onCaptorDamaged(Player captor, ServerLevel level, float originalAmount) {
        MinecraftServer server = level.getServer();
        if (server == null || originalAmount <= 0)
            return originalAmount;

        ArrayList<Player> playersToTakeDamage = new ArrayList<>();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer other : players) {
            if (other == captor)
                continue;
            IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(other);
            AbstractArmRestraint arm = cap.getArmRestraint();
            AbstractLegRestraint leg = cap.getLegRestraint();
            if ((arm != null && arm.getCaptor(level) == captor) || (leg != null && leg.getCaptor(level) == captor))
                playersToTakeDamage.add(other);
        }
        if (playersToTakeDamage.isEmpty())
            return originalAmount;

        float amountNegated = 0;
        for (Player pl : playersToTakeDamage) {
            IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(pl);
            if (cap.armsRestrained() && cap.getArmRestraint() instanceof IEnchantableRestraint e
                    && e.hasEnchantment(ModEnchantments.IMBUE)) {
                int enchLevel = e.getEnchantmentLevel(ModEnchantments.IMBUE);
                amountNegated += originalAmount * (((float) enchLevel / 3f) * 0.8f);
            }
            if (cap.legsRestrained() && cap.getLegRestraint() instanceof IEnchantableRestraint e
                    && e.hasEnchantment(ModEnchantments.IMBUE)) {
                int enchLevel = e.getEnchantmentLevel(ModEnchantments.IMBUE);
                amountNegated += originalAmount * (((float) enchLevel / 3f) * 0.8f);
            }
        }
        if (amountNegated <= 0)
            return originalAmount;
        for (Player pl : playersToTakeDamage) {
            // each restrained player takes a share of the total damage negated by imbue
            if (pl.level() instanceof ServerLevel plLevel)
                pl.hurtServer(plLevel, captor.damageSources().magic(), amountNegated / (float) playersToTakeDamage.size());
        }
        return Mth.clamp(originalAmount - amountNegated, 0, originalAmount);
    }

    /** Called from CommandsMixin: players who cannot move cannot run commands (unless they are operators). */
    public static boolean shouldBlockCommand(ServerPlayer player) {
        RestrainableCapability cap = (RestrainableCapability) CuffedAPI.Capabilities.getRestrainableCapability(player);
        if (cap != null && cap.restraintsDisabledMovement()
                && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            player.sendSystemMessage(Component.literal("You cannot do this right now.").withStyle(ChatFormatting.RED));
            return true;
        }
        return false;
    }
}
