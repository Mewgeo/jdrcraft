package fr.jdrcraft.core;

import java.util.List;
import java.util.Optional;

public enum Ability {
	FORCE("force", "Force"),
	DEXTERITE("dexterite", "Dextérité"),
	CONSTITUTION("constitution", "Constitution"),
	INTELLIGENCE("intelligence", "Intelligence"),
	SAGESSE("sagesse", "Sagesse"),
	CHARISME("charisme", "Charisme");

	private final String id;
	private final String label;

	Ability(String id, String label) { this.id = id; this.label = label; }

	public String id() { return id; }
	public String label() { return label; }

	public static Optional<Ability> parse(String s) {
		for (Ability a : values()) if (a.id.equalsIgnoreCase(s)) return Optional.of(a);
		return Optional.empty();
	}

	public static List<String> ids() {
		return java.util.Arrays.stream(values()).map(Ability::id).toList();
	}
}
