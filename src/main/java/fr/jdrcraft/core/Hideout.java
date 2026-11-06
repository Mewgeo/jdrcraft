package fr.jdrcraft.core;

import java.util.Random;

/** Choix des coordonnées d'une cachette. L'entité n'apparaît qu'au chargement des chunks (matérialisation paresseuse). */
public final class Hideout {
	private Hideout() {}

	public static int[] pick(int lastX, int lastZ, Random rng, int minDist, int maxDist) {
		double angle = rng.nextDouble() * Math.PI * 2;
		double dist = minDist + rng.nextDouble() * (maxDist - minDist);
		return new int[] {
				lastX + (int) Math.round(Math.cos(angle) * dist),
				lastZ + (int) Math.round(Math.sin(angle) * dist)
		};
	}
}
