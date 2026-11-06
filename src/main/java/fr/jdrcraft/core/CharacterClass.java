package fr.jdrcraft.core;

import java.util.List;
import java.util.Optional;

/** Classes originales (noms provisoires, à renommer à volonté). */
public enum CharacterClass {
	LAME_LIGE("lame_lige", "Lame-lige", 10),
	TISSEUR("tisseur", "Tisseur", 6),
	OMBRELIER("ombrelier", "Ombrelier", 8),
	VEILLEUR("veilleur", "Veilleur", 8),
	TRAQUEUR("traqueur", "Traqueur", 10);

	private final String id;
	private final String label;
	private final int hitDie;

	CharacterClass(String id, String label, int hitDie) {
		this.id = id; this.label = label; this.hitDie = hitDie;
	}

	public String id() { return id; }
	public String label() { return label; }
	public int hitDie() { return hitDie; }

	public static Optional<CharacterClass> parse(String s) {
		for (CharacterClass c : values()) if (c.id.equalsIgnoreCase(s)) return Optional.of(c);
		return Optional.empty();
	}

	public static List<String> ids() {
		return java.util.Arrays.stream(values()).map(CharacterClass::id).toList();
	}
}
