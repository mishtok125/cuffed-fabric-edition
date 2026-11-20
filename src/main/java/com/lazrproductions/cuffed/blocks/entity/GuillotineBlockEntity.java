package com.lazrproductions.cuffed.blocks.entity;




import com.lazrproductions.cuffed.utils.StackTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.blocks.GuillotineBlock;
import com.lazrproductions.cuffed.blocks.PilloryBlock;
import com.lazrproductions.cuffed.init.ModBlockEntities;
import com.lazrproductions.cuffed.init.ModDamageTypes;
import com.lazrproductions.cuffed.init.ModSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.commands.KillCommand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GuillotineBlockEntity extends BlockEntity {

    static final String TAG_IS_DOWN = "IsDown"; 
    static final String TAG_IS_BLOODY = "IsBloody"; 

    public boolean isBloody;
    public boolean isDown;

    /** Client-side blade animation progress, used by the renderer. */
    public float clientDropProgress = 0;

    public GuillotineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GUILLOTINE, pos, state);
    }

    public void interact(@Nonnull Level l, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if(!l.isClientSide()) {
            this.isDown = !isDown;
            l.playSound(null, pos, isDown ? ModSounds.GUILLOTINE_USE : ModSounds.PILLORY_USE, SoundSource.BLOCKS, 1, 1);
            
            if(isDown)
                chopDelay = 5;
            
            setChanged(l, pos, state);
            l.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void chop(@Nonnull Level l, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        Player player = PilloryBlock.getDetainedEntity(l, pos.below());
        if(player != null) {
            if (l instanceof net.minecraft.server.level.ServerLevel serverLevel)
                player.hurtServer(serverLevel, ModDamageTypes.GetModSource(player, ModDamageTypes.HANG, null), Float.MAX_VALUE);


            if(CuffedMod.SERVER_CONFIG.GUILLOTINE_DROPS_HEAD.get()) {
                ItemStack stack = new ItemStack(Items.PLAYER_HEAD, 1);
                stack.set(net.minecraft.core.component.DataComponents.PROFILE,
                        net.minecraft.world.item.component.ResolvableProfile.createResolved(player.getGameProfile()));
                // Drop the head in front of the blade (the side the head sticks out to) instead of at the
                // player's position, which is inside the pillory blocks and gets the item pushed out sideways.
                net.minecraft.core.Direction front = state.getValue(GuillotineBlock.FACING);
                ItemEntity entity = new ItemEntity(l,
                        pos.getX() + 0.5D + front.getStepX() * 0.7D,
                        pos.getY() + 0.3D,
                        pos.getZ() + 0.5D + front.getStepZ() * 0.7D, stack);
                entity.setDeltaMovement(front.getStepX() * 0.05D, 0.1D, front.getStepZ() * 0.05D);
                l.addFreshEntity(entity);
            }

            this.isBloody = true;
        }
    }

    int chopDelay = 0;

    public void tick(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if(chopDelay > 0) {
            chopDelay--;
            if(chopDelay == 0) {
                chop(level, pos, state);
            }
        }
    }

    
    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        tag.putBoolean(TAG_IS_DOWN, isDown);
        tag.putBoolean(TAG_IS_BLOODY, isBloody);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        isDown = tag.getBooleanOr(TAG_IS_DOWN, false);
        isBloody = tag.getBooleanOr(TAG_IS_BLOODY, false);
    }


    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}
