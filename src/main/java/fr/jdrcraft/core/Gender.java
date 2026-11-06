package fr.jdrcraft.core;

import java.util.List;
import java.util.Optional;

public enum Gender {
	FEMALE("femme"), MALE("homme"), NEUTRAL("neutre");

	private final String id;
	Gender(String id) { this.id = id; }
	public String id() { return id; }

	public static Optional<Gender> parse(String s) {
		for (Gender g : values()) if (g.id.equalsIgnoreCase(s)) return Optional.of(g);
		return Optional.empty();
	}

	public static List<String> ids() {
		return java.util.Arrays.stream(values()).map(Gender::id).toList();
	}
}
