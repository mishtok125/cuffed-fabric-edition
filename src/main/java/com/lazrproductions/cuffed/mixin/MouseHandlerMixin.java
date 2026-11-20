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

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void cuffed$onButton(long handle, MouseButtonInfo info, int action, CallbackInfo callback) {
        Minecraft inst = Minecraft.getInstance();
        int keyCode = info.button();

        // Former ModClientBusEvents#onMouseInput
        if (inst.gui.screen() instanceof GenericScreen sc)
            sc.handleMouseAction(keyCode, action);

        LocalPlayer player = inst.player;
        if (player != null && inst.gui.screen() == null) {
            RestrainableCapability cap = (RestrainableCapability) CuffedAPI.Capabilities.getRestrainableCapability(player);
            if (cap != null && cap.isRestrained()) {
                cap.onMouseInput(player, keyCode, action);
                for (int i : cap.gatherBlockedInputs()) {
                    if (keyCode == i) {
                        callback.cancel();
                        return;
                    }
                }
            }
            if (player instanceof IDetainableEntity detainable && detainable.getDetained() > -1)
                for (int i : getBlockedKeyCodesWhenDetained()) {
                    if (keyCode == i) {
                        callback.cancel();
                        return;
                    }
                }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void cuffed$onScroll(long handle, double xoffset, double yoffset, CallbackInfo callback) {
        Minecraft inst = Minecraft.getInstance();
        LocalPlayer player = inst.player;
        if (player != null && inst.gui.screen() == null) {
            RestrainableCapability cap = (RestrainableCapability) CuffedAPI.Capabilities.getRestrainableCapability(player);
            if (cap != null && cap.armsRestrained()) {
                callback.cancel();
                return;
            }
            if (player instanceof IDetainableEntity detainable && detainable.getDetained() > -1)
                callback.cancel();
        }
    }

    private static ArrayList<Integer> getBlockedKeyCodesWhenDetained() {
        ArrayList<Integer> b = new ArrayList<Integer>();
        Minecraft inst = Minecraft.getInstance();
        if (inst == null || inst.options == null)
            return b;
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
