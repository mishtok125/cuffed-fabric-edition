package com.lazrproductions.cuffed.mixin;


import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lazrproductions.cuffed.api.CuffedAPI;
import com.lazrproductions.cuffed.cap.RestrainableCapability;
import com.lazrproductions.cuffed.client.gui.screen.GenericScreen;
import com.lazrproductions.cuffed.entity.base.IDetainableEntity;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void cuffed$keyPress(long handle, int action, KeyEvent event, CallbackInfo callback) {
        Minecraft inst = Minecraft.getInstance();
        int keyCode = event.key();

        // Former ModClientBusEvents#onKeyInput: forward input to the mod's own screens.
        if (inst.gui.screen() instanceof GenericScreen sc) {
            sc.handleKeyAction(keyCode, action);
            if (keyCode == 256 && action == 1) {
                sc.onClose();
                inst.gui.setScreen(null);
                callback.cancel();
                return;
            }
        }

        LocalPlayer player = inst.player;
        if (player != null && inst.gui.screen() == null) {
            RestrainableCapability cap = (RestrainableCapability) CuffedAPI.Capabilities.getRestrainableCapability(player);
            if (cap != null && cap.isRestrained()) {
                cap.onKeyInput(player, keyCode, action);
                for (int i : cap.gatherBlockedInputs()) {
                    if (keyCode == i && (action == 1 || action == 2)) {
                        callback.cancel();
                        return;
                    }
                }
            }
            if (player instanceof IDetainableEntity detainable && detainable.getDetained() > -1)
                for (int i : getBlockedKeyCodesWhenDetained()) {
                    if (keyCode == i && (action == 1 || action == 2)) {
                        callback.cancel();
                        return;
                    }
                }
        }
    }

    private static ArrayList<Integer> getBlockedKeyCodesWhenDetained() {
        ArrayList<Integer> b = new ArrayList<Integer>();
        Minecraft inst = Minecraft.getInstance();
        if (inst == null || inst.options == null)
            return b;
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyUp).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyDown).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyLeft).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyRight).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyJump).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyAttack).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyUse).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyInventory).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyDrop).getValue());
        for (var i : inst.options.keyHotbarSlots)
            b.add(KeyMappingHelper.getBoundKeyOf(i).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keyPickItem).getValue());
        b.add(KeyMappingHelper.getBoundKeyOf(inst.options.keySwapOffhand).getValue());
        return b;
    }
}
