package fr.jdrcraft.core;

import java.util.Collection;
import java.util.List;

public final class SkillRegistry {
	private SkillRegistry() {}

	/** Compétences accessibles : universelles, de la classe ou de la race, au bon niveau et avec les caractéristiques requises. */
	public static List<SkillDef> unlocked(Collection<SkillDef> all, String classId, String raceId,
										  int level, int[] scores) {
		return all.stream()
				.filter(s -> (s.classes().isEmpty() && s.races().isEmpty())
						|| s.classes().contains(classId) || s.races().contains(raceId))
				.filter(s -> level >= s.unlockLevel())
				.filter(s -> s.requires().entrySet().stream()
						.allMatch(e -> scores[e.getKey().ordinal()] >= e.getValue()))
				.toList();
	}
}
