package fr.jdrcraft.core;

import java.util.List;
import java.util.Optional;

/** Races provisoires (bonus de caractéristiques dans l'ordre de {@link Ability}). */
public enum Race {
	HUMAIN("humain", "Humain", new int[] {1, 1, 1, 1, 1, 1}),
	SYLVAIN("sylvain", "Sylvain", new int[] {0, 2, 0, 0, 1, 0}),
	PIERREUX("pierreux", "Pierreux", new int[] {1, 0, 2, 0, 0, 0});

	private final String id;
	private final String label;
	private final int[] bonuses;

	Race(String id, String label, int[] bonuses) {
		this.id = id; this.label = label; this.bonuses = bonuses;
	}

	public String id() { return id; }
	public String label() { return label; }
	public int bonus(Ability a) { return bonuses[a.ordinal()]; }

	public static Optional<Race> parse(String s) {
		for (Race r : values()) if (r.id.equalsIgnoreCase(s)) return Optional.of(r);
		return Optional.empty();
	}

	public static List<String> ids() {
		return java.util.Arrays.stream(values()).map(Race::id).toList();
	}
}
