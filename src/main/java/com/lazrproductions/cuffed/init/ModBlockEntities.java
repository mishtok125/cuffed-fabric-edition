package com.lazrproductions.cuffed.init;

import java.util.Set;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.blocks.entity.BunkBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.GuillotineBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.LockableBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.SafeBlockEntity;
import com.lazrproductions.cuffed.blocks.entity.TrayBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    public static final BlockEntityType<GuillotineBlockEntity> GUILLOTINE = register("guillotine_block_entity",
            GuillotineBlockEntity::new, ModBlocks.GUILLOTINE);
    public static final BlockEntityType<SafeBlockEntity> SAFE_BLOCK_ENTITY = register("safe_block_entity",
            SafeBlockEntity::new, ModBlocks.SAFE);
    public static final BlockEntityType<BunkBlockEntity> BUNK_BLOCK_ENTITY = register("bunk_block_entity",
            BunkBlockEntity::new, ModBlocks.BUNK);
    public static final BlockEntityType<TrayBlockEntity> TRAY = register("tray_block_entity",
            TrayBlockEntity::new, ModBlocks.TRAY);
    public static final BlockEntityType<LockableBlockEntity> CELL_DOOR_BLOCK_ENTITY = register("cell_door_block_entity",
            LockableBlockEntity::new, ModBlocks.CELL_DOOR);

    static <T extends BlockEntity> BlockEntityType<T> register(String name,
            BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(CuffedMod.MODID, name), new BlockEntityType<>(factory, Set.of(block)));
    }

    public static void register() {
    }
}
