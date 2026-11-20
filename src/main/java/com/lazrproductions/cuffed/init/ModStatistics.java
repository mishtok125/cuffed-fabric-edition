package com.lazrproductions.cuffed.init;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.lazrproductions.cuffed.CuffedMod;
import com.lazrproductions.cuffed.restraints.base.AbstractRestraint;
import com.lazrproductions.cuffed.restraints.custom.FuzzyHandcuffsRestraint;
import com.lazrproductions.cuffed.restraints.custom.HandcuffsArmsRestraint;
import com.lazrproductions.cuffed.restraints.custom.HandcuffsLegsRestraint;
import com.lazrproductions.cuffed.restraints.custom.ShacklesArmsRestraint;
import com.lazrproductions.cuffed.restraints.custom.ShacklesLegsRestraint;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;

public class ModStatistics {
	private static final List<Runnable> RUN_IN_SETUP = new ArrayList<>();


	public static final Identifier HANDCUFFS_TIME_RESTRAINED = registerCustomStat("handcuffs_times_restrained", StatFormatter.DEFAULT);
	public static final Identifier HANDCUFFS_BROKEN = registerCustomStat("handcuffs_broken", StatFormatter.DEFAULT);
	public static final Identifier HANDCUFFS_TIME_SPENT_RESTRAINED = registerCustomStat("handcuffs_time_spent_restrained", StatFormatter.TIME);

	public static final Identifier FUZZY_HANDCUFFS_TIME_RESTRAINED = registerCustomStat("fuzzy_handcuffs_times_restrained", StatFormatter.DEFAULT);
	public static final Identifier FUZZY_HANDCUFFS_BROKEN = registerCustomStat("fuzzy_handcuffs_broken", StatFormatter.DEFAULT);
	public static final Identifier FUZZY_HANDCUFFS_TIME_SPENT_RESTRAINED = registerCustomStat("fuzzy_handcuffs_time_spent_restrained", StatFormatter.TIME);

	public static final Identifier SHACKLES_TIME_RESTRAINED = registerCustomStat("shackles_times_restrained", StatFormatter.DEFAULT);
	public static final Identifier SHACKLES_BROKEN = registerCustomStat("shackles_broken", StatFormatter.DEFAULT);
	public static final Identifier SHACKLES_TIME_SPENT_RESTRAINED = registerCustomStat("shackles_time_spent_restrained", StatFormatter.TIME);


	public static final Identifier LEGCUFFS_TIME_RESTRAINED = registerCustomStat("legcuffs_times_restrained", StatFormatter.DEFAULT);
	public static final Identifier LEGCUFFS_BROKEN = registerCustomStat("legcuffs_broken", StatFormatter.DEFAULT);
	public static final Identifier LEGCUFFS_TIME_SPENT_RESTRAINED = registerCustomStat("legcuffs_time_spent_restrained", StatFormatter.TIME);

	public static final Identifier LEG_SHACKLES_TIME_RESTRAINED = registerCustomStat("leg_shackles_times_restrained", StatFormatter.DEFAULT);
	public static final Identifier LEG_SHACKLES_BROKEN = registerCustomStat("leg_shackles_broken", StatFormatter.DEFAULT);
	public static final Identifier LEG_SHACKLES_TIME_SPENT_RESTRAINED = registerCustomStat("leg_shackles_time_spent_restrained", StatFormatter.TIME);
	

	public static final Identifier TIMES_NICKNAMED = registerCustomStat("times_nicknamed", StatFormatter.DEFAULT);


	public static final Identifier SUCCESSFUL_LOCKPICKS = registerCustomStat("successful_lockpicks", StatFormatter.DEFAULT);
	public static final Identifier LOCKPICKS_BROKEN = registerCustomStat("lockpicks_broken", StatFormatter.DEFAULT);

	
	public static final Identifier OPEN_SAFE = registerCustomStat("open_safe", StatFormatter.DEFAULT);


	public static void register()
	{
	}

	public static void setup()
	{
		RUN_IN_SETUP.forEach(Runnable::run);
	}

	private static Identifier registerCustomStat(String name, StatFormatter formatter)
	{
		Identifier regName = Identifier.fromNamespaceAndPath(CuffedMod.MODID, name);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, regName, regName);
		RUN_IN_SETUP.add(() -> Stats.CUSTOM.get(regName, formatter));
		return regName;
	}


	public static void awardRestraintItemUsed(@Nonnull ServerPlayer player, @Nonnull ItemStack stack) {
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()), 1);
	}
	public static void awardRestrained(@Nonnull ServerPlayer player, @Nonnull AbstractRestraint restraint) {
		if(restraint instanceof HandcuffsArmsRestraint)
			player.awardStat(HANDCUFFS_TIME_RESTRAINED, 1);
		else if(restraint instanceof FuzzyHandcuffsRestraint)
			player.awardStat(FUZZY_HANDCUFFS_TIME_RESTRAINED, 1);
		else if(restraint instanceof ShacklesArmsRestraint)
			player.awardStat(SHACKLES_TIME_RESTRAINED, 1);
		else if(restraint instanceof HandcuffsLegsRestraint)
			player.awardStat(LEGCUFFS_TIME_RESTRAINED, 1);
		else if(restraint instanceof ShacklesLegsRestraint)
			player.awardStat(LEG_SHACKLES_TIME_RESTRAINED, 1);
	}
	public static void awardRestraintBroken(@Nonnull ServerPlayer player, @Nonnull AbstractRestraint restraint) {
		if(restraint instanceof HandcuffsArmsRestraint)
			player.awardStat(HANDCUFFS_BROKEN, 1);
		else if(restraint instanceof FuzzyHandcuffsRestraint)
			player.awardStat(FUZZY_HANDCUFFS_BROKEN, 1);
		else if(restraint instanceof ShacklesArmsRestraint)
			player.awardStat(SHACKLES_BROKEN, 1);
		else if(restraint instanceof HandcuffsLegsRestraint)
			player.awardStat(LEGCUFFS_BROKEN, 1);
		else if(restraint instanceof ShacklesLegsRestraint)
			player.awardStat(LEG_SHACKLES_BROKEN, 1);
	}
	public static void awardTimeSpentRestrained(@Nonnull ServerPlayer player, @Nonnull AbstractRestraint restraint) {
		if(restraint instanceof HandcuffsArmsRestraint)
			player.awardStat(HANDCUFFS_TIME_SPENT_RESTRAINED, 1);
		else if(restraint instanceof FuzzyHandcuffsRestraint)
			player.awardStat(FUZZY_HANDCUFFS_TIME_SPENT_RESTRAINED, 1);
		else if(restraint instanceof ShacklesArmsRestraint)
			player.awardStat(SHACKLES_TIME_SPENT_RESTRAINED, 1);
		else if(restraint instanceof HandcuffsLegsRestraint)
			player.awardStat(LEGCUFFS_TIME_SPENT_RESTRAINED, 1);
		else if(restraint instanceof ShacklesLegsRestraint)
			player.awardStat(LEG_SHACKLES_TIME_SPENT_RESTRAINED, 1);
	}
}
