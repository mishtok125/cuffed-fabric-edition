package com.lazrproductions.cuffed.init;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.restraints.RestraintAPI;
import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;
import com.lazrproductions.cuffed.restraints.custom.BundleRestraint;
import com.lazrproductions.cuffed.restraints.custom.DuckTapeArmsRestraint;
import com.lazrproductions.cuffed.restraints.custom.DuckTapeHeadRestraint;
import com.lazrproductions.cuffed.restraints.custom.DuckTapeLegsRestraint;
import com.lazrproductions.cuffed.restraints.custom.FuzzyHandcuffsRestraint;
import com.lazrproductions.cuffed.restraints.custom.HandcuffsArmsRestraint;
import com.lazrproductions.cuffed.restraints.custom.HandcuffsLegsRestraint;
import com.lazrproductions.cuffed.restraints.custom.PilloryRestraint;
import com.lazrproductions.cuffed.restraints.custom.ShacklesArmsRestraint;
import com.lazrproductions.cuffed.restraints.custom.ShacklesLegsRestraint;

import net.minecraft.resources.Identifier;

public class ModRestraints {
    private static boolean isInitialized = false;
    private static final List<Entry> ENTRIES = new ArrayList<>();

    public static final Entry BUNDLE = entry("bundle", BundleRestraint::new);
    public static final Entry PILLORY = entry("pillory", PilloryRestraint::new);
    public static final Entry DUCK_TAPE_HEAD = entry("duck_tape_head", DuckTapeHeadRestraint::new);
    public static final Entry HANDCUFFS_ARMS = entry("handcuffs_arms", HandcuffsArmsRestraint::new);
    public static final Entry SHACKLES = entry("shackles_arms", ShacklesArmsRestraint::new);
    public static final Entry DUCK_TAPE_ARMS = entry("duck_tape_arms", DuckTapeArmsRestraint::new);
    public static final Entry HANDCUFFS_LEGS = entry("handcuffs_legs", HandcuffsLegsRestraint::new);
    public static final Entry SHACKLES_LEGS = entry("shackles_legs", ShacklesLegsRestraint::new);
    public static final Entry DUCK_TAPE_LEGS = entry("duck_tape_legs", DuckTapeLegsRestraint::new);
    // Supporter only restraints:
    public static final Entry FUZZY_HANDCUFFS = entry("fuzzy_handcuffs", FuzzyHandcuffsRestraint::new);

    /** Replacement for Forge's RegistryObject: id is known immediately, the instance is created on register. */
    public static final class Entry {
        private final Identifier id;
        private final Supplier<AbstractRestraint> factory;
        private AbstractRestraint value;

        Entry(Identifier id, Supplier<AbstractRestraint> factory) {
            this.id = id;
            this.factory = factory;
        }

        public Identifier getId() {
            return id;
        }

        public AbstractRestraint get() {
            if (value == null)
                value = factory.get();
            return value;
        }
    }

    static Entry entry(String name, Supplier<AbstractRestraint> factory) {
        Entry e = new Entry(Identifier.fromNamespaceAndPath(CuffedMod.MODID, name), factory);
        ENTRIES.add(e);
        return e;
    }

    public static void register() {
        if (isInitialized)
            throw new IllegalStateException("Restraints already initialized");
        for (Entry e : ENTRIES)
            RestraintAPI.Registries.register(e.getId(), e.get());
        isInitialized = true;
        CuffedMod.LOGGER.info("Registered restraints");
    }
}
