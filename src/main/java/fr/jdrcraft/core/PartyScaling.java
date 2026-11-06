package fr.jdrcraft.core;

import java.util.List;

/** Adaptation des mobs au groupe de joueurs proches (multijoueur). */
public final class PartyScaling {
	private PartyScaling() {}

	/** Niveau effectif : moitié moyenne, moitié maximum des joueurs proches. */
	public static int effectivePartyLevel(List<Integer> nearbyLevels) {
		if (nearbyLevels.isEmpty()) return 1;
		double avg = nearbyLevels.stream().mapToInt(i -> i).average().orElse(1);
		int max = nearbyLevels.stream().mapToInt(i -> i).max().orElse(1);
		return (int) Math.round(0.5 * avg + 0.5 * max);
	}

	public static double healthMultiplier(int nearbyPlayers) {
		return 1.0 + 0.5 * (Math.max(1, nearbyPlayers) - 1);
	}
}
