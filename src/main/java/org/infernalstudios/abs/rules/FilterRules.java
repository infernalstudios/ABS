package org.infernalstudios.abs.rules;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;

public class FilterRules {

	Set<ResourceLocation> entities;
	Set<MobSpawnType> types;

	public FilterRules(Set<ResourceLocation> entities, Set<MobSpawnType> types) {
		this.entities = entities;
		this.types = types;
	}

	public Set<ResourceLocation> getEntities() {
		return entities;
	}

	public Set<MobSpawnType> getTypes() {
		return types;
	}

	@Override
	public String toString() {
		return "FilterRules(Entities: " + entities + ", Spawn Types: " + types + ")";
	}

	public static FilterRules decode(JsonElement element) {
		Set<ResourceLocation> entities = new HashSet<ResourceLocation>();
		Set<MobSpawnType> types = new HashSet<MobSpawnType>();

		if (element.isJsonObject()) {
			JsonObject json = element.getAsJsonObject();

			if (json.has("entities")) {
				for (JsonElement id : json.get("entities").getAsJsonArray()) {
					entities.add(new ResourceLocation(id.getAsString()));
				}
			}

			if (json.has("types")) {
				for (JsonElement id : json.get("types").getAsJsonArray()) {
					types.add(MobSpawnType.valueOf(id.getAsString()));
				}
			} else {
				types.add(MobSpawnType.CHUNK_GENERATION);
				types.add(MobSpawnType.JOCKEY);
				types.add(MobSpawnType.NATURAL);
				types.add(MobSpawnType.REINFORCEMENT);
				types.add(MobSpawnType.PATROL);
			}
		} else {
			for (JsonElement id : element.getAsJsonArray()) {
				entities.add(new ResourceLocation(id.getAsString()));
			}

			types.add(MobSpawnType.CHUNK_GENERATION);
			types.add(MobSpawnType.JOCKEY);
			types.add(MobSpawnType.NATURAL);
			types.add(MobSpawnType.REINFORCEMENT);
			types.add(MobSpawnType.PATROL);
		}

		return new FilterRules(ImmutableSet.copyOf(entities), ImmutableSet.copyOf(types));
	}

	public static FilterRules empty() {
		return new FilterRules(ImmutableSet.of(), ImmutableSet.of(MobSpawnType.CHUNK_GENERATION, MobSpawnType.JOCKEY, MobSpawnType.NATURAL, MobSpawnType.REINFORCEMENT, MobSpawnType.PATROL));
	}

}
