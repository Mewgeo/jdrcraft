package fr.jdrcraft.core;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Attribue à chaque joueur sa propre arène des Limbes (même dimension, arènes très éloignées). */
public final class ArenaAllocator {
	private static final int SPACING = 4000, COLUMNS = 100;
	private final Map<UUID, Integer> slots = new HashMap<>();
	private final BitSet used = new BitSet();

	/** Coordonnées {x, z} de l'arène du joueur (la même tant qu'il n'est pas libéré). */
	public synchronized int[] allocate(UUID player) {
		int slot = slots.computeIfAbsent(player, p -> {
			int s = used.nextClearBit(0);
			used.set(s);
			return s;
		});
		return new int[] {(slot % COLUMNS) * SPACING, (slot / COLUMNS) * SPACING};
	}

	public synchronized void release(UUID player) {
		Integer s = slots.remove(player);
		if (s != null) used.clear(s);
	}
}
