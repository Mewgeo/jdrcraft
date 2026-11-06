package fr.jdrcraft.core;

import java.util.UUID;

/** Conjoint d'un joueur : harmonie, négligence, accord pour le polyamour. */
public final class Spouse {
	public enum BondStage { HARMONY, MELANCHOLY, RESENTMENT, OBSESSION }

	public UUID villagerId;
	public Gender gender = Gender.NEUTRAL;
	public int harmony = 80;                 // 0 à 100
	public long lastSeenDay;

	/** Chaque jour : l'usure grandit avec l'absence et le nombre de conjoints. */
	public void dailyTick(long today, boolean playerAtHome, int spouseCount) {
		int absence = (int) Math.max(0, today - lastSeenDay);
		int decay = playerAtHome ? 0 : 2 + absence / 3 + (Math.max(1, spouseCount) - 1);
		harmony = Math.max(0, harmony - decay);
		if (playerAtHome) lastSeenDay = today;
	}

	public BondStage stage() {
		if (harmony >= 60) return BondStage.HARMONY;
		if (harmony >= 30) return BondStage.MELANCHOLY;
		if (harmony >= 10) return BondStage.RESENTMENT;
		return BondStage.OBSESSION;
	}

	public static boolean resolveAcceptance(int roll, int charismaMod, int harmony, int affinity, boolean openMinded) {
		int dc = openMinded ? 10 : 16;
		int bonus = charismaMod + harmony / 20 + affinity / 25;
		return roll == 20 || (roll != 1 && roll + bonus >= dc);
	}

	/** Un conjoint accepte-t-il un nouveau partenaire ? */
	public boolean acceptsNewPartner(int affinity, boolean openMinded, int charismaScore) {
		return resolveAcceptance(Dice.d20(), Dice.modifier(charismaScore), harmony, affinity, openMinded);
	}

	public String widowName(String playerName) {
		return switch (gender) {
			case FEMALE -> "Veuve de " + playerName;
			case MALE -> "Veuf de " + playerName;
			case NEUTRAL -> "Veuf·ve de " + playerName;
		};
	}
}
