package com.lazrproductions.cuffed.entity;



import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.entity.base.IAnchorableEntity;
import com.lazrproductions.cuffed.init.ModEnchantments;
import com.lazrproductions.cuffed.init.ModEntityTypes;
import com.lazrproductions.cuffed.init.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.resources.ResourceKey;

public class WeightedAnchorEntity extends LivingEntity {

    private static final EntityDataAccessor<Boolean> DATA_ENCHANTED = SynchedEntityData.defineId(WeightedAnchorEntity.class, EntityDataSerializers.BOOLEAN);


    public WeightedAnchorEntity(EntityType<? extends WeightedAnchorEntity> type, Level level) {
        super(type, level);
    }
    public WeightedAnchorEntity(Level world, BlockPos pos) {
        super(ModEntityTypes.WEIGHTED_ANCHOR, world);
        this.setPos((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
    }

    public static WeightedAnchorEntity createFromItem(@Nonnull Level level, @Nonnull ItemStack stack, @Nonnull BlockPos pos) {
        WeightedAnchorEntity entity = new WeightedAnchorEntity(level, pos);

        entity.setEnchantments(EnchantmentHelper.getEnchantmentsForCrafting(stack));

        return entity;
    }

    public ItemStack getDroppedItem() {
        ItemStack stack = new ItemStack(ModItems.WEIGHTED_ANCHOR_ITEM, 1);
        EnchantmentHelper.setEnchantments(stack, getEnchantments());
        return stack;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENCHANTED, false);
    }
    @Override
    public void tick() {
        if(!level().isClientSide()) {
            entityData.set(DATA_ENCHANTED, !getEnchantments().isEmpty());
            if(this.isInWater() && getEnchantmentLevel(ModEnchantments.BUOYANT) >= 1)
                this.addDeltaMovement(new Vec3(0f, 0.023f, 0f));
         }
  
        super.tick();

    }

    public boolean isInvulnerable() {
        return true;
    }
    @Override
    public boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel level, @Nonnull DamageSource source) {

        if(source.is(DamageTypes.GENERIC_KILL))
            return false;
        return true;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.CHAIN_PLACE, SoundEvents.ANVIL_FALL);
    }

    @Override
    public ItemStack getPickResult() {
        return getDroppedItem();
    }

    protected Vec3 getLeashOffset() {
        return new Vec3(0.0D, 8D / 16D, 0);
    }
    @Override
    public Vec3 getRopeHoldPosition(float p_20347_) {
        return position().add(getLeashOffset());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public InteractionResult interact(Player interactor, InteractionHand hand, net.minecraft.world.phys.Vec3 location) {

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        } else if(hand == InteractionHand.MAIN_HAND) {
            if(!interactor.isCrouching() && CuffedMod.SERVER_CONFIG.ANCHORING_ALLOW_ANCHORING_TO_WEIGHTED_ANCHORS.get()) {
                boolean flag = false;
                double maxDist = CuffedMod.SERVER_CONFIG.ANCHORING_SUFFOCATION_LENGTH.get() + 5;
                List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,
                        new AABB(this.getX() - maxDist - 2.0D, this.getY() - maxDist - 2.0D, this.getZ() - maxDist - 2.0D,
                                this.getX() + maxDist + 2.0D, this.getY() + maxDist + 2.0D, this.getZ() + maxDist + 2.0D));

                for (LivingEntity entity : list) {
                    IAnchorableEntity anchorableEntity = (IAnchorableEntity) entity;
                    if (anchorableEntity.getAnchor() == interactor) {
                        anchorableEntity.setAnchoredTo(this);
                        flag = true;
                    }
                }

                boolean flag1 = false;
                if (!flag) {
                    for (LivingEntity entity : list) {
                        IAnchorableEntity anchorableEntity = (IAnchorableEntity) entity;
                        if (anchorableEntity.isAnchored() && anchorableEntity.getAnchor() == this) {
                            anchorableEntity.setAnchoredTo(null);
                            flag1 = true;
                        }
                    }
                    if(flag1)
                        level().playSound(null, blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.7f, 1);
                }

                if(flag)
                    level().playSound(null, blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.7f, 1);
                if(flag1)
                    level().playSound(null, blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.7f, 1);

                if (flag || flag1) {
                    this.gameEvent(GameEvent.BLOCK_ATTACH, interactor);
                }

                return InteractionResult.CONSUME;
            } else {
                ItemStack stack = getDroppedItem();
                ItemEntity entity = new ItemEntity(level(), position().x(), position().y(), position().z(), stack); 
                level().addFreshEntity(entity);
                
                level().playSound(null, blockPosition(), SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 0.7f, 1);
                this.discard();
            }
        }

        return InteractionResult.FAIL;
    }


    @Override
    protected void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.store("CuffedEnchantments", ItemEnchantments.CODEC, enchantments);
    }
    @Override
    protected void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        enchantments = tag.read("CuffedEnchantments", ItemEnchantments.CODEC).orElse(ItemEnchantments.EMPTY);

    }


    //#region Enchantment Management

    ItemEnchantments enchantments = ItemEnchantments.EMPTY;

    public ItemEnchantments getEnchantments() {
        return enchantments;
    }

    public void setEnchantments(ItemEnchantments value) {
        enchantments = value != null ? value : ItemEnchantments.EMPTY;
    }

    public boolean hasEnchantment(ResourceKey<Enchantment> enchantment) {
        return getEnchantmentLevel(enchantment) > 0;
    }

    public int getEnchantmentLevel(ResourceKey<Enchantment> enchantment) {
        for (var entry : enchantments.entrySet())
            if (entry.getKey().is(enchantment))
                return entry.getIntValue();
        return 0;
    }

    public boolean getIsEnchanted() {
        return entityData.get(DATA_ENCHANTED);
    }

    //#endregion


    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.MOVEMENT_SPEED, 0F);
    }



    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
