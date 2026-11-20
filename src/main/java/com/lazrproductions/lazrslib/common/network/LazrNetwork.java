package com.lazrproductions.lazrslib.common.network;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.lazrproductions.lazrslib.common.network.packet.ParameterizedLazrPacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Fabric replacement for the Forge SimpleChannel wrapper. Each packet class gets its own payload type. */
public class LazrNetwork {
    final Identifier name;
    final Map<Class<?>, CustomPacketPayload.Type<LazrPayload>> types = new HashMap<>();

    public LazrNetwork(Identifier location, int version) {
        this.name = location;
    }

    public record LazrPayload(CustomPacketPayload.Type<LazrPayload> payloadType, ParameterizedLazrPacket packet)
            implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return payloadType;
        }
    }

    public <T extends ParameterizedLazrPacket> void registerPacket(Class<T> c, Function<FriendlyByteBuf, T> decoder) {
        Identifier id = Identifier.fromNamespaceAndPath(name.getNamespace(),
                name.getPath() + "/" + c.getSimpleName().toLowerCase(Locale.ROOT));
        CustomPacketPayload.Type<LazrPayload> type = new CustomPacketPayload.Type<>(id);
        StreamCodec<RegistryFriendlyByteBuf, LazrPayload> codec = StreamCodec.of(
                (buf, payload) -> payload.packet().encode(buf),
                buf -> new LazrPayload(type, decoder.apply(buf)));
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        ServerPlayNetworking.registerGlobalReceiver(type,
                (payload, context) -> payload.packet().handleServerside(context::player));
        types.put(c, type);
    }

    /** Must be called from the client entrypoint after all packets are registered. */
    public void registerClientReceivers() {
        Client.registerReceivers(this);
    }

    LazrPayload wrap(ParameterizedLazrPacket packet) {
        CustomPacketPayload.Type<LazrPayload> type = types.get(packet.getClass());
        if (type == null)
            throw new IllegalStateException("Packet " + packet.getClass().getName() + " was never registered");
        return new LazrPayload(type, packet);
    }

    public void sendToServer(ParameterizedLazrPacket msg) {
        Client.send(wrap(msg));
    }

    public void sendTo(ParameterizedLazrPacket msg, Player player) {
        if (player instanceof ServerPlayer serverPlayer)
            sendTo(msg, serverPlayer);
    }

    public void sendTo(ParameterizedLazrPacket msg, ServerPlayer player) {
        if (player.connection != null)
            ServerPlayNetworking.send(player, wrap(msg));
    }

    /** Kept separate so dedicated servers never load client networking classes. */
    static final class Client {
        static void registerReceivers(LazrNetwork network) {
            for (CustomPacketPayload.Type<LazrPayload> type : network.types.values())
                ClientPlayNetworking.registerGlobalReceiver(type,
                        (payload, context) -> payload.packet().handleClientside(() -> null));
        }

        static void send(LazrPayload payload) {
            ClientPlayNetworking.send(payload);
        }
    }
}
