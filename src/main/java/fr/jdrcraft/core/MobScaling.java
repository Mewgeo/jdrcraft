package fr.jdrcraft.core;

/** Niveau et statistiques des mobs : zone, temps, niveau du groupe. */
public final class MobScaling {
	private MobScaling() {}

	public static int computeLevel(int partyLevel, double distanceFromSpawn, long worldDays) {
		int byDistance = (int) Math.min(distanceFromSpawn / 500.0, 5);
		int byTime = (int) Math.min(worldDays / 20.0, 5);
		int base = Math.max(partyLevel - 1, 1);
		return Math.max(1, Math.min(20, base + byDistance + byTime));
	}

	public static MobStats statsFor(int level, int vanillaHp) {
		int proficiency = 2 + (level - 1) / 4;
		return new MobStats(level, vanillaHp + level * 3, 10 + level / 3, proficiency,
				1 + level / 5, proficiency / 2, 10 + level * 5);
	}

	public static int levelUp(int level) { return Math.min(20, level + 1); }
}
