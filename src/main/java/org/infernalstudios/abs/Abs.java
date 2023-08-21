package org.infernalstudios.abs;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Abs.MODID)
public class Abs {
	public static final String MODID = "abs";
	public static Map<ResourceLocation, AbsSpawningRules> abs = new HashMap<>();
	public static final Logger LOGGER = LogUtils.getLogger();

	public Abs() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void addReloadListenerEvent(AddReloadListenerEvent event) {
		event.addListener(new AbsLoader());
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public void checkSpawns(CheckSpawn event) {
		LevelAccessor level = event.getLevel();
		Player nearestPlayer = level.getNearestPlayer(TargetingConditions.forNonCombat().range(64.0D), event.getEntity().position().x, event.getEntity().position().y, event.getEntity().position().z);

		if (nearestPlayer != null && nearestPlayer instanceof ServerPlayer serverPlayer) {
			AbsSpawningRules removedRule = null;

			for (ResourceLocation advancement : abs.keySet()) {
				AbsSpawningRules rules = abs.get(advancement);
				boolean wantsToDeny = false;
				if (rules.excludeRules.types.contains(event.getSpawnReason())) {
					if ((hasAdvancement(serverPlayer, advancement) ? rules.excludeRules.with : rules.excludeRules.without).contains(Registry.ENTITY_TYPE.getKey(event.getEntity().getType()))) {
						wantsToDeny = true;

						if (removedRule != null) {
							removedRule = AbsSpawningRules.handleMerge(rules, removedRule);
						} else {
							removedRule = rules;
						}
					}
				}

				if (rules.includeRules.types.contains(event.getSpawnReason())) {
					if ((hasAdvancement(serverPlayer, advancement) ? rules.includeRules.with : rules.includeRules.without).contains(Registry.ENTITY_TYPE.getKey(event.getEntity().getType()))) {
						if (removedRule != null) {
							if (rules.getPriority() < removedRule.getPriority()) {
								wantsToDeny = false;
							}
						}
					}
				}

				if (wantsToDeny) {
					event.setResult(Result.DENY);
				}
			}
		}
	}

	private static boolean hasAdvancement(ServerPlayer serverPlayer, ResourceLocation adv) {
		Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(adv);
		return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
