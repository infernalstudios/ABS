package org.infernalstudios.abs.rules;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;

public class SpawningRules {
	FilterRules include;
	FilterRules exclude;

	public SpawningRules(FilterRules include, FilterRules exclude) {
		this.include = include;
		this.exclude = exclude;
	}

	public FilterRules getIncluded() {
		return include;
	}

	public FilterRules getExcluded() {
		return exclude;
	}

	@Override
	public String toString() {
		return "SpawningRules(Include: " + include + ", Exclude: " + exclude + ")";
	}

	public static SpawningRules handleMerge(AdvancementRule a, AdvancementRule b, boolean with) {
		return handleMerge(with ? a.with : a.without, a.priority, with ? b.with : b.without, b.priority);
	}

	public static SpawningRules handleMerge(SpawningRules a, int priorityA, SpawningRules b, int priorityB) {
		Set<ResourceLocation> mergedIncluded = new HashSet<ResourceLocation>();
		Set<ResourceLocation> mergedExcluded = new HashSet<ResourceLocation>();
		Set<MobSpawnType> mergedIncludedTypes = new HashSet<MobSpawnType>();
		Set<MobSpawnType> mergedExcludedTypes = new HashSet<MobSpawnType>();

		for (MobSpawnType type : a.include.types) {
			if (b.exclude.types.contains(type)) {
				if (priorityA > priorityB) {
					mergedIncludedTypes.add(type);
				} else {
					mergedExcludedTypes.add(type);
				}
			} else {
				mergedIncludedTypes.add(type);
			}
		}

		for (MobSpawnType type : a.exclude.types) {
			if (b.include.types.contains(type)) {
				if (priorityA > priorityB) {
					mergedExcludedTypes.add(type);
				} else {
					mergedIncludedTypes.add(type);
				}
			} else {
				mergedExcludedTypes.add(type);
			}
		}

		for (ResourceLocation id : a.include.entities) {
			if (b.exclude.entities.contains(id)) {
				if (priorityA > priorityB) {
					mergedIncluded.add(id);
				} else {
					mergedExcluded.add(id);
				}
			} else {
				mergedIncluded.add(id);
			}
		}

		for (ResourceLocation id : a.exclude.entities) {
			if (b.include.entities.contains(id)) {
				if (priorityA > priorityB) {
					mergedExcluded.add(id);
				} else {
					mergedIncluded.add(id);
				}
			} else {
				mergedExcluded.add(id);
			}
		}

		return new SpawningRules(new FilterRules(mergedIncluded, mergedIncludedTypes), new FilterRules(mergedExcluded, mergedExcludedTypes));
	}

	public static SpawningRules decode(JsonElement element) {
		FilterRules include = FilterRules.empty();
		FilterRules exclude = FilterRules.empty();

		if (element != null) {
			JsonObject json = element.getAsJsonObject();
			if (json.has("include")) {
				include = FilterRules.decode(json.get("include"));
			}

			if (json.has("exclude")) {
				exclude = FilterRules.decode(json.get("exclude"));
			}
		}

		return new SpawningRules(include, exclude);
	}

}
