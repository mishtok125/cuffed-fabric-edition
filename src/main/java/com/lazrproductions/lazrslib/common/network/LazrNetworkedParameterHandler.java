package com.lazrproductions.lazrslib.common.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Writes and reads packet parameters with an explicit type tag per value.
 * <p>
 * The original library used Java object serialization here, which allows arbitrary code execution
 * from a malicious peer; only a fixed set of value types is accepted now.
 */
public class LazrNetworkedParameterHandler {
    static final int MAX_PARAMETERS = 64;
    static final int MAX_STRING = 262144;

    public static void writePacketParameters(Object[] parameters, FriendlyByteBuf buffer) {
        buffer.writeVarInt(parameters.length);
        for (Object p : parameters) {
            if (p == null) {
                buffer.writeByte(0);
            } else if (p instanceof Integer i) {
                buffer.writeByte(1);
                buffer.writeInt(i);
            } else if (p instanceof Boolean b) {
                buffer.writeByte(2);
                buffer.writeBoolean(b);
            } else if (p instanceof Double d) {
                buffer.writeByte(3);
                buffer.writeDouble(d);
            } else if (p instanceof Float f) {
                buffer.writeByte(4);
                buffer.writeFloat(f);
            } else if (p instanceof Long l) {
                buffer.writeByte(5);
                buffer.writeLong(l);
            } else if (p instanceof String s) {
                buffer.writeByte(6);
                buffer.writeUtf(s, MAX_STRING);
            } else {
                throw new IllegalArgumentException("Unsupported network parameter type " + p.getClass().getName());
            }
        }
    }

    public static Object[] readPacketParameters(FriendlyByteBuf buffer) {
        int length = buffer.readVarInt();
        if (length < 0 || length > MAX_PARAMETERS)
            throw new IllegalArgumentException("Too many network parameters: " + length);
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            byte type = buffer.readByte();
            result[i] = switch (type) {
                case 0 -> null;
                case 1 -> buffer.readInt();
                case 2 -> buffer.readBoolean();
                case 3 -> buffer.readDouble();
                case 4 -> buffer.readFloat();
                case 5 -> buffer.readLong();
                case 6 -> buffer.readUtf(MAX_STRING);
                default -> throw new IllegalArgumentException("Unknown network parameter type " + type);
            };
        }
        return result;
    }
}
