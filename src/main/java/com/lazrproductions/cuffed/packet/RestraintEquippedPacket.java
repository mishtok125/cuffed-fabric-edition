package com.lazrproductions.cuffed.packet;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.cap.RestrainableCapability;
import com.lazrproductions.cuffed.restraints.RestraintAPI;
import com.lazrproductions.cuffed.restraints.base.AbstractArmRestraint;
import com.lazrproductions.cuffed.restraints.base.AbstractHeadRestraint;
import com.lazrproductions.cuffed.restraints.base.AbstractLegRestraint;
import com.lazrproductions.cuffed.restraints.base.RestraintType;
import com.lazrproductions.lazrslib.common.network.packet.ParameterizedLazrPacket;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.lazrproductions.lazrslib.common.network.PacketContext;

/**
 * A packet to sync the equipping and unequipping of restraints to the
 * restrained client.
 */
public class RestraintEquippedPacket extends ParameterizedLazrPacket {

    int playerId;
    String playerUUID;

    int type;
    String oldData;
    String newData;

    String captorUUID;

    public RestraintEquippedPacket(int playerId, String playerUUID,
            @Nonnull RestraintType type, @Nullable CompoundTag oldData, @Nullable CompoundTag newData,
            String captorUUID) {
        super(playerId, playerUUID, type.toInteger(), oldData != null ? oldData.toString() : "null", newData != null ? newData.toString() : "null", captorUUID);
        
        this.playerId = playerId;
        this.playerUUID = playerUUID;

        this.type = type.toInteger();
        this.oldData = oldData != null ? oldData.toString() : "null";
        this.newData = newData != null ? newData.toString() : "null";
        
        this.captorUUID = captorUUID;
    }

    public RestraintEquippedPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void loadValues(Object[] arg0) {
        this.playerId = (int)arg0[0];
        this.playerUUID = (String)arg0[1];

        this.type = (int)arg0[2];

        this.oldData = (String)arg0[3];
        this.newData = (String)arg0[4];

        this.captorUUID = (String)arg0[5];
    }

    @Override
    public void handleClientside(PacketContext ctx) {
        Clientside.handleClientside(ctx, playerId, playerUUID, type, oldData, newData, captorUUID);
    }

    @Override
    public void handleServerside(PacketContext ctx) {
    }

    static class Clientside {
        private static void closeInventory(Minecraft minecraft) {
            if (minecraft.gui.screen() instanceof InventoryScreen
                    || minecraft.gui.screen() instanceof CreativeModeInventoryScreen)
                minecraft.player.closeContainer();
        }

        public static void handleClientside(PacketContext ctx, int playerId, String playerUUID, int type, String oldData, String newData, String captorUUID) {
            Minecraft inst = Minecraft.getInstance();
            Player arg0 = inst.player;
            
            if(arg0 != null) {
                RestrainableCapability cap = (RestrainableCapability) CuffedAPI.Capabilities.getRestrainableCapability(arg0);
    
                try {
                    CompoundTag newTag = newData.toLowerCase().equals("null") ? null : TagParser.parseCompoundFully(newData);
                    CompoundTag oldTag = oldData.toLowerCase().equals("null") ? null : TagParser.parseCompoundFully(oldData);
                    Player captor = !captorUUID.equals("null") ? arg0.level().getPlayerByUUID(UUID.fromString(captorUUID)) : null;
    
                    if (type == RestraintType.Arm.toInteger()) {
                        // Sets the client-side version of the restraint AND sends onEquippedClient &
                        // onUnequipped events
                        if (!newData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(newTag) instanceof AbstractArmRestraint arm) {
                                cap.armRestraint = arm;
                                arm.onEquippedClient(arg0, captor);
                                closeInventory(inst);
                            }
                        } else
                            cap.armRestraint = null;

                        if (!oldData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(oldTag) instanceof AbstractArmRestraint oldRestraint)
                                oldRestraint.onUnequippedClient(arg0);
                        }
                    } else if(type == RestraintType.Leg.toInteger()) {
                        // Sets the client-side version of the restraint AND sends onEquippedClient &
                        // onUnequipped events
                        if (!newData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(newTag) instanceof AbstractLegRestraint leg) {
                                cap.legRestraint = leg;
                                leg.onEquippedClient(arg0, captor);
                                closeInventory(inst);
                            }
                        } else
                            cap.legRestraint = null;
                        if (!oldData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(oldTag) instanceof AbstractLegRestraint oldRestraint)
                                oldRestraint.onUnequippedClient(arg0);
                        }
                    }  else if(type == RestraintType.Head.toInteger())  {
                        // Sets the client-side version of the restraint AND sends onEquippedClient &
                        // onUnequipped events
                        if (!newData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(newTag) instanceof AbstractHeadRestraint head) {
                                cap.headRestraint = head;
                                head.onEquippedClient(arg0, captor);
                                closeInventory(inst);
                            }
                        } else
                            cap.headRestraint = null;
                        if (!oldData.toLowerCase().equals("null")) {
                            if (RestraintAPI.getRestraintFromTag(oldTag) instanceof AbstractHeadRestraint oldRestraint)
                                oldRestraint.onUnequippedClient(arg0);
                        }
                    }
                } catch (CommandSyntaxException exception) {
                    CuffedMod.LOGGER.warn("NBT was received in incorrect format!");
                }
            }
        }
    }
}
