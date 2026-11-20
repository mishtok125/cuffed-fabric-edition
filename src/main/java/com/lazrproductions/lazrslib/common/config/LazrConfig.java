package com.lazrproductions.lazrslib.common.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

/**
 * JSON-backed replacement for the Forge config wrapper.
 * The file is written to {@code config/<name>-<type>.json}; each property is stored as
 * {@code {"value": ..., "comment": "..."}} inside its category object.
 */
public abstract class LazrConfig {
    static final Logger LOGGER = LoggerFactory.getLogger("lazrslib-config");
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public enum Type {
        CLIENT, COMMON, SERVER
    }

    public interface CreatePropertiesFunction {
        void call(ConfigCategory category);
    }

    final String name;
    final Type type;
    final ArrayList<ConfigProperty<?>> genericProperties = new ArrayList<>(0);
    final ArrayList<ConfigCategory> categories = new ArrayList<>(0);

    public LazrConfig(String name, Type type) {
        this.name = name;
        this.type = type;
        this.registerProperties();
    }

    public abstract void registerProperties();

    public <T> ConfigProperty<T> createGenericProperty(@Nonnull ConfigProperty<T> property) {
        genericProperties.add(property);
        return property;
    }

    public ConfigCategory createCategory(@Nonnull ConfigCategory category, CreatePropertiesFunction createProperties) {
        createProperties.call(category);
        categories.add(category);
        return category;
    }

    Type getType() {
        return type;
    }

    public Path getPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(name + "-" + type.name().toLowerCase(Locale.ROOT) + ".json");
    }

    /** Loads the config from disk (creating it with defaults if missing) and writes back any new options. */
    public void registerConfig() {
        load();
        save();
        LOGGER.info("Loaded config '{}'", getPath().getFileName());
    }

    public void load() {
        Path path = getPath();
        if (!Files.exists(path))
            return;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject())
                return;
            JsonObject obj = root.getAsJsonObject();
            readProperties(obj, genericProperties);
            for (ConfigCategory category : categories) {
                if (obj.has(category.name) && obj.get(category.name).isJsonObject())
                    readProperties(obj.getAsJsonObject(category.name), category.properties);
            }
        } catch (Exception e) {
            LOGGER.error("Could not read config {}, using defaults", path, e);
        }
    }

    static void readProperties(JsonObject obj, Iterable<ConfigProperty<?>> properties) {
        for (ConfigProperty<?> p : properties) {
            JsonElement e = obj.get(p.name);
            if (e != null && e.isJsonObject())
                e = e.getAsJsonObject().get("value");
            if (e != null && !e.isJsonNull())
                p.read(e);
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        writeProperties(root, genericProperties);
        for (ConfigCategory category : categories) {
            JsonObject obj = new JsonObject();
            writeProperties(obj, category.properties);
            root.add(category.name, obj);
        }
        Path path = getPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Could not write config {}", path, e);
        }
    }

    static void writeProperties(JsonObject obj, Iterable<ConfigProperty<?>> properties) {
        for (ConfigProperty<?> p : properties) {
            JsonObject entry = new JsonObject();
            entry.add("value", p.write());
            entry.addProperty("comment", p.comment);
            obj.add(p.name, entry);
        }
    }
}
