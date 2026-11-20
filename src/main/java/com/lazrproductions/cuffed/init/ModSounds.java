package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent HANDCUFFED = register("restraint.apply_handcuffs");
    public static final SoundEvent SHACKLES_EQUIP = register("restraint.apply_shackles");
    public static final SoundEvent PILLORY_USE = register("block.pillory.use");
    public static final SoundEvent GUILLOTINE_USE = register("block.guillotine.use");
    public static final SoundEvent SAFE_OPEN = register("block.safe.open");
    public static final SoundEvent SAFE_CLOSE = register("block.safe.close");

    static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(CuffedMod.MODID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
    }
}
