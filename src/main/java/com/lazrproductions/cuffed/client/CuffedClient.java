package com.lazrproductions.cuffed.client;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.blocks.entity.renderer.GuillotineBlockEntityRenderer;
import com.lazrproductions.cuffed.blocks.entity.renderer.TrayBlockEntityRenderer;
import com.lazrproductions.cuffed.client.gui.screen.FriskingScreen;
import com.lazrproductions.cuffed.client.particle.BloodDripParticle;
import com.lazrproductions.cuffed.entity.renderer.ChainKnotEntityRenderer;
import com.lazrproductions.cuffed.entity.renderer.CrumblingBlockRenderer;
import com.lazrproductions.cuffed.entity.renderer.PadlockEntityRenderer;
import com.lazrproductions.cuffed.entity.renderer.WeightedAnchorEntityRenderer;
import com.lazrproductions.cuffed.event.ModClientEvents;
import com.lazrproductions.cuffed.init.ModBlockEntities;
import com.lazrproductions.cuffed.init.ModEntityTypes;
import com.lazrproductions.cuffed.init.ModMenuTypes;
import com.lazrproductions.cuffed.init.ModModelLayers;
import com.lazrproductions.cuffed.init.ModParticleTypes;
import com.lazrproductions.cuffed.inventory.tooltip.PossessionsBoxTooltip;
import com.lazrproductions.cuffed.inventory.tooltip.TrayTooltip;
import com.lazrproductions.cuffed.restraints.client.layer.PilloryEntityLayer;
import com.lazrproductions.cuffed.restraints.client.layer.RestraintEntityLayer;
import com.lazrproductions.cuffed.utils.RegistryUtil;

import java.util.ArrayList;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;

public class CuffedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CuffedMod.LOGGER.info("Running client setup for Cuffed");

        RegistryUtil.setClientRegistries(() -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.level != null ? mc.level.registryAccess() : null;
        });

        CuffedAPI.Networking.NETWORK.registerClientReceivers();

        CuffedItemModelProperties.register();
        ModModelLayers.registerLayers();

        MenuScreens.register(ModMenuTypes.FRISKING_MENU, FriskingScreen::new);

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            // Creative search indexes tooltip text on a worker thread.
            if (!RenderSystem.isOnRenderThread())
                return;

            if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(CuffedMod.MODID))
                return;

            Minecraft minecraft = Minecraft.getInstance();
            int width = Math.max(1, Math.min(240, minecraft.getWindow().getGuiScaledWidth() - 24));
            var wrappedLines = new ArrayList<Component>();
            for (Component line : lines) {
                if (minecraft.font.width(line) <= width) {
                    wrappedLines.add(line);
                    continue;
                }

                for (var part : minecraft.font.getSplitter().splitLines(line, width, Style.EMPTY)) {
                    var wrappedLine = Component.empty();
                    part.visit((style, text) -> {
                        wrappedLine.append(Component.literal(text).withStyle(style));
                        return Optional.empty();
                    }, Style.EMPTY);
                    wrappedLines.add(wrappedLine);
                }
            }
            lines.clear();
            lines.addAll(wrappedLines);
        });

        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof PossessionsBoxTooltip.Data d)
                return new PossessionsBoxTooltip(d);
            if (data instanceof TrayTooltip.Data d)
                return new TrayTooltip(d);
            return null;
        });

        ParticleProviderRegistry.getInstance().register(ModParticleTypes.BLOOD_DRIP_FALL_PARTICLE,
                BloodDripParticle.Provider::new);

        EntityRendererRegistry.register(ModEntityTypes.CHAIN_KNOT, ChainKnotEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.PADLOCK, PadlockEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.WEIGHTED_ANCHOR, WeightedAnchorEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CRUMBLING_BLOCK, CrumblingBlockRenderer::new);

        BlockEntityRendererRegistry.register(ModBlockEntities.GUILLOTINE, GuillotineBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.TRAY, TrayBlockEntityRenderer::new);

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatar) {
                helper.register(new RestraintEntityLayer(avatar, context));
                helper.register(new PilloryEntityLayer(avatar));
            }
        });

        ModClientEvents.register();
        WardenGuide.register();
    }
}
