package com.lazrproductions.cuffed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.blocks.base.PosterType;
import com.lazrproductions.cuffed.command.CuffedDebugCommand;
import com.lazrproductions.cuffed.command.HandcuffCommand;
import com.lazrproductions.cuffed.config.CuffedServerConfig;
import com.lazrproductions.cuffed.event.ModServerEvents;
import com.lazrproductions.cuffed.init.ModBlockEntities;
import com.lazrproductions.cuffed.init.ModBlocks;
import com.lazrproductions.cuffed.init.ModCreativeTabs;
import com.lazrproductions.cuffed.init.ModEffects;
import com.lazrproductions.cuffed.init.ModEntityTypes;
import com.lazrproductions.cuffed.init.ModItems;
import com.lazrproductions.cuffed.init.ModMenuTypes;
import com.lazrproductions.cuffed.init.ModParticleTypes;
import com.lazrproductions.cuffed.init.ModRecipes;
import com.lazrproductions.cuffed.init.ModRestraints;
import com.lazrproductions.cuffed.init.ModSounds;
import com.lazrproductions.cuffed.init.ModStatistics;
import com.lazrproductions.cuffed.items.base.AbstractRestraintItem;
import com.lazrproductions.cuffed.utils.RegistryUtil;
import com.lazrproductions.lazrslib.common.config.LazrConfig;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

public class CuffedMod implements ModInitializer {
    public static final String MODID = "cuffed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final CuffedServerConfig SERVER_CONFIG = new CuffedServerConfig(MODID, LazrConfig.Type.SERVER);

    // The Forge version had optional integrations with Forge-only mods (Better Combat, Epic Fight, Parcool,
    // PlayerRevive, ...). None of them are loaded here, so these always stay false.
    public static boolean PlayerReviveInstalled = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Cuffed");
        SERVER_CONFIG.registerConfig();

        ModSounds.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModEntityTypes.register();
        ModParticleTypes.register();
        ModCreativeTabs.register();
        ModRecipes.register();
        ModEffects.register();
        ModStatistics.register();
        ModMenuTypes.register();
        ModRestraints.register();

        CuffedAPI.Networking.registerPackets();
        ModStatistics.setup();
        ModServerEvents.register();

        ServerLifecycleEvents.SERVER_STARTING.register(RegistryUtil::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RegistryUtil.setServer(null));

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
            new HandcuffCommand(dispatcher, buildContext);
            new CuffedDebugCommand(dispatcher, buildContext);
        });

        // Register Dispenser
        DispenseItemBehavior dispenseitembehavior = new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                this.setSuccess(AbstractRestraintItem.dispenseRestraint(source, stack));
                if (this.isSuccess())
                    stack.shrink(1);
                return stack;
            }
        };
        DispenserBlock.registerBehavior(ModItems.HANDCUFFS, dispenseitembehavior);
        DispenserBlock.registerBehavior(ModItems.FUZZY_HANDCUFFS, dispenseitembehavior);
        DispenserBlock.registerBehavior(ModItems.SHACKLES, dispenseitembehavior);
        DispenserBlock.registerBehavior(Items.BUNDLE, dispenseitembehavior);

        PosterType.values(); // force enum init
    }
}
