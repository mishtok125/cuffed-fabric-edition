package com.lazrproductions.cuffed.utils;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

/** Replacement for the removed Component.Serializer JSON helpers. */
public final class NicknameUtil {
    private NicknameUtil() {
    }

    public static String toJson(@Nullable Component component) {
        if (component == null)
            return "";
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, component).result()
                .map(JsonElement::toString).orElse("");
    }

    @Nullable
    public static Component fromJson(@Nullable String json) {
        if (json == null || json.isEmpty())
            return null;
        try {
            return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result()
                    .orElse(Component.literal(json));
        } catch (Exception e) {
            return Component.literal(json);
        }
    }
}
