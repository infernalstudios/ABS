package org.infernalstudios.abs;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.infernalstudios.abs.rules.AdvancementRule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class AbsLoader extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public AbsLoader() {
		super(GSON, "abs");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
		Abs.abs.clear();

		Map<ResourceLocation, AdvancementRule> abs = elements.entrySet().stream().map((entry) -> entry.getValue()).flatMap((element) -> element.getAsJsonObject().entrySet().stream())
				.sorted(Comparator.comparingInt((entry) -> entry.getValue().getAsJsonObject().has("priority") ? entry.getValue().getAsJsonObject().get("priority").getAsInt() : 1000))
				.map((entry) -> Map.entry(entry.getKey(), AdvancementRule.decode(entry.getValue().getAsJsonObject())))
				.sorted((a, b) -> Integer.compare(a.getValue().getPriority(), b.getValue().getPriority()))
				.collect(Collectors.toMap((entry) -> new ResourceLocation(entry.getKey()), (entry) -> entry.getValue(), AdvancementRule::handleMerge, LinkedHashMap::new));

		Abs.abs.putAll(abs);
	}
}
