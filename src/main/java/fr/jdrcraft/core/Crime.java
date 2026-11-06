package fr.jdrcraft.core;

/** Actes qui font bouger la moralité. */
public enum Crime {
	PROTECT_VILLAGER(+3), THEFT(-3), ASSAULT(-8), ARSON_HOUSE(-10), KILL_VILLAGER(-15), KILL_GUARD(-20);

	public final int moralityDelta;
	Crime(int delta) { this.moralityDelta = delta; }
}
