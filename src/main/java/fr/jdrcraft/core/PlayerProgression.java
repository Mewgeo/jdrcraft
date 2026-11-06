package fr.jdrcraft.core;

/** Courbe d'XP, bonus de maîtrise, gains de PV et XP tirée des mobs. */
public final class PlayerProgression {
	public static final int MAX_LEVEL = 20;

	private PlayerProgression() {}

	public static int xpToNext(int level) { return 100 + 75 * level; }

	public static int proficiency(int level) { return 2 + (level - 1) / 4; }

	/** Gain de PV à partir d'un jet de dé de vie (ou de la moyenne fixe) et du modificateur de Constitution. */
	public static int hpGainFromRoll(int roll, int conMod) { return Math.max(1, roll + conMod); }

	public static int hpGain(int hitDie, int conScore, boolean rollIt) {
		int base = rollIt ? Dice.roll(1, hitDie) : hitDie / 2 + 1;
		return hpGainFromRoll(base, Dice.modifier(conScore));
	}

	/** XP gagnée selon l'écart entre le niveau du mob et celui du joueur. */
	public static int xpFromKill(int mobXpReward, int playerLevel, int mobLevel) {
		int diff = mobLevel - playerLevel;
		double mult = diff >= 3 ? 1.5 : diff >= 0 ? 1.0 : diff >= -3 ? 0.5 : 0.1;
		return (int) Math.round(mobXpReward * mult);
	}
}
