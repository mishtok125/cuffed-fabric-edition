package com.lazrproductions.cuffed.restraints.base;


import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import java.util.ArrayList;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.lazrslib.client.screen.ScreenUtilities;
import com.lazrproductions.lazrslib.client.screen.base.BlitCoordinates;
import com.lazrproductions.lazrslib.client.screen.base.ScreenTexture;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractArmRestraint extends AbstractRestraint {

    static final Identifier WIDGETS = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "textures/gui/widgets.png");

    static final ScreenTexture ARMS_ICON = new ScreenTexture(WIDGETS, 76, 24, 16, 16, 192, 192);

    public AbstractArmRestraint(){}
    public AbstractArmRestraint(ItemStack stack, ServerPlayer player, ServerPlayer captor) {
        super(stack, player, captor);
    }

    public RestraintType getType() {
        return RestraintType.Arm;
    }

    public ArrayList<Integer> getBlockedKeyCodes() {
        ArrayList<Integer> b = new ArrayList<Integer>();
        Minecraft inst = Minecraft.getInstance();
        
        if(inst == null || inst.options == null)
        return b;

        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyAttack).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyUse).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyInventory).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyDrop).getValue());
        for (var i : inst.options.keyHotbarSlots) {
            b.add(KeyMappingHelper.getBoundKeyOf(i).getValue());
        }
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyInventory).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyPickItem).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keySwapOffhand).getValue());

        for (KeyMapping mapping : inst.options.keyMappings) {
            switch (mapping.getName()) {
                case "key.parcool.Crawl":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.Breakfall":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.WallSlide":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.Vault":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.Flipping":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.FastRun":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.ClingToCliff":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.HangDown":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.WallJump":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
                case "key.parcool.HorizontalWallRun":
                    b.add(KeyMappingHelper.getBoundKeyOf(mapping).getValue());
                    break;
            }
        }

        return b;
    }

    public void renderOverlay(Player player, GuiGraphicsExtractor graphics, float partialTick, Window window) {
        ScreenUtilities.setColor(1, 1, 1, 1);

        int screenWidth = (int) (16 * 1.75f);
        int screenHeight = (int) (16 * 1.75f);
        int x = (window.getGuiScaledWidth() / 2) - (screenWidth / 2);
        int y = (window.getGuiScaledHeight() / 2) - (screenHeight) - 65;
        ScreenUtilities.drawTexture(graphics, new BlitCoordinates(x, y, screenWidth, screenHeight), ARMS_ICON);

        ArrayList<Component> c = new ArrayList<>();
        c.add(Component.translatable(getActionBarLabel()));
        ScreenUtilities.renderLabel(Minecraft.getInstance(), graphics, window.getGuiScaledWidth() / 2, y + screenHeight, c, 16579836);
    }
}
