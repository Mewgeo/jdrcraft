package fr.jdrcraft.core;

/** Moralité, accueil des villages et attitude des monstres. Le camp est dérivé de la moralité. */
public final class Standing {
	public enum VillageStanding { WELCOME, WARY, SUSPECT, BANNED, OUTLAW }
	public enum Stance { ALLY, NEUTRAL, HOSTILE }
	public enum ShiftEvent { NONE, FALL_TO_SHADOW, RISE_TO_LIGHT }

	private Standing() {}

	/** Penchant de départ : reste dans la zone où les villageois acceptent de commercer. */
	public static int startingMorality(Camp chosen) {
		return switch (chosen) { case HERO -> 15; case NEUTRAL -> 0; case VILLAIN -> -15; };
	}

	public static int clamp(int morality) { return Math.max(-100, Math.min(100, morality)); }

	public static int applyCrime(int morality, Crime crime) { return clamp(morality + crime.moralityDelta); }

	/** Accueil d'un village : moralité globale + réputation locale (-50 à +50). */
	public static VillageStanding village(int morality, int localReputation) {
		int e = morality + localReputation;
		if (e >= -19) return VillageStanding.WELCOME;
		if (e >= -39) return VillageStanding.WARY;
		if (e >= -59) return VillageStanding.SUSPECT;
		if (e >= -79) return VillageStanding.BANNED;
		return VillageStanding.OUTLAW;
	}

	/** Multiplicateur de prix d'un village, ou -1 si le marchand refuse de commercer. */
	public static double villagePrice(VillageStanding s, int morality) {
		return switch (s) {
			case WELCOME -> morality >= 40 ? 0.9 : 1.0;
			case WARY -> 1.15;
			case SUSPECT -> 1.4;
			case BANNED, OUTLAW -> -1;
		};
	}

	public static Stance monsters(int morality) {
		if (morality <= -60) return Stance.ALLY;
		if (morality <= -30) return Stance.NEUTRAL;
		return Stance.HOSTILE;
	}

	public static Camp camp(int morality) {
		if (morality >= 30) return Camp.HERO;
		if (morality <= -30) return Camp.VILLAIN;
		return Camp.NEUTRAL;
	}

	/** Événement de basculement quand la moralité franchit -50 ou +50. */
	public static ShiftEvent shiftEvent(int before, int after) {
		if (before > -50 && after <= -50) return ShiftEvent.FALL_TO_SHADOW;
		if (before < 50 && after >= 50) return ShiftEvent.RISE_TO_LIGHT;
		return ShiftEvent.NONE;
	}

	public static boolean resolveCrimeSeen(int roll, int dexMod, int witnessPerceptionDc) {
		return roll + dexMod < witnessPerceptionDc;
	}

	/** Un crime est-il vu ? Jet de Discrétion (Dextérité) contre la perception des témoins. */
	public static boolean crimeSeen(int dexScore, int witnessPerceptionDc) {
		return resolveCrimeSeen(Dice.d20(), Dice.modifier(dexScore), witnessPerceptionDc);
	}
}
