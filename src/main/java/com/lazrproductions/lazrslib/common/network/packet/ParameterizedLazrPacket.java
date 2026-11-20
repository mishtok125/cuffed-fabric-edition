package com.lazrproductions.lazrslib.common.network.packet;

import com.lazrproductions.lazrslib.common.network.LazrNetworkedParameterHandler;
import com.lazrproductions.lazrslib.common.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

public abstract class ParameterizedLazrPacket {
    Object[] parameters;

    public ParameterizedLazrPacket(FriendlyByteBuf buffer) {
        setFrom(LazrNetworkedParameterHandler.readPacketParameters(buffer));
    }

    public ParameterizedLazrPacket(Object... p) {
        this.parameters = p;
    }

    public void encode(FriendlyByteBuf buffer) {
        LazrNetworkedParameterHandler.writePacketParameters(parameters, buffer);
    }

    public void setFrom(Object[] parameters) {
        this.parameters = parameters;
        loadValues(parameters);
    }

    public abstract void loadValues(Object[] parameters);

    public abstract void handleClientside(PacketContext context);

    public abstract void handleServerside(PacketContext context);
}
