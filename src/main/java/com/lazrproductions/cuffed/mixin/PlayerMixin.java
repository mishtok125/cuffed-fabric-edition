package com.lazrproductions.cuffed.mixin;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.blocks.PilloryBlock;
import com.lazrproductions.cuffed.blocks.base.DetentionBlock;
import com.lazrproductions.cuffed.cap.RestrainableCapability;
import com.lazrproductions.cuffed.cap.base.IRestrainableCapability;
import com.lazrproductions.cuffed.cap.base.IRestrainableHolder;
import com.lazrproductions.cuffed.entity.base.IDetainableEntity;
import com.lazrproductions.cuffed.entity.base.INicknamable;
import com.lazrproductions.cuffed.entity.base.IPrivacyOperand;
import com.lazrproductions.cuffed.entity.base.IRestrainableEntity;
import com.lazrproductions.cuffed.event.ModServerEvents;
import com.lazrproductions.cuffed.init.ModEffects;
import com.lazrproductions.cuffed.restraints.base.IEnchantableRestraint;
import com.lazrproductions.cuffed.utils.NicknameUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity
        implements IRestrainableEntity, IDetainableEntity, INicknamable, IPrivacyOperand, IRestrainableHolder {

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Unique
    private static final EntityDataAccessor<Boolean> DATA_HAS_NICKNAME = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Component> DATA_NICKNAME = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPONENT);
    @Unique
    private static final String NICKNAME_TAG = "Nickname";

    @Unique
    private static final EntityDataAccessor<Integer> DATA_RESTRAINT_CODE = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<String> DATA_ARM_RESTRAINT_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);
    @Unique
    private static final EntityDataAccessor<String> DATA_LEG_RESTRAINT_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);
    @Unique
    private static final EntityDataAccessor<String> DATA_HEAD_RESTRAINT_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);
    @Unique
    private static final EntityDataAccessor<Boolean> DATA_ARM_ENCHANTED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> DATA_LEG_ENCHANTED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> DATA_HEAD_ENCHANTED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Integer> DATA_DETAINED = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<BlockPos> DATA_DETAINED_TO_BLOCK = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BLOCK_POS);
    @Unique
    private static final EntityDataAccessor<Float> DATA_DETAINED_ROTATION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<org.joml.Vector3fc> DATA_DETAINED_POSITION = SynchedEntityData.defineId(Player.class, EntityDataSerializers.VECTOR3);

    @Unique
    private static final String DETAINED_TAG = "Detained";
    @Unique
    private static final String RESTRAINTS_TAG = "CuffedRestraints";

    @Unique
    private RestrainableCapability cuffed$restrainable = new RestrainableCapability();

    @Override
    public RestrainableCapability cuffed$getRestrainable() {
        return cuffed$restrainable;
    }

    @Override
    public void cuffed$setRestrainable(RestrainableCapability value) {
        cuffed$restrainable = value;
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    private void cuffed$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo callback) {
        builder.define(DATA_HAS_NICKNAME, false);
        builder.define(DATA_NICKNAME, Component.empty());
        builder.define(DATA_RESTRAINT_CODE, 0); // used for synching restraint proprties to clients.
        builder.define(DATA_ARM_RESTRAINT_ID, ""); // used for displaying arm restraint models
        builder.define(DATA_LEG_RESTRAINT_ID, ""); // used for displaying leg restraint models
        builder.define(DATA_HEAD_RESTRAINT_ID, ""); // used for displaying head restraint models
        builder.define(DATA_ARM_ENCHANTED, false);
        builder.define(DATA_LEG_ENCHANTED, false);
        builder.define(DATA_HEAD_ENCHANTED, false);
        builder.define(DATA_DETAINED, -1);
        builder.define(DATA_DETAINED_ROTATION, 0f);
        builder.define(DATA_DETAINED_TO_BLOCK, BlockPos.ZERO);
        builder.define(DATA_DETAINED_POSITION, new Vector3f());
    }

    @Inject(at = @At("HEAD"), method = "getName", cancellable = true)
    private void cuffed$getName(CallbackInfoReturnable<Component> callback) {
        if (entityData.get(DATA_HAS_NICKNAME)) {
            Component n = getNickname();
            if (n != null)
                callback.setReturnValue(n);
        }
    }

    @Inject(at = @At("HEAD"), method = "getDisplayName", cancellable = true)
    private void cuffed$getDisplayName(CallbackInfoReturnable<Component> callback) {
        if (entityData.get(DATA_HAS_NICKNAME)) {
            Component n = getNickname();
            if (n != null)
                callback.setReturnValue(n);
        }
    }

    @Unique
    float pilloryBreakOutProgress = 0;
    @Unique
    boolean wasCrouching;
    @Unique
    boolean hasProcessedSwing = false;

    @Inject(at = @At("TAIL"), method = "tick")
    private void cuffed$tick(CallbackInfo callback) {
        if (!this.level().isClientSide()) {
            RestrainableCapability cap = cuffed$restrainable;
            setArmRestraintId(cap.getArmRestraintId());
            setLegRestraintId(cap.getLegRestraintId());
            setHeadRestraintId(cap.getHeadRestraintId());
            setArmsEnchanted(cap.getArmRestraint() instanceof IEnchantableRestraint e && e.isEnchanted());
            setLegsEnchanted(cap.getLegRestraint() instanceof IEnchantableRestraint e && e.isEnchanted());
            setHeadEnchanted(cap.getHeadRestraint() instanceof IEnchantableRestraint e && e.isEnchanted());

            MobEffectInstance restrained = this.getEffect(ModEffects.RESTRAINED_EFFECT);
            setRestraintCode(restrained == null ? 0 : restrained.getAmplifier());

            if (getDetained() > -1) {
                this.setYBodyRot(getDetainedRotation());
                this.setYRot(getDetainedRotation());
                this.teleportTo(getDetainedPosition().x(), getDetainedPosition().y(), getDetainedPosition().z());

                if (this.isAlive()) {
                    if (!wasCrouching && this.isCrouching()) {
                        pilloryBreakOutProgress += 1;
                        wasCrouching = true;
                    } else if (wasCrouching && !this.isCrouching()) {
                        pilloryBreakOutProgress += 1;
                        wasCrouching = false;
                    }

                    BlockPos pos = entityData.get(DATA_DETAINED_TO_BLOCK);
                    BlockState state = getBlockDetainedTo(level());
                    boolean flag = state.getBlock() instanceof DetentionBlock;
                    boolean flag1 = false;

                    if (CuffedMod.SERVER_CONFIG.ALLOW_BREAKING_OUT_OF_PILLORY.get()) {
                        if (pilloryBreakOutProgress >= 100) {
                            if (state.getBlock() instanceof PilloryBlock) {
                                level().levelEvent(2001, pos, Block.getId(state));
                                level().playSound(null, pos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS);
                                undetain();
                                pilloryBreakOutProgress = 0;
                            }
                        } else {
                            if (state.getBlock() instanceof DetentionBlock detentionBlock)
                                flag1 = detentionBlock.canDetainPlayer(level(), state,
                                        entityData.get(DATA_DETAINED_TO_BLOCK), (Player) (Object) this, false);
                            if (!flag || !flag1) {
                                undetain();
                            }
                        }
                    }
                } else {
                    undetain();
                }
            }

            if (CuffedMod.SERVER_CONFIG.ALLOW_SELF_RESTRAINING.get()) {
                if (swinging) {
                    if (!hasProcessedSwing) {
                        hasProcessedSwing = true;
                        ServerPlayer me = (ServerPlayer) (Object) this;
                        cuffed$attemptToRemoveRestraint(me, cap);
                    }
                } else
                    hasProcessedSwing = false;
            }
        }
    }

    @Unique
    private boolean cuffed$attemptToRemoveRestraint(ServerPlayer player, RestrainableCapability cap) {
        boolean armsRestrained = cap.armsRestrained();
        if (!armsRestrained) {
            Vec3 lookAngle = player.getLookAngle();
            if (lookAngle.y >= 60f / 180f * Mth.DEG_TO_RAD) {
                cap.onInteractedByOther(player, player, 2d, getItemInHand(InteractionHand.MAIN_HAND),
                        InteractionHand.MAIN_HAND, true);
            } else if (lookAngle.y <= -60f / 180f) {
                cap.onInteractedByOther(player, player, 0d, getItemInHand(InteractionHand.MAIN_HAND),
                        InteractionHand.MAIN_HAND, true);
            } else {
                cap.onInteractedByOther(player, player, 1d, getItemInHand(InteractionHand.MAIN_HAND),
                        InteractionHand.MAIN_HAND, true);
            }
            return true;
        }
        return false;
    }

    @Inject(at = @At("HEAD"), method = "wantsToStopRiding", cancellable = true)
    private void cuffed$wantsToStopRiding(CallbackInfoReturnable<Boolean> callback) {
        Player player = (Player) (Object) this;
        IRestrainableCapability cap = CuffedAPI.Capabilities.getRestrainableCapability(player);
        if (player.isPassenger() && (cap.armsRestrained() || cap.legsRestrained()))
            callback.setReturnValue(false);
    }

    /** Former LivingFallEvent handler: restraints can change the fall damage multiplier. */
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true)
    private float cuffed$causeFallDamage(float damageModifier, double fallDistance) {
        if ((Object) this instanceof ServerPlayer player)
            return cuffed$restrainable.onLandServer(player, (float) fallDistance, damageModifier);
        cuffed$restrainable.onLandClient((Player) (Object) this, (float) fallDistance, damageModifier);
        return damageModifier;
    }

    /** Former LivingDamageEvent handler: the Imbue enchantment shares a captor's damage with their captives. */
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float cuffed$actuallyHurt(float damage, ServerLevel level, DamageSource source) {
        return ModServerEvents.onCaptorDamaged((Player) (Object) this, level, damage);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void cuffed$addAdditionalSaveData(ValueOutput output, CallbackInfo callback) {
        CompoundTag detainedTag = new CompoundTag();
        detainedTag.putInt("Id", getDetained());
        detainedTag.putFloat("Rotation", getDetainedRotation());
        BlockPos blockPos = entityData.get(DATA_DETAINED_TO_BLOCK);
        detainedTag.putIntArray("Block", new int[] { blockPos.getX(), blockPos.getY(), blockPos.getZ() });
        Vector3f pos = getDetainedPosition();
        detainedTag.putFloat("X", pos.x());
        detainedTag.putFloat("Y", pos.y());
        detainedTag.putFloat("Z", pos.z());
        output.store(DETAINED_TAG, CompoundTag.CODEC, detainedTag);

        if (CuffedMod.SERVER_CONFIG.NICKNAME_PERSISTS_ON_LOGOUT.get() && getNickname() != null)
            output.putString(NICKNAME_TAG, serializeNickname());

        CompoundTag privacy = new CompoundTag();
        privacy.put(TAG_RESTRICTIONS, serializeRestrictions());
        output.store(TAG_RESTRICTIONS, CompoundTag.CODEC, privacy);

        output.store(RESTRAINTS_TAG, CompoundTag.CODEC, cuffed$restrainable.serializeNBT());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void cuffed$readAdditionalSaveData(ValueInput input, CallbackInfo callback) {
        input.read(DETAINED_TAG, CompoundTag.CODEC).ifPresent(t -> {
            setDetained(t.getIntOr("Id", -1));
            setDetainedRotation(t.getFloatOr("Rotation", 0));
            int[] a = t.getIntArray("Block").orElse(new int[] { 0, 0, 0 });
            if (a.length >= 3)
                setBlockDetainedTo(new BlockPos(a[0], a[1], a[2]));
            setDetainedPosition(new Vector3f(t.getFloatOr("X", 0), t.getFloatOr("Y", 0), t.getFloatOr("Z", 0)));
        });

        String nick = input.getStringOr(NICKNAME_TAG, "");
        if (CuffedMod.SERVER_CONFIG.NICKNAME_PERSISTS_ON_LOGOUT.get() && !nick.isEmpty())
            deserializeNickname(nick);
        else
            setNickname(null);

        input.read(TAG_RESTRICTIONS, CompoundTag.CODEC).ifPresentOrElse(this::deserializeRestrictions,
                this::writeDefaultRestrictions);

        input.read(RESTRAINTS_TAG, CompoundTag.CODEC).ifPresent(cuffed$restrainable::deserializeNBT);
    }

    // #region Nicknamable Data Management

    @Override
    public Component getNickname() {
        if (entityData.get(DATA_HAS_NICKNAME))
            return entityData.get(DATA_NICKNAME);
        return null;
    }

    @Override
    public void setNickname(@Nullable Component value) {
        if (value == null) {
            entityData.set(DATA_HAS_NICKNAME, false);
            entityData.set(DATA_NICKNAME, Component.empty());
        } else {
            entityData.set(DATA_HAS_NICKNAME, true);
            entityData.set(DATA_NICKNAME, value);
        }
    }

    @Override
    public String serializeNickname() {
        return NicknameUtil.toJson(getNickname());
    }

    @Override
    public void deserializeNickname(CompoundTag tag) {
        tag.getString(NICKNAME_TAG).ifPresent(this::deserializeNickname);
    }

    @Override
    public void deserializeNickname(String nickTag) {
        setNickname(NicknameUtil.fromJson(nickTag));
    }

    // #endregion

    // #region Restraint Data Management

    @Override
    public boolean isRestrained() {
        return entityData.get(DATA_RESTRAINT_CODE) > 0;
    }

    @Override
    public int getRestraintCode() {
        return entityData.get(DATA_RESTRAINT_CODE);
    }

    @Override
    public Identifier getArmRestraintId() {
        return cuffed$parseId(entityData.get(DATA_ARM_RESTRAINT_ID));
    }

    @Override
    public Identifier getLegRestraintId() {
        return cuffed$parseId(entityData.get(DATA_LEG_RESTRAINT_ID));
    }

    @Override
    public Identifier getHeadRestraintId() {
        return cuffed$parseId(entityData.get(DATA_HEAD_RESTRAINT_ID));
    }

    @Unique
    private static Identifier cuffed$parseId(String s) {
        Identifier id = s == null || s.isEmpty() ? null : Identifier.tryParse(s);
        return id != null ? id : Identifier.withDefaultNamespace("air");
    }

    @Override
    public void setRestraintCode(int v) {
        entityData.set(DATA_RESTRAINT_CODE, v);
    }

    @Override
    public void setArmRestraintId(Identifier v) {
        entityData.set(DATA_ARM_RESTRAINT_ID, v.toString());
    }

    @Override
    public void setLegRestraintId(Identifier v) {
        entityData.set(DATA_LEG_RESTRAINT_ID, v.toString());
    }

    @Override
    public void setHeadRestraintId(Identifier v) {
        entityData.set(DATA_HEAD_RESTRAINT_ID, v.toString());
    }

    @Override
    public boolean getArmsAreEnchanted() {
        return entityData.get(DATA_ARM_ENCHANTED);
    }

    @Override
    public boolean getLegsAreEnchanted() {
        return entityData.get(DATA_LEG_ENCHANTED);
    }

    @Override
    public boolean getHeadIsEnchanted() {
        return entityData.get(DATA_HEAD_ENCHANTED);
    }

    @Override
    public void setArmsEnchanted(boolean v) {
        entityData.set(DATA_ARM_ENCHANTED, v);
    }

    @Override
    public void setLegsEnchanted(boolean v) {
        entityData.set(DATA_LEG_ENCHANTED, v);
    }

    @Override
    public void setHeadEnchanted(boolean v) {
        entityData.set(DATA_HEAD_ENCHANTED, v);
    }

    // #endregion

    // #region Detainable Data Management

    @Override
    public void detainToBlock(@Nonnull Level level, Vector3f detainPos, @Nonnull BlockPos pos, int detaintType,
            float facingRotation) {
        setDetained(detaintType);
        setDetainedRotation(facingRotation);
        setBlockDetainedTo(pos);
        setDetainedPosition(detainPos);
    }

    @Override
    public void undetain() {
        setDetained(-1);
        setDetainedRotation(0);
        setBlockDetainedTo(BlockPos.ZERO);
        setDetainedPosition(new Vector3f(0, 0, 0));
    }

    @Override
    public int getDetained() {
        return entityData.get(DATA_DETAINED);
    }

    @Override
    public void setDetained(int value) {
        entityData.set(DATA_DETAINED, value);
    }

    @Override
    public float getDetainedRotation() {
        return entityData.get(DATA_DETAINED_ROTATION);
    }

    @Override
    public void setDetainedRotation(float value) {
        entityData.set(DATA_DETAINED_ROTATION, value);
    }

    @Override
    public BlockState getBlockDetainedTo(@Nonnull Level level) {
        return level.getBlockState(getDetainedBlockPos());
    }

    @Override
    public BlockPos getDetainedBlockPos() {
        return entityData.get(DATA_DETAINED_TO_BLOCK);
    }

    @Override
    public void setBlockDetainedTo(@Nonnull BlockPos pos) {
        entityData.set(DATA_DETAINED_TO_BLOCK, pos);
    }

    @Override
    public Vector3f getDetainedPosition() {
        return new Vector3f(entityData.get(DATA_DETAINED_POSITION));
    }

    @Override
    public void setDetainedPosition(Vector3f value) {
        entityData.set(DATA_DETAINED_POSITION, new Vector3f(value));
    }

    // #endregion

    // #region Privacy Data Management

    @Unique
    final HashMap<String, PrivacyRestriction> privacyRestrictions = new HashMap<>();

    @Override
    public PrivacyRestriction getAnchoringRestrictions() {
        return getRestriction(ANCHORING_RESTRICTION);
    }

    @Override
    public void setAnchoringRestrictions(PrivacyRestriction newRestriction) {
        setRestriction(ANCHORING_RESTRICTION, newRestriction);
    }

    @Override
    public PrivacyRestriction getDetainingRestrictions() {
        return getRestriction(DETAINING_RESTRICTION);
    }

    @Override
    public void setDetainingRestrictions(PrivacyRestriction newRestriction) {
        setRestriction(DETAINING_RESTRICTION, newRestriction);
    }

    @Override
    public PrivacyRestriction getNicknamingRestrictions() {
        return getRestriction(NICKNAMING_RESTRICTION);
    }

    @Override
    public void setNicknamingRestrictions(PrivacyRestriction newRestriction) {
        setRestriction(NICKNAMING_RESTRICTION, newRestriction);
    }

    @Override
    public PrivacyRestriction getRestrainingRestrictions() {
        return getRestriction(RESTRAINING_RESTRICTION);
    }

    @Override
    public void setRestrainingRestrictions(PrivacyRestriction newRestriction) {
        setRestriction(RESTRAINING_RESTRICTION, newRestriction);
    }

    @Override
    public void setRestriction(String key, PrivacyRestriction newRestriction) {
        this.privacyRestrictions.put(key, newRestriction);
    }

    @Override
    public PrivacyRestriction getRestriction(String key) {
        return this.privacyRestrictions.get(key);
    }

    @Override
    public void deserializeRestrictions(CompoundTag tag) {
        ListTag list = tag.getListOrEmpty(TAG_RESTRICTIONS);
        if (list.isEmpty()) {
            writeDefaultRestrictions();
            return;
        }
        privacyRestrictions.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompoundOrEmpty(i);
            privacyRestrictions.put(t.getStringOr(TAG_RESTRICTION_ID, ""),
                    PrivacyRestriction.fromInteger(t.getIntOr(TAG_RESTRICTION, 3)));
        }
    }

    @Override
    public ListTag serializeRestrictions() {
        ListTag list = new ListTag();
        if (privacyRestrictions.size() == 0)
            writeDefaultRestrictions();
        for (Entry<String, PrivacyRestriction> r : privacyRestrictions.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(TAG_RESTRICTION, r.getValue().toInteger());
            tag.putString(TAG_RESTRICTION_ID, r.getKey());
            list.add(tag);
        }
        return list;
    }

    @Override
    public void writeDefaultRestrictions() {
        privacyRestrictions.clear();
        privacyRestrictions.put(ANCHORING_RESTRICTION, PrivacyRestriction.NEVER);
        privacyRestrictions.put(DETAINING_RESTRICTION, PrivacyRestriction.NEVER);
        privacyRestrictions.put(NICKNAMING_RESTRICTION, PrivacyRestriction.NEVER);
        privacyRestrictions.put(RESTRAINING_RESTRICTION, PrivacyRestriction.NEVER);
    }

    // #endregion
}
