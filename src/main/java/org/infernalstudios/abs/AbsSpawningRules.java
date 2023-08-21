package org.infernalstudios.abs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;

public class AbsSpawningRules {

	int priority;
	AbsMobRules includeRules;
	AbsMobRules excludeRules;
	boolean claimPriority;

	public AbsSpawningRules(int priority, AbsMobRules includeRules, AbsMobRules excludeRules, boolean claimPriority) {
		this.priority = priority;
		this.includeRules = includeRules;
		this.excludeRules = excludeRules;
		this.claimPriority = claimPriority;
	}

	public int getPriority() {
		return priority;
	}

	public AbsMobRules getIncludeRules() {
		return includeRules;
	}

	public AbsMobRules getExcludeRules() {
		return excludeRules;
	}

	public boolean claimsPriority() {
		return claimPriority;
	}

	@Override
	public String toString() {
		return "AbsSpawningRules(" + (this.claimsPriority() ? "Claims " : "") + "Priority: " + this.priority + ", Include: " + this.includeRules + ", Exclude: " + this.excludeRules + ")";
	}

	public static AbsSpawningRules decode(JsonObject json) {
		int priority = 1000;
		boolean claimsPriority = false;

		if (json.has("priority")) {
			priority = json.get("priority").getAsInt();
		}

		if (json.has("claim_priority")) {
			claimsPriority = json.get("claim_priority").getAsBoolean();
		}

		return new AbsSpawningRules(priority, AbsMobRules.decode(json, "include"), AbsMobRules.decode(json, "exclude"), claimsPriority);
	}

	public static AbsSpawningRules handleMerge(AbsSpawningRules a, AbsSpawningRules b) {
		Set<ResourceLocation> mergedIncludedWith = new HashSet<ResourceLocation>();
		Set<ResourceLocation> mergedExcludedWith = new HashSet<ResourceLocation>();
		Set<ResourceLocation> mergedIncludedWithout = new HashSet<ResourceLocation>();
		Set<ResourceLocation> mergedExcludedWithout = new HashSet<ResourceLocation>();
		Set<MobSpawnType> mergedIncludedTypes = new HashSet<MobSpawnType>();
		Set<MobSpawnType> mergedExcludedTypes = new HashSet<MobSpawnType>();

		for (MobSpawnType type : a.includeRules.types) {
			if (b.excludeRules.types.contains(type)) {
				if (a.priority > b.priority) {
					mergedIncludedTypes.add(type);
				} else {
					mergedExcludedTypes.add(type);
				}
			} else {
				mergedIncludedTypes.add(type);
			}
		}

		for (MobSpawnType type : a.excludeRules.types) {
			if (b.includeRules.types.contains(type)) {
				if (a.priority > b.priority) {
					mergedExcludedTypes.add(type);
				} else {
					mergedIncludedTypes.add(type);
				}
			} else {
				mergedExcludedTypes.add(type);
			}
		}

		for (ResourceLocation id : a.includeRules.with) {
			if (b.excludeRules.with.contains(id)) {
				if (a.priority > b.priority) {
					mergedIncludedWith.add(id);
				} else {
					mergedExcludedWith.add(id);
				}
			} else {
				mergedIncludedWith.add(id);
			}
		}

		for (ResourceLocation id : a.excludeRules.with) {
			if (b.includeRules.with.contains(id)) {
				if (a.priority > b.priority) {
					mergedExcludedWith.add(id);
				} else {
					mergedIncludedWith.add(id);
				}
			} else {
				mergedExcludedWith.add(id);
			}
		}

		for (ResourceLocation id : a.includeRules.without) {
			if (b.excludeRules.without.contains(id)) {
				if (a.priority > b.priority) {
					mergedIncludedWithout.add(id);
				} else {
					mergedExcludedWithout.add(id);
				}
			} else {
				mergedIncludedWithout.add(id);
			}
		}

		for (ResourceLocation id : a.excludeRules.without) {
			if (b.includeRules.without.contains(id)) {
				if (a.priority > b.priority) {
					mergedExcludedWithout.add(id);
				} else {
					mergedIncludedWithout.add(id);
				}
			} else {
				mergedExcludedWithout.add(id);
			}
		}

		int priority = Integer.min(a.priority, b.priority);

		if (a.claimsPriority() && !b.claimsPriority()) {
			priority = a.priority;
		} else if (!a.claimsPriority() && b.claimsPriority()) {
			priority = b.priority;
		}

		mergedIncludedTypes.addAll(a.includeRules.types);
		mergedIncludedTypes.addAll(b.includeRules.types);

		mergedExcludedTypes.addAll(a.excludeRules.types);
		mergedExcludedTypes.addAll(b.excludeRules.types);

		return new AbsSpawningRules(priority, new AbsMobRules(mergedIncludedWith, mergedIncludedWithout, mergedIncludedTypes),
				new AbsMobRules(mergedExcludedWith, mergedExcludedWithout, mergedExcludedTypes), a.claimsPriority() || b.claimsPriority());
	}

	public static class AbsMobRules {
		Set<ResourceLocation> with;
		Set<ResourceLocation> without;
		Set<MobSpawnType> types;

		public AbsMobRules(Set<ResourceLocation> with, Set<ResourceLocation> without, Set<MobSpawnType> types) {
			this.with = with;
			this.without = without;
			this.types = types;
		}

		public Set<ResourceLocation> getWith() {
			return with;
		}

		public Set<ResourceLocation> getWithout() {
			return without;
		}

		public Set<MobSpawnType> getTypes() {
			return types;
		}

		@Override
		public String toString() {
			return "AbsMobRules(With: " + with + ", Without: " + without + ", Types: " + types + ")";
		}

		public static AbsMobRules decode(JsonObject json, String key) {
			Set<ResourceLocation> with = new HashSet<ResourceLocation>();
			Set<ResourceLocation> without = new HashSet<ResourceLocation>();
			Set<MobSpawnType> types = new HashSet<MobSpawnType>();

			if (json.has("with")) {
				JsonObject withList = json.get("with").getAsJsonObject();
				if (withList.has(key)) {
					for (JsonElement id : withList.get(key).getAsJsonArray()) {
						with.add(new ResourceLocation(id.getAsString()));
					}
				}
			}

			if (json.has("without")) {
				JsonObject withoutList = json.get("without").getAsJsonObject();
				if (withoutList.has(key)) {
					for (JsonElement id : withoutList.get(key).getAsJsonArray()) {
						without.add(new ResourceLocation(id.getAsString()));
					}
				}
			}

			if (json.has("types")) {
				JsonObject typesList = json.get("types").getAsJsonObject();
				if (typesList.has(key)) {
					for (JsonElement id : typesList.get(key).getAsJsonArray()) {
						types.add(MobSpawnType.valueOf(id.getAsString()));
					}
				}
			} else {
				types.add(MobSpawnType.CHUNK_GENERATION);
				types.add(MobSpawnType.JOCKEY);
				types.add(MobSpawnType.NATURAL);
				types.add(MobSpawnType.REINFORCEMENT);
				types.add(MobSpawnType.PATROL);
			}

			return new AbsMobRules(Collections.unmodifiableSet(with), Collections.unmodifiableSet(without), Collections.unmodifiableSet(types));
		}
	}

}
