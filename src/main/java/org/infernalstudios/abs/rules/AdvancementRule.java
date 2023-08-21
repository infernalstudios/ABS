package org.infernalstudios.abs.rules;

import com.google.gson.JsonObject;

public class AdvancementRule {

	int priority;
	SpawningRules with;
	SpawningRules without;
	boolean claimPriority;

	public AdvancementRule(int priority, SpawningRules with, SpawningRules without, boolean claimPriority) {
		this.priority = priority;
		this.with = with;
		this.without = without;
		this.claimPriority = claimPriority;
	}

	public int getPriority() {
		return priority;
	}

	public SpawningRules getWith() {
		return with;
	}

	public SpawningRules getWithout() {
		return without;
	}

	public boolean claimsPriority() {
		return claimPriority;
	}

	@Override
	public String toString() {
		return "AdvancementRule(" + (this.claimsPriority() ? "Claims " : "") + "Priority: " + this.priority + ", With: " + this.with + ", Without: " + this.without + ")";
	}

	public static AdvancementRule decode(JsonObject json) {
		int priority = 1000;
		boolean claimsPriority = false;

		if (json.has("priority")) {
			priority = json.get("priority").getAsInt();
		}

		if (json.has("claim_priority")) {
			claimsPriority = json.get("claim_priority").getAsBoolean();
		}

		return new AdvancementRule(priority, SpawningRules.decode(json.get("with")), SpawningRules.decode(json.get("without")), claimsPriority);
	}

	public static AdvancementRule handleMerge(AdvancementRule a, AdvancementRule b) {
		SpawningRules spawningRulesWith = SpawningRules.handleMerge(a, b, true);
		SpawningRules spawningRulesWithout = SpawningRules.handleMerge(a, b, false);

		int priority = Integer.max(a.priority, b.priority);

		if (a.claimsPriority() && !b.claimsPriority()) {
			priority = a.priority;
		} else if (!a.claimsPriority() && b.claimsPriority()) {
			priority = b.priority;
		}

		return new AdvancementRule(priority, spawningRulesWith, spawningRulesWithout, a.claimsPriority() || b.claimsPriority());
	}

}
