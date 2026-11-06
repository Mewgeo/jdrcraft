package fr.jdrcraft.core;

/** Transformation progressive du conjoint après la mort du joueur. */
public record WidowTransformation(long deathDay) {
	public enum State { MOURNING, TRANSFORMING, DEMON_GOD }

	public State state(long today) {
		long days = today - deathDay;
		if (days < 3) return State.MOURNING;
		if (days < 10) return State.TRANSFORMING;
		return State.DEMON_GOD;
	}
}
