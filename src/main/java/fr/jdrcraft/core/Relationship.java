package fr.jdrcraft.core;

/** Relation entre un joueur et un villageois adulte (affinité, romance, fiançailles, mariage). */
public final class Relationship {
	public enum Stage { STRANGER, ACQUAINTANCE, FRIEND, CLOSE, SWEETHEART, ENGAGED, MARRIED }
	public enum GiftTaste { LOVED, LIKED, NEUTRAL, DISLIKED, HATED }

	public int affinity;                       // -100 à +100
	public long lastGiftDay = -1, lastTalkDay = -1;
	public boolean romance, engaged, married;

	public Stage stage() {
		if (married) return Stage.MARRIED;
		if (engaged) return Stage.ENGAGED;
		if (romance) return Stage.SWEETHEART;
		if (affinity >= 70) return Stage.CLOSE;
		if (affinity >= 40) return Stage.FRIEND;
		if (affinity >= 10) return Stage.ACQUAINTANCE;
		return Stage.STRANGER;
	}

	public static int giftDelta(GiftTaste taste, Spoilage.Freshness freshness) {
		if (freshness == Spoilage.Freshness.ROTTEN) return -8;
		int base = switch (taste) { case LOVED -> 8; case LIKED -> 4; case NEUTRAL -> 1; case DISLIKED -> -4; case HATED -> -10; };
		return freshness == Spoilage.Freshness.STALE ? base / 2 : base;
	}

	/** Un cadeau par jour. Renvoie la variation appliquée, ou 0 si le villageois a déjà reçu un cadeau aujourd'hui. */
	public int giveGift(GiftTaste taste, Spoilage.Freshness freshness, long today) {
		if (lastGiftDay == today) return 0;
		lastGiftDay = today;
		int delta = giftDelta(taste, freshness);
		affinity = Math.max(-100, Math.min(100, affinity + delta));
		return delta;
	}

	public static boolean resolveConfession(int roll, int charismaMod, int affinity) {
		int dc = 18 - affinity / 8;
		return roll == 20 || (roll != 1 && roll + charismaMod >= dc);
	}

	/** Déclaration : affinité d'au moins 60 requise. Un 1 naturel coûte 15 points d'affinité. */
	public boolean confess(int charismaScore) {
		if (affinity < 60) return false;
		int roll = Dice.d20();
		boolean ok = resolveConfession(roll, Dice.modifier(charismaScore), affinity);
		if (ok) romance = true; else if (roll == 1) affinity -= 15;
		return ok;
	}
}
