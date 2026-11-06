package fr.jdrcraft.core;

/** Lancer d'engagement optionnel avant un combat : Dextérité du joueur contre celle de l'ennemi. */
public final class CombatEngagement {
	public enum Engagement { ADVANTAGE, NEUTRAL, DISADVANTAGE }

	private CombatEngagement() {}

	public static Engagement resolve(int playerTotal, int enemyTotal) {
		int diff = playerTotal - enemyTotal;
		if (diff >= 5) return Engagement.ADVANTAGE;
		if (diff <= -5) return Engagement.DISADVANTAGE;
		return Engagement.NEUTRAL;
	}

	public static Engagement roll(int playerDex, int enemyDex) {
		int p = Dice.d20() + Dice.modifier(playerDex);
		int e = Dice.d20() + Dice.modifier(enemyDex);
		return resolve(p, e);
	}
}
