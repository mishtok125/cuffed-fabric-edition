package com.lazrproductions.cuffed.blocks.entity;



import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.UUID;

import com.lazrproductions.cuffed.blocks.SafeBlock;
import com.lazrproductions.cuffed.init.ModBlockEntities;
import com.lazrproductions.cuffed.init.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("null")
public class SafeBlockEntity extends RandomizableContainerBlockEntity {
    private UUID lockId;
    private boolean locked;
    private boolean hasBeenBound;

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
            SafeBlockEntity.this.playSound(p_155064_, ModSounds.SAFE_OPEN);
            SafeBlockEntity.this.updateBlockState(p_155064_, true);
        }

        protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
            SafeBlockEntity.this.playSound(p_155074_, ModSounds.SAFE_CLOSE);
            SafeBlockEntity.this.updateBlockState(p_155074_, false);
        }

        protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_,
                int p_155070_) {
        }

        @Override
        public boolean isOwnContainer(Player p_155060_) {
            if (p_155060_.containerMenu instanceof ChestMenu) {
                Container container = ((ChestMenu) p_155060_.containerMenu).getContainer();
                return container == SafeBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public SafeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAFE_BLOCK_ENTITY, pos, state);
        lockId = UUID.randomUUID();
        locked = false;
        hasBeenBound = false;
    }

    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.store("LockId", net.minecraft.core.UUIDUtil.CODEC, lockId);
        tag.putBoolean("Locked", locked);
        tag.putBoolean("HasBeenBound", hasBeenBound);
    }

    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
        lockId = tag.read("LockId", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        locked = tag.getBooleanOr("Locked", false);
        hasBeenBound = tag.getBooleanOr("HasBeenBound", false);
    }

    public int getContainerSize() {
        return 27;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> newItems) {
        this.items = newItems;
    }

    protected Component getDefaultName() {
        return Component.translatable("block.cuffed.safe");
    }

    protected AbstractContainerMenu createMenu(int menuId, Inventory inventory) {
        return ChestMenu.threeRows(menuId, inventory, this);
    }

    @Override
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.incrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(),
                    this.getBlockState(), user.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator()) {
            this.openersCounter.decrementOpeners(user.getLivingEntity(), this.getLevel(), this.getBlockPos(),
                    this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }


    public void setLocked(boolean value, Level level, Player player, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.0F,
                level.getRandom().nextFloat() * 0.1F + 0.9F);
        player.sendOverlayMessage(
                Component.translatable("info.lock.toggle_" + (!locked ? "on" : "off")));
        locked = value;
    }
    public boolean isLocked() {
        return locked;
    }
    public UUID getLockId() {
        return lockId;
    }
    public boolean hasBeenBound() {
        return hasBeenBound;
    }
    public void bind() {
        hasBeenBound = true;
    }
    public void resetBinding() {
        lockId = UUID.randomUUID();
        locked = false;
        hasBeenBound = false;
    }


    void updateBlockState(BlockState state, boolean b1) {
        this.level.setBlock(this.getBlockPos(), state.setValue(SafeBlock.OPEN, Boolean.valueOf(b1)), 3);
    }

    void playSound(BlockState state, SoundEvent soundEvent) {
        Vec3i vec3i = state.getValue(SafeBlock.FACING).getUnitVec3i();
        double d0 = (double) this.worldPosition.getX() + 0.5D + (double) vec3i.getX() / 2.0D;
        double d1 = (double) this.worldPosition.getY() + 0.5D + (double) vec3i.getY() / 2.0D;
        double d2 = (double) this.worldPosition.getZ() + 0.5D + (double) vec3i.getZ() / 2.0D;
        this.level.playSound((Player) null, d0, d1, d2, soundEvent, SoundSource.BLOCKS, 0.5F,
                this.level.getRandom().nextFloat() * 0.1F + 0.9F);
    }
}