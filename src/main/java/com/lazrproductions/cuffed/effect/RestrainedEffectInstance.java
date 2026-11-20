package com.lazrproductions.cuffed.effect;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.init.ModEffects;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * The restrained effect stores which actions are disabled as bit flags in its amplifier.
 * The attribute changes themselves are applied by {@link RestrainedEffect}.
 */
public class RestrainedEffectInstance extends MobEffectInstance {
    public static final Identifier NEGATIVE_MINE_SPEED_ID = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "restrain_mine_speed");
    public static final Identifier NEGATIVE_MOVE_SPEED_ID = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "restrain_movement_speed");
    public static final Identifier NEGATIVE_JUMP_ID = Identifier.fromNamespaceAndPath(CuffedMod.MODID, "restrain_jump_strength");

    public boolean noMining;
    public boolean noItemUse;
    public boolean noMovement;
    public boolean noJumping;

    public RestrainedEffectInstance(int duration, int restraintType) {
        super(ModEffects.RESTRAINED_EFFECT, duration, restraintType, false, false, true);
        boolean[] decodedValues = decodeRestraintProperties(restraintType);
        this.noMining = decodedValues[0];
        this.noItemUse = decodedValues[1];
        this.noMovement = decodedValues[2];
        this.noJumping = decodedValues[3];
    }

    public static int encodeRestraintProperties(boolean noMining, boolean noItemUse, boolean noMovement, boolean noJumping) {
        int encodedValue = 0;
        if (noMining) encodedValue |= 1 << 0;
        if (noItemUse) encodedValue |= 1 << 1;
        if (noMovement) encodedValue |= 1 << 2;
        if (noJumping) encodedValue |= 1 << 3;
        return encodedValue;
    }

    public static boolean[] decodeRestraintProperties(int v) {
        boolean[] decodedValues = new boolean[4];
        decodedValues[0] = (v & (1 << 0)) != 0;
        decodedValues[1] = (v & (1 << 1)) != 0;
        decodedValues[2] = (v & (1 << 2)) != 0;
        decodedValues[3] = (v & (1 << 3)) != 0;
        return decodedValues;
    }

    public static boolean decodeNoMining(int v) {
        return decodeRestraintProperties(v)[0];
    }

    public static boolean decodeNoItemUse(int v) {
        return decodeRestraintProperties(v)[1];
    }

    public static boolean decodeNoMovement(int v) {
        return decodeRestraintProperties(v)[2];
    }

    public static boolean decodeNoJumping(int v) {
        return decodeRestraintProperties(v)[3];
    }
}
