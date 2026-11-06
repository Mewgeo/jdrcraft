package fr.jdrcraft.core;

import java.util.function.IntSupplier;

/** Plaidoyer pour convaincre un conjoint devenu dieu : 3 réussites avant 2 échecs. */
public final class Persuasion {
	private Persuasion() {}

	public static int difficulty(int harmonyAtDeath, int overworldDeaths, boolean hasRelic, int allySupport) {
		return 12 + overworldDeaths / 2 - harmonyAtDeath / 20 - (hasRelic ? 2 : 0) - allySupport;
	}

	public static boolean resolve(IntSupplier d20, int charismaMod, int dc) {
		int successes = 0, failures = 0;
		while (successes < 3 && failures < 2) {
			int roll = d20.getAsInt();
			boolean ok = roll == 20 || (roll != 1 && roll + charismaMod >= dc);
			if (ok) successes++; else failures++;
		}
		return successes >= 3;
	}

	public static boolean persuade(int charismaScore, int harmonyAtDeath, int overworldDeaths,
								   boolean hasRelic, int allySupport) {
		int dc = difficulty(harmonyAtDeath, overworldDeaths, hasRelic, allySupport);
		return resolve(Dice::d20, Dice.modifier(charismaScore), dc);
	}
}
