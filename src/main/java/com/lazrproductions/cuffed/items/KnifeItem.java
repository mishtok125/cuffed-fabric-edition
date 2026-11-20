package com.lazrproductions.cuffed.items;

import com.lazrproductions.cuffed.effect.WoundedEffect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class KnifeItem extends Item {
    public KnifeItem(Properties p) {
        super(p);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        WoundedEffect.woundEntity(target, 20);
        super.hurtEnemy(stack, target, attacker);
    }
}
