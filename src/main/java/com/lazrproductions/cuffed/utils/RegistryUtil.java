package com.lazrproductions.cuffed.utils;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

/**
 * Item stacks need registry access to be (de)serialized since 1.20.5. Restraints are created from
 * NBT in places without a level at hand, so this keeps a reference to the current registries.
 */
public final class RegistryUtil {
    private static @Nullable MinecraftServer server;
    private static Supplier<RegistryAccess> clientRegistries = () -> null;

    private RegistryUtil() {
    }

    public static void setServer(@Nullable MinecraftServer value) {
        server = value;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return server;
    }

    public static void setClientRegistries(Supplier<RegistryAccess> supplier) {
        clientRegistries = supplier;
    }

    @Nullable
    public static HolderLookup.Provider get() {
        if (server != null)
            return server.registryAccess();
        return clientRegistries.get();
    }

    public static Tag saveStack(ItemStack stack) {
        HolderLookup.Provider registries = get();
        if (stack.isEmpty() || registries == null)
            return new CompoundTag();
        return ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result().orElseGet(CompoundTag::new);
    }

    public static ItemStack loadStack(@Nullable Tag tag) {
        HolderLookup.Provider registries = get();
        if (tag == null || registries == null || (tag instanceof CompoundTag c && c.isEmpty()))
            return ItemStack.EMPTY;
        Optional<ItemStack> result = ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
                .result();
        return result.orElse(ItemStack.EMPTY);
    }
}
