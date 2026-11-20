package com.lazrproductions.cuffed.blocks.entity;



import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.init.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LockableBlockEntity extends BlockEntity {
    private UUID lockId;
    private boolean locked;
    private String lockName;
    private boolean hasBeenBound;

    public LockableBlockEntity(BlockPos pos, BlockState state, String lockName) {
        super(ModBlockEntities.CELL_DOOR_BLOCK_ENTITY, pos, state);
        lockId = UUID.randomUUID();
        locked = false;
        this.lockName = lockName;
        hasBeenBound = false;
    }
    public LockableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CELL_DOOR_BLOCK_ENTITY, pos, state);
        lockId = UUID.randomUUID();
        locked = false;
        lockName = "info.cuffed.lock";
        hasBeenBound = false;
    }

    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.store("LockId", net.minecraft.core.UUIDUtil.CODEC, lockId);
        tag.putBoolean("Locked", locked);
        tag.putString("LockName", lockName);
        tag.putBoolean("HasBeenBound", hasBeenBound);
    }

    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        lockId = tag.read("LockId", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        locked = tag.getBooleanOr("Locked", false);
        lockName = tag.getStringOr("LockName", "");
        hasBeenBound = tag.getBooleanOr("HasBeenBound", false);
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
    public String getLockName() {
        return lockName;
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
}
