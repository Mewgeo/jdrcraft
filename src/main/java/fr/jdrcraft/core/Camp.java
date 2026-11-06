package fr.jdrcraft.core;

import java.util.List;
import java.util.Optional;

/** Camp du joueur. Il est dérivé de la moralité ; le choix de départ n'est qu'un penchant. */
public enum Camp {
	HERO("heros"), NEUTRAL("neutre"), VILLAIN("malefique");

	private final String id;
	Camp(String id) { this.id = id; }
	public String id() { return id; }

	public static Optional<Camp> parse(String s) {
		for (Camp c : values()) if (c.id.equalsIgnoreCase(s)) return Optional.of(c);
		return Optional.empty();
	}

	public static List<String> ids() {
		return java.util.Arrays.stream(values()).map(Camp::id).toList();
	}
}
