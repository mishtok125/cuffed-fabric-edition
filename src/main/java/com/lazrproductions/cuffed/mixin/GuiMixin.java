package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.event.ModClientEvents;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

/** Replacement for Forge's ScreenEvent.Opening: restrained players cannot open their inventory. */
@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void cuffed$setScreen(Screen screen, CallbackInfo callback) {
        if (ModClientEvents.shouldBlockScreen(screen))
            callback.cancel();
    }
}
