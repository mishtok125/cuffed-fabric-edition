package com.lazrproductions.cuffed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lazrproductions.cuffed.effect.RestrainedEffect;
import com.lazrproductions.cuffed.effect.WoundedEffect;

import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;

@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {
    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void cuffed$getEffectName(MobEffectInstance effect, CallbackInfoReturnable<Component> callback) {
        if (effect.getEffect().value() instanceof WoundedEffect) {
            MutableComponent c = effect.getEffect().value().getDisplayName().copy();
            c.append(CommonComponents.SPACE).append(Component.literal(effect.getAmplifier() + "%"));
            callback.setReturnValue(c);
        } else if (effect.getEffect().value() instanceof RestrainedEffect) {
            callback.setReturnValue(effect.getEffect().value().getDisplayName().copy());
        }
    }
}
