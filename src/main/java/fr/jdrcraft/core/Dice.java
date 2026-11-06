package fr.jdrcraft.core;

import java.util.Random;

/** Moteur de dés : unique point de hasard du mod (remplaçable pour les tests). */
public final class Dice {
	private static volatile Random rng = new Random();

	private Dice() {}

	public static void setRandom(Random random) { rng = random; }

	public static int roll(int count, int sides) {
		int total = 0;
		for (int i = 0; i < count; i++) total += rng.nextInt(sides) + 1;
		return total;
	}

	public static int d20() { return roll(1, 20); }
	public static int d20Advantage() { return Math.max(d20(), d20()); }
	public static int d20Disadvantage() { return Math.min(d20(), d20()); }

	/** Modificateur d'une caractéristique : (score - 10) / 2, arrondi vers le bas. */
	public static int modifier(int score) { return Math.floorDiv(score - 10, 2); }
}
