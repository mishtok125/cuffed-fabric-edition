package com.lazrproductions.lazrslib.common.config;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ConfigProperty<T> {
    final LazrConfig config;
    final String name;
    final String comment;
    final T defaultValue;
    T value;

    public ConfigProperty(@Nonnull LazrConfig config, String name, String description, T defaultValue) {
        this.config = config;
        this.name = name;
        this.comment = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return comment;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        this.value = newValue;
    }

    @SuppressWarnings("unchecked")
    void read(JsonElement e) {
        try {
            Object v;
            if (defaultValue instanceof Boolean)
                v = e.getAsBoolean();
            else if (defaultValue instanceof Integer)
                v = e.getAsInt();
            else if (defaultValue instanceof Float)
                v = e.getAsFloat();
            else if (defaultValue instanceof Double)
                v = e.getAsDouble();
            else if (defaultValue instanceof Long)
                v = e.getAsLong();
            else
                v = e.getAsString();
            this.value = (T) v;
        } catch (Exception ex) {
            LazrConfig.LOGGER.warn("Invalid value for config option '{}', using default {}", name, defaultValue);
            this.value = defaultValue;
        }
    }

    JsonElement write() {
        if (value instanceof Boolean b)
            return new JsonPrimitive(b);
        if (value instanceof Number n)
            return new JsonPrimitive(n);
        return new JsonPrimitive(String.valueOf(value));
    }
}
