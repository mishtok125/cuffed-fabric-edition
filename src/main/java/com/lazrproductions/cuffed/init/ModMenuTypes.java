package com.lazrproductions.cuffed.init;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.inventory.FriskingMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final MenuType<FriskingMenu> FRISKING_MENU = Registry.register(BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(CuffedMod.MODID, "frisking_menu"),
            new MenuType<>(FriskingMenu::new, FeatureFlags.VANILLA_SET));

    public static void register() {
    }
}
