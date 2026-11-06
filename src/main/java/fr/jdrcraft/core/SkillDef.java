package fr.jdrcraft.core;

import java.util.Map;
import java.util.Set;

/** Compétence définie par données. Classes et races vides = compétence universelle. */
public record SkillDef(String id, Set<String> classes, Set<String> races, int unlockLevel,
					   Map<Ability, Integer> requires, int resourceCost, int cooldownTicks) {}
