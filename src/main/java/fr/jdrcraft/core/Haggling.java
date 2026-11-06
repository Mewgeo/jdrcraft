package fr.jdrcraft.core;

/** Marchandage avec un villageois : d20 + Charisme contre une difficulté. */
public final class Haggling {
	public enum Result { FREE, BIG_DISCOUNT, DISCOUNT, MARKUP, ANGRY }

	public record Outcome(Result result, int roll, int total, int dc, double priceMultiplier) {}

	private Haggling() {}

	public static Outcome resolve(int roll, int charismaMod, int dc) {
		int total = roll + charismaMod;
		if (roll == 1) return new Outcome(Result.ANGRY, roll, total, dc, 1.5);
		if (roll == 20 || total >= dc + 10) return new Outcome(Result.FREE, roll, total, dc, 0.0);
		if (total >= dc + 5) return new Outcome(Result.BIG_DISCOUNT, roll, total, dc, 0.5);
		if (total >= dc) return new Outcome(Result.DISCOUNT, roll, total, dc, 0.75);
		return new Outcome(Result.MARKUP, roll, total, dc, 1.25);
	}

	public static Outcome attempt(int charismaScore, int dc) {
		return resolve(Dice.d20(), Dice.modifier(charismaScore), dc);
	}
}
