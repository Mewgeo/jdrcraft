package fr.jdrcraft.core;

import java.util.UUID;

/** Rumeur sur un rescapé : se dégrade en circulant de villageois en villageois. */
public record Rumor(UUID survivorId, int x, int z, int errorRadius, long createdDay, int hops) {
	public enum Precision { VAGUE, REGIONAL, PRECISE }

	public Precision precision() {
		if (errorRadius <= 32) return Precision.PRECISE;
		if (errorRadius <= 200) return Precision.REGIONAL;
		return Precision.VAGUE;
	}

	/** Un villageois répète la rumeur à un autre : elle devient moins précise. */
	public Rumor spread() {
		return new Rumor(survivorId, x, z, (int) (errorRadius * 1.5) + 10, createdDay, hops + 1);
	}

	/** Direction cardinale depuis le joueur (+x = est, +z = sud). */
	public String direction(int playerX, int playerZ) {
		double angle = Math.toDegrees(Math.atan2(z - playerZ, x - playerX));
		String[] names = {"l'est", "le sud-est", "le sud", "le sud-ouest",
				"l'ouest", "le nord-ouest", "le nord", "le nord-est"};
		return names[(int) Math.round(((angle + 360) % 360) / 45) % 8];
	}
}
