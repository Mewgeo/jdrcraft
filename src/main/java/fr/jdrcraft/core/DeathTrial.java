package fr.jdrcraft.core;

/** Épreuve des Limbes : difficulté exponentielle plafonnée, puis mode sans fin (temps de survie croissant). */
public final class DeathTrial {
	private static final double GROWTH = 1.35, MAX_MULTIPLIER = 20.0;
	public static final int CAP_DEATHS = 10;
	private static final int BASE_SECONDS = 300, EXTRA_PER_DEATH = 30, MAX_SECONDS = 1200;

	public record Plan(boolean endless, double difficulty, int waves, int mobLevelBonus, int survivalSeconds) {}

	private DeathTrial() {}

	public static boolean isEndless(int deaths) { return deaths >= CAP_DEATHS; }

	/** La force des monstres est plafonnée : au seuil, elle ne grandit plus. */
	public static double difficulty(int deaths) {
		return Math.min(MAX_MULTIPLIER, Math.pow(GROWTH, Math.min(Math.max(0, deaths), CAP_DEATHS)));
	}

	public static int waveCount(int deaths) { return 3 + Math.min(Math.max(0, deaths), 10) / 2; }

	public static int mobLevelBonus(int deaths) { return Math.min(10, Math.max(0, deaths)); }

	/** Temps à survivre en mode sans fin (0 en mode vagues). */
	public static int survivalSeconds(int deaths) {
		if (!isEndless(deaths)) return 0;
		return Math.min(MAX_SECONDS, BASE_SECONDS + EXTRA_PER_DEATH * (deaths - CAP_DEATHS));
	}

	/** Indulgence : -10 % par défaite d'affilée, jusqu'à -30 %, jamais sous la base. */
	public static int withMercy(int seconds, int consecutiveDefeats) {
		double reduction = Math.min(0.30, 0.10 * Math.max(0, consecutiveDefeats));
		return Math.max(BASE_SECONDS, (int) Math.round(seconds * (1.0 - reduction)));
	}

	/** Intensité des apparitions (0 à 1) selon le temps écoulé : montée, plateau, répit de 15 s toutes les 2 minutes. */
	public static double intensity(int elapsedSeconds, int totalSeconds) {
		if ((elapsedSeconds % 120) >= 105) return 0.1;
		double ramp = 0.4 + 0.6 * elapsedSeconds / Math.max(1.0, totalSeconds / 3.0);
		return Math.min(1.0, ramp);
	}

	public static Plan plan(int deaths) {
		boolean endless = isEndless(deaths);
		return new Plan(endless, difficulty(deaths), endless ? 0 : waveCount(deaths),
				mobLevelBonus(deaths), survivalSeconds(deaths));
	}
}
