package fr.jdrcraft.core;

/** Péremption de la nourriture et des potions (calculée à la demande, jamais à chaque tick). */
public final class Spoilage {
	public enum Freshness { FRESH, GOOD, STALE, ROTTEN }

	private Spoilage() {}

	public static Freshness freshness(long ageTicks, long shelfLifeTicks) {
		if (shelfLifeTicks <= 0) return Freshness.FRESH; // ne périme pas
		double r = (double) ageTicks / shelfLifeTicks;
		if (r < 0.25) return Freshness.FRESH;
		if (r < 0.60) return Freshness.GOOD;
		if (r < 1.00) return Freshness.STALE;
		return Freshness.ROTTEN;
	}

	public static double foodMultiplier(Freshness f) {
		return switch (f) { case FRESH -> 1.0; case GOOD -> 0.9; case STALE -> 0.5; case ROTTEN -> 0.0; };
	}

	public static double potionPotency(Freshness f) {
		return switch (f) { case FRESH, GOOD -> 1.0; case STALE -> 0.6; case ROTTEN -> 0.0; };
	}

	public static boolean resolveRottenSave(int roll, int conMod) { return roll + conMod >= 12; }

	/** Manger un aliment avarié : jet de sauvegarde de Constitution contre DD 12. */
	public static boolean resistsRottenFood(int conScore) {
		return resolveRottenSave(Dice.d20(), Dice.modifier(conScore));
	}

	/** Âge en ticks selon le jour de création et le rythme du lieu de stockage (1.0 normal, 0.5 cave, 0.25 froid). */
	public static long effectiveAge(long createdDay, long currentDay, double storageRate) {
		return Math.round((currentDay - createdDay) * 24000L * storageRate);
	}
}
