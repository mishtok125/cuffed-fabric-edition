package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.event.ModServerEvents;
import com.mojang.brigadier.ParseResults;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** Replacement for the Forge CommandEvent handler. */
@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true)
    private void cuffed$performCommand(ParseResults<CommandSourceStack> command, String commandString,
            CallbackInfo callback) {
        ServerPlayer player = command.getContext().getSource().getPlayer();
        if (player != null && ModServerEvents.shouldBlockCommand(player))
            callback.cancel();
    }
}
