package fr.jdrcraft.core;

/** Interroger un villageois sur une rumeur : jet de Charisme + bonus d'affinité contre DD 12. */
public final class RumorLearning {
	private RumorLearning() {}

	/** 0 = réponse évasive, 1 = rumeur, 2 = rumeur + faiblesse du rescapé (20 naturel). */
	public static int resolve(int roll, int charismaMod, int affinity) {
		if (roll == 20) return 2;
		int total = roll + charismaMod + affinity / 20;
		return (roll != 1 && total >= 12) ? 1 : 0;
	}

	public static int ask(int charismaScore, int affinity) {
		return resolve(Dice.d20(), Dice.modifier(charismaScore), affinity);
	}
}
