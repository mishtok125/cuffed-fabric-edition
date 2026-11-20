package com.lazrproductions.cuffed.client;

import java.util.ArrayList;
import java.util.List;

import com.lazrproductions.cuffed.init.ModItems;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;

public final class WardenGuide {
    private static final String PREFIX = "guide.cuffed.";
    private static final int PAGE_WIDTH = 114;
    private static final int PAGE_LINES = 13;

    private WardenGuide() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide() || player.isSpectator()
                    || !player.getItemInHand(hand).is(ModItems.INFORMATION_BOOKLET))
                return InteractionResult.PASS;

            Minecraft minecraft = Minecraft.getInstance();
            List<Component> pages = new ArrayList<>();
            addPages(pages, Component.translatable("item.cuffed.information_booklet").getString(),
                    text("title_page.info"));
            addPages(pages, text("restraints_page.title"),
                    text("arm_restraints_page.info") + "\n\n" + text("leg_restraints_page.info"));
            for (String section : List.of("escorting", "anchoring", "reinforced", "locks", "keys",
                    "copying_keys", "lockpicking", "pillory", "guillotine", "prisoner_tag")) {
                String key = section + "_page.";
                String info = text(key + "info");
                if (section.equals("anchoring"))
                    info += " " + text(key + "fence") + " " + text(key + "info2") + " " + text(key + "info3");
                if (section.equals("locks"))
                    info += " " + Component.translatable("item.cuffed.key").getString()
                            + " " + text(key + "info2") + "\n\n" + text(key + "info4") + " " + text(key + "info5");
                if (section.equals("keys"))
                    info += " " + Component.translatable("item.cuffed.key_mold").getString() + ".";
                addPages(pages, text(key + "title"), info);
            }
            addPages(pages, text("bundle_page.title"), text("bundle_page.info"));
            minecraft.gui.setScreen(new BookViewScreen(new BookViewScreen.BookAccess(pages)));
            return InteractionResult.CONSUME;
        });
    }

    private static String text(String key) {
        return Component.translatable(PREFIX + key).getString();
    }

    private static void addPages(List<Component> pages, String title, String info) {
        var splitter = Minecraft.getInstance().font.getSplitter();
        var lines = splitter.splitLines(Component.literal(title + "\n\n" + info), PAGE_WIDTH, Style.EMPTY);
        for (int start = 0; start < lines.size(); start += PAGE_LINES) {
            var page = Component.empty();
            for (int index = start; index < Math.min(start + PAGE_LINES, lines.size()); index++) {
                if (index > start)
                    page.append("\n");
                page.append(lines.get(index).getString().stripTrailing());
            }
            pages.add(page);
        }
    }
}
