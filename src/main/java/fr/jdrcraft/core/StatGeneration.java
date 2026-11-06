package fr.jdrcraft.core;

import java.util.Arrays;

/** Génération des caractéristiques : achat de 27 points ou jets de dés (4d6, on retire le plus petit). */
public final class StatGeneration {
	public static final int BUDGET = 27, MIN = 8, MAX = 15;
	private static final int[] COST = {0, 1, 2, 3, 4, 5, 7, 9}; // index = score - 8

	private StatGeneration() {}

	/** Coût d'un score en points, ou -1 s'il est hors de la plage 8 à 15. */
	public static int cost(int score) {
		if (score < MIN || score > MAX) return -1;
		return COST[score - MIN];
	}

	/** Coût total d'une répartition, ou -1 si un score est invalide. */
	public static int totalCost(int[] scores) {
		int total = 0;
		for (int s : scores) {
			int c = cost(s);
			if (c < 0) return -1;
			total += c;
		}
		return total;
	}

	public static boolean isValidPointBuy(int[] scores) {
		if (scores == null || scores.length != Ability.values().length) return false;
		int total = totalCost(scores);
		return total >= 0 && total <= BUDGET;
	}

	/** Somme de 4 dés en retirant le plus petit. */
	public static int dropLowestSum(int[] dice) {
		int sum = 0, min = Integer.MAX_VALUE;
		for (int d : dice) { sum += d; min = Math.min(min, d); }
		return sum - min;
	}

	public static int rollOneStat() {
		int[] d = new int[4];
		for (int i = 0; i < 4; i++) d[i] = Dice.roll(1, 6);
		return dropLowestSum(d);
	}

	/** Les 6 valeurs tirées (triées de la plus haute à la plus basse) que le joueur répartit lui-même. */
	public static int[] rollPool() {
		int[] pool = new int[6];
		for (int i = 0; i < 6; i++) pool[i] = rollOneStat();
		Arrays.sort(pool);
		for (int i = 0; i < 3; i++) { int t = pool[i]; pool[i] = pool[5 - i]; pool[5 - i] = t; }
		return pool;
	}

	/** Les valeurs envoyées sont-elles exactement celles tirées, réarrangées ? */
	public static boolean isPermutation(int[] pool, int[] scores) {
		if (pool == null || scores == null || pool.length != scores.length) return false;
		int[] a = pool.clone(), b = scores.clone();
		Arrays.sort(a); Arrays.sort(b);
		return Arrays.equals(a, b);
	}
}
