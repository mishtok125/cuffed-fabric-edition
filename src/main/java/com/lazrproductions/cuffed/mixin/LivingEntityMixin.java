package com.lazrproductions.cuffed.mixin;

import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.cap.base.IRestrainableCapability;
import com.lazrproductions.cuffed.effect.RestrainedEffectInstance;
import com.lazrproductions.cuffed.entity.base.IAnchorableEntity;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.init.ModDamageTypes;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IAnchorableEntity {
    @Unique
    private static final EntityDataAccessor<Integer> DATA_ANCHOR_ID = SynchedEntityData.defineId(LivingEntity.class,
            EntityDataSerializers.INT);
    @Unique
    private static final String ANCHOR_TAG = "Anchor";

    @Unique
    @Nullable
    private Entity anchor = null;
    @Unique
    private boolean shouldBeLoaded = false;
    @Unique
    private UUID uuidToBeLoaded;
    @Unique
    boolean wasAnchored;

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isAnchored() {
        return getAnchor() != null;
    }

    @Override
    public Entity getAnchor() {
        return anchor;
    }

    @Override
    public Entity getAnchorClientSide() {
        return level().getEntity(entityData.get(DATA_ANCHOR_ID));
    }

    @Override
    public void setAnchoredTo(@Nullable Entity e) {
        if (!level().isClientSide()) {
            if (e == null) {
                setAnchor(null);
                wasAnchored = false;
                ItemStack stack = new ItemStack(Items.IRON_CHAIN, 1);
                ItemEntity en = new ItemEntity(level(), this.position().x(), this.position().y(),
                        this.position().z(), stack);
                en.setDefaultPickUpDelay();
                level().addFreshEntity(en);
            } else
                setAnchor(e);
        }
    }

    @Override
    public void setAnchor(@Nullable Entity e) {
        anchor = e;
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void cuffed$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo info) {
        builder.define(DATA_ANCHOR_ID, -1);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void cuffed$tick(CallbackInfo callback) {
        LivingEntity thisEntity = (LivingEntity) (Object) this;
        if (level() instanceof ServerLevel serverLevel) {
            if (shouldBeLoaded) {
                Entity a = serverLevel.getEntity(uuidToBeLoaded);
                setAnchor(a);
                if (a != null)
                    shouldBeLoaded = false;
            }

            if ((anchor == null || anchor.isRemoved()) && wasAnchored) {
                setAnchoredTo(null);
                wasAnchored = false;
            }

            if (anchor != null) {
                wasAnchored = true;
                entityData.set(DATA_ANCHOR_ID, anchor.getId());
                double minDist = CuffedMod.SERVER_CONFIG.ANCHORING_MAX_CHAIN_LENGTH.get();
                double maxDist = CuffedMod.SERVER_CONFIG.ANCHORING_SUFFOCATION_LENGTH.get();
                float distance = distanceTo(anchor);
                if (distance > minDist) {
                    double dx = (getAnchor().getX() - getX()) / (double) distance;
                    double dy = (getAnchor().getY() - getY()) / (double) distance;
                    double dz = (getAnchor().getZ() - getZ()) / (double) distance;

                    setDeltaMovement(
                            Math.copySign(dx * dx * (distance / 5D) * .45, dx),
                            Math.copySign(dy * dy * (distance / 5D) * .45, dy),
                            Math.copySign(dz * dz * (distance / 5D) * .45, dz));

                    boolean hanging = fallDistance > 0.2F && anchor.getY() - getY() > minDist;
                    if (hanging || distance > maxDist) {
                        thisEntity.hurtServer(serverLevel, ModDamageTypes.GetModSource(this, ModDamageTypes.HANG, anchor), 2);
                    }
                }
            } else
                entityData.set(DATA_ANCHOR_ID, -1);
        } else if (thisEntity instanceof Player player && getAnchorClientSide() != null) {
            double minDist = CuffedMod.SERVER_CONFIG.ANCHORING_MAX_CHAIN_LENGTH.get();
            if (this.distanceTo(getAnchorClientSide()) > minDist) {
                float distance = distanceTo(getAnchorClientSide());
                double dx = (getAnchorClientSide().getX() - getX()) / (double) distance;
                double dy = (getAnchorClientSide().getY() - getY()) / (double) distance;
                double dz = (getAnchorClientSide().getZ() - getZ()) / (double) distance;

                player.setDeltaMovement(
                        Math.copySign(dx * dx * (distance / 5D) * .45, dx),
                        Math.copySign(dy * dy * (distance / 5D) * .45, dy),
                        Math.copySign(dz * dz * (distance / 5D) * .45, dz));
            }
        }
    }

    /** Former LivingJumpEvent handlers (server and client). */
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void cuffed$jumpFromGround(CallbackInfo callback) {
        if (!((Object) this instanceof Player player))
            return;
        IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(player);
        if (player instanceof ServerPlayer serverPlayer) {
            cap.onJumpServer(serverPlayer);
        } else {
            cap.onJumpClient(player);
            IRestrainableEntity res = (IRestrainableEntity) player;
            if (RestrainedEffectInstance.decodeNoJumping(res.getRestraintCode())) {
                if (player.isSprinting() && RestrainedEffectInstance.decodeNoMovement(res.getRestraintCode()))
                    player.setDeltaMovement(0, -1, 0);
                else
                    player.setDeltaMovement(player.getDeltaMovement().x, -1, player.getDeltaMovement().z);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void cuffed$addAdditionalSaveData(ValueOutput output, CallbackInfo info) {
        if (!level().isClientSide() && isAnchored())
            output.store(ANCHOR_TAG, UUIDUtil.CODEC, getAnchor().getUUID());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void cuffed$readAdditionalSaveData(ValueInput input, CallbackInfo info) {
        input.read(ANCHOR_TAG, UUIDUtil.CODEC).ifPresentOrElse(uuid -> {
            uuidToBeLoaded = uuid;
            shouldBeLoaded = true;
        }, () -> setAnchor(null));
    }
}
