package com.lazrproductions.lazrslib.client.screen;

import java.util.List;

import javax.annotation.Nonnull;

import com.lazrproductions.lazrslib.client.screen.base.BlitCoordinates;
import com.lazrproductions.lazrslib.client.screen.base.ScreenRect;
import com.lazrproductions.lazrslib.client.screen.base.ScreenTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Port of Lazr's Lib screen helpers to the 26.x GUI extraction API.
 * <p>
 * The old {@code GuiGraphics#setColor} global tint no longer exists, so the tint is kept here
 * and applied to every texture drawn through these helpers.
 */
public class ScreenUtilities {
    static final Identifier GENERIC_PROGRESS_BAR_LOCATION = Identifier.fromNamespaceAndPath("cuffed", "textures/gui/progress_bar.png");

    private static int tint = -1;

    /** Replacement for the removed {@code GuiGraphics#setColor}. Values are 0..1 (values above 1 are clamped). */
    public static void setColor(float r, float g, float b, float a) {
        tint = ARGB.colorFromFloat(Mth.clamp(a, 0, 1), Mth.clamp(r, 0, 1), Mth.clamp(g, 0, 1), Mth.clamp(b, 0, 1));
    }

    public static void resetColor() {
        tint = -1;
    }

    public static int getColor() {
        return tint;
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, BlitCoordinates pos, ScreenTexture texture) {
        blit(graphics, texture.getResourceLocation(), pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(),
                texture.getU(), texture.getV(), texture.getBoundsX(), texture.getBoundsY(), texture.getWidth(),
                texture.getHeight());
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, BlitCoordinates pos, float rotation, ScreenTexture texture) {
        drawTexture(graphics, pos, rotation, (pos.getWidth() / 2f), (pos.getHeight() / 2f), texture);
    }

    public static void drawTexture(GuiGraphicsExtractor graphics, BlitCoordinates pos, float rotation, float rotateAroundX,
            float rotateAroundY, ScreenTexture texture) {
        graphics.pose().pushMatrix();
        graphics.pose().rotateAbout(rotation * Mth.DEG_TO_RAD, pos.getX() + rotateAroundX, pos.getY() + rotateAroundY);
        drawTexture(graphics, pos, texture);
        graphics.pose().popMatrix();
    }

    /** Same argument order as the old {@code GuiGraphics#blit(location, x, y, w, h, u, v, uW, vH, texW, texH)}. */
    public static void blit(GuiGraphicsExtractor graphics, Identifier location, int x, int y, int width, int height, float u,
            float v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, location, x, y, u, v, width, height, uWidth, vHeight, textureWidth,
                textureHeight, tint);
    }

    public static void drawProgressBar(GuiGraphicsExtractor graphics, BlitCoordinates pos, ScreenTexture texture, float progress,
            int totalFramesHorizontal, int totalFramesVertically) {
        int frames = (totalFramesHorizontal * totalFramesVertically);
        int frameIndex = Mth.floor((float) frames * progress);
        int column = Mth.floor((float) frameIndex % (float) totalFramesHorizontal);
        int row = Mth.floor((float) frameIndex / (float) totalFramesHorizontal);
        float uvXF = texture.getU() + (column * texture.getBoundsX());
        float uvYF = texture.getV() + (row * texture.getBoundsY());
        blit(graphics, texture.getResourceLocation(), pos.getX(), pos.getY(), pos.getWidth(), pos.getHeight(), uvXF,
                uvYF, texture.getBoundsX(), texture.getBoundsY(), texture.getWidth(), texture.getHeight());
    }

    public static void drawGenericProgressBar(GuiGraphicsExtractor graphics, BlitCoordinates pos, float progress) {
        graphics.fill(pos.getX(), pos.getY(), pos.getX() + pos.getWidth(), pos.getY() + 2, 1325400064);
        int i = Mth.hsvToRgb(progress / 3.0F, 1.0F, 1.0F);
        graphics.fill(pos.getX(), pos.getY(), pos.getX() + (int) (pos.getWidth() * progress), pos.getY() + 1,
                i | -16777216);
    }

    public static void drawGenericProgressBarUpright(GuiGraphicsExtractor graphics, BlitCoordinates pos, float progress,
            float partialTick) {
        progress = Mth.clamp(progress, 0, 2);
        int shakeX = 0;
        if (progress > 1)
            shakeX = Mth.floor(Mth.sin(partialTick * (100f * (progress - 1)) * 2f) - 1);
        graphics.fill(pos.getX() + shakeX, pos.getY(), pos.getX() + 2 + shakeX, pos.getY() + pos.getHeight(),
                1325400064);
        int i = Mth.hsvToRgb(progress / 3.0F, 1.0F, 1.0F);
        progress = Mth.clamp(progress, 0, 1f);
        graphics.fill(pos.getX() + shakeX, pos.getY() + pos.getHeight(), pos.getX() + 1 + shakeX,
                pos.getY() + pos.getHeight() - (int) (pos.getHeight() * progress), i | -16777216);
    }

    public static void renderLabel(Minecraft instance, GuiGraphicsExtractor graphics, int x, int y, List<Component> list,
            int color, boolean renderShadow) {
        int space = 15;
        for (int i = 0; i < list.size(); i++) {
            String text = list.get(i).getString();
            graphics.text(instance.font, text, x - instance.font.width(text) / 2,
                    y + ((list.size() / 2) * space + (space * i)), ARGB.opaque(color), renderShadow);
        }
    }

    public static void renderLabel(Minecraft instance, GuiGraphicsExtractor graphics, int x, int y, List<Component> list,
            int color) {
        renderLabel(instance, graphics, x, y, list, color, true);
    }

    public static void drawParagraph(Minecraft instance, GuiGraphicsExtractor graphics, int x, int y, List<Component> list,
            int maxWidth, int color, boolean renderShadow) {
        FormattedText text = FormattedText.composite(list);
        graphics.textWithWordWrap(instance.font, text, x, y, maxWidth, ARGB.opaque(color), renderShadow);
    }

    public static void drawParagraph(Minecraft instance, GuiGraphicsExtractor graphics, int x, int y, List<Component> list,
            int maxWidth, int color) {
        drawParagraph(instance, graphics, x, y, list, maxWidth, color, true);
    }

    public static void drawParagraph(Minecraft instance, GuiGraphicsExtractor graphics, BlitCoordinates pos,
            List<Component> list, int color) {
        drawParagraph(instance, graphics, pos.getX(), pos.getY(), list, pos.getWidth(), color, true);
    }

    public static void drawItemStack(@Nonnull Minecraft instance, @Nonnull GuiGraphicsExtractor graphics,
            @Nonnull ItemStack stack, int x, int y) {
        drawItemStack(instance, graphics, stack, x, y, 16);
    }

    public static void drawItemStack(@Nonnull Minecraft instance, @Nonnull GuiGraphicsExtractor graphics,
            @Nonnull ItemStack stack, int x, int y, int size) {
        if (stack.isEmpty())
            return;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(size / 16f, size / 16f);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    public static boolean mouseInArea(double mouseX, double mouseY, ScreenRect area) {
        return area.positionEnvlopes(mouseX, mouseY);
    }
}
