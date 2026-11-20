package com.lazrproductions.cuffed.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.inventory.FriskingMenu;
import com.lazrproductions.cuffed.inventory.FriskingSlot;
import com.lazrproductions.lazrslib.client.screen.ScreenUtilities;
import com.lazrproductions.lazrslib.client.screen.base.BlitCoordinates;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FriskingScreen extends AbstractContainerScreen<FriskingMenu> {
    private static final Identifier FRISKING_BACKGROUND = Identifier.fromNamespaceAndPath(CuffedMod.MODID,
            "textures/gui/container/frisking.png");

    private static final Integer TICKS_TO_TAKE = 40;

    public FriskingScreen(FriskingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 256);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractContents(graphics, mouseX, mouseY, partialTick);
        this.extractCarriedItem(graphics, mouseX, mouseY);

        if (mouseHeld) {
            graphics.nextStratum();
            ScreenUtilities.drawGenericProgressBar(graphics, new BlitCoordinates(mouseX - 8, mouseY - 4, 16, 4),
                    (float) mouseHeldTicks / (float) TICKS_TO_TAKE);
            ScreenUtilities.renderLabel(minecraft, graphics, mouseX, mouseY + 6,
                    List.of(Component.translatable("info.cuffed.frisk.taking")), 0xFFFFFF, true);
        } else
            this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            ItemStack itemstack = this.hoveredSlot.getItem();
            ArrayList<Component> tooltips = new ArrayList<Component>(this.getTooltipFromContainerItem(itemstack));
            if (hoveredSlot instanceof FriskingSlot)
                tooltips.add(Component.translatable("info.cuffed.frisk.tooltip").withStyle(ChatFormatting.GRAY));
            Optional<net.minecraft.world.inventory.tooltip.TooltipComponent> image = itemstack.getTooltipImage();
            graphics.setTooltipForNextFrame(this.font, tooltips, image, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, FRISKING_BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth,
                this.imageHeight, 256, 256);

        Minecraft instance = this.minecraft;
        if (instance != null) {
            ClientLevel level = instance.level;
            if (level != null && instance.player != null) {
                LivingEntity entity = null;
                for (Player p : level.players()) {
                    if (p.getDisplayName().getString().equals(this.title.getString()))
                        entity = p;
                }
                if (entity != null)
                    InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, i + 26, j + 1, i + 76, j + 71, 30,
                            0.0625F, mouseX, mouseY, entity);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040,
                false);
        graphics.text(this.font, this.title, this.titleLabelX + 110 - (this.font.width(this.title) / 2),
                this.titleLabelY + 20, 0xFF404040, false);
    }

    private boolean isHovering(Slot slot, double mouseX, double mouseY) {
        return this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    @Nullable
    private Slot findSlot(double x, double y) {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot, x, y) && slot.isActive())
                return slot;
        }
        return null;
    }

    boolean mouseHeld;
    int mouseHeldTicks;
    MouseButtonEvent heldEvent;
    Slot heldSlot;

    @Override
    protected void containerTick() {
        if (mouseHeld) {
            mouseHeldTicks++;
            if (mouseHeldTicks >= TICKS_TO_TAKE) {
                MouseButtonEvent event = heldEvent;
                mouseHeld = false;
                mouseHeldTicks = 0;
                heldSlot = null;
                heldEvent = null;
                if (event != null)
                    super.mouseClicked(event, false);
            }
        }
        super.containerTick();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Slot slot = this.findSlot(event.x(), event.y());
        if (slot instanceof FriskingSlot && event.button() == 0 && !slot.getItem().isEmpty()) {
            // Taking items from someone else takes time.
            mouseHeld = true;
            mouseHeldTicks = 0;
            heldEvent = event;
            heldSlot = slot;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        mouseHeld = false;
        mouseHeldTicks = 0;
        heldSlot = null;
        heldEvent = null;
        return super.mouseReleased(event);
    }
}
