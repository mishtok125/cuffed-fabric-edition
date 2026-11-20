package com.lazrproductions.lazrslib.common.network;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;

/** Stand-in for Forge's NetworkEvent.Context. */
@FunctionalInterface
public interface PacketContext {
    /** The player who sent the packet, or null when the packet was received on the client. */
    @Nullable
    ServerPlayer getSender();
}
