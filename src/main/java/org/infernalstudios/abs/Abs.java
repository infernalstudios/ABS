package org.infernalstudios.abs;

import java.util.HashMap;
import java.util.Map;

import org.infernalstudios.abs.rules.AdvancementRule;
import org.infernalstudios.abs.rules.SpawningRules;
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
	public static Map<ResourceLocation, AdvancementRule> abs = new HashMap<>();
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
			AdvancementRule removedRule = null;
			boolean wantsToDeny = false;

			for (ResourceLocation advancement : abs.keySet()) {
				AdvancementRule advancementRule = abs.get(advancement);
				SpawningRules rules = hasAdvancement(serverPlayer, advancement) ? advancementRule.getWith() : advancementRule.getWithout();

				if (rules.getExcluded().getTypes().contains(event.getSpawnReason())) {
					if (rules.getExcluded().getEntities().contains(Registry.ENTITY_TYPE.getKey(event.getEntity().getType()))) {
						wantsToDeny = true;

						if (removedRule != null) {
							removedRule = AdvancementRule.handleMerge(advancementRule, removedRule);
						} else {
							removedRule = advancementRule;
						}
					}
				}
			}

			for (ResourceLocation advancement : abs.keySet()) {
				AdvancementRule advancementRule = abs.get(advancement);
				SpawningRules rules = hasAdvancement(serverPlayer, advancement) ? advancementRule.getWith() : advancementRule.getWithout();

				if (rules.getIncluded().getTypes().contains(event.getSpawnReason())) {
					if (rules.getIncluded().getEntities().contains(Registry.ENTITY_TYPE.getKey(event.getEntity().getType()))) {
						if (removedRule != null) {
							if (advancementRule.getPriority() > removedRule.getPriority()) {
								wantsToDeny = false;
							}
						}
					}
				}
			}

			if (wantsToDeny) {
				event.setResult(Result.DENY);
			}
		}
	}

	private static boolean hasAdvancement(ServerPlayer serverPlayer, ResourceLocation adv) {
		Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(adv);
		return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
	}
}
