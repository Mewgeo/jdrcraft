package fr.jdrcraft.core;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

/** Mob rescapé : mémorise les tactiques du joueur, s'adapte, puis revient ou se cache. */
public final class Survivor {
	public enum PlayerTactic { MELEE, RANGED, MAGIC, FIRE, TRAPS, POTIONS, SHIELD, FLEE }

	public enum Adaptation {
		COVER_AND_CHARGE, KEEP_DISTANCE, FIRE_RESISTANT, MAGIC_WARD,
		TRAP_SENSE, HEALER_HUNTER, GUARD_BREAKER, RELENTLESS_PURSUIT
	}

	public enum Fate { RETURNS_HUNTING, HIDES_AS_BOSS, JOINS_PACK, VANISHES }

	public UUID id = UUID.randomUUID();
	public UUID nemesisOf;                       // joueur auquel il est lié
	public String mobType = "", epithet = "";
	public int level = 1, survivals;
	public boolean boss, overpowered;
	public long returnDay;
	public int hideoutX, hideoutZ;
	public Fate fate;
	public final EnumMap<PlayerTactic, Integer> observed = new EnumMap<>(PlayerTactic.class);
	public final EnumSet<Adaptation> adaptations = EnumSet.noneOf(Adaptation.class);

	public void observe(PlayerTactic t) { observed.merge(t, 1, Integer::sum); }

	/** Après une survie : adopte les contre-mesures des tactiques les plus utilisées (limite fixée). */
	public void adapt(int maxAdaptations) {
		observed.entrySet().stream()
				.sorted(Map.Entry.<PlayerTactic, Integer>comparingByValue(Comparator.reverseOrder()))
				.map(e -> counterTo(e.getKey()))
				.forEach(a -> { if (adaptations.size() < maxAdaptations) adaptations.add(a); });
	}

	public static Adaptation counterTo(PlayerTactic t) {
		return switch (t) {
			case RANGED -> Adaptation.COVER_AND_CHARGE;
			case MELEE -> Adaptation.KEEP_DISTANCE;
			case FIRE -> Adaptation.FIRE_RESISTANT;
			case MAGIC -> Adaptation.MAGIC_WARD;
			case TRAPS -> Adaptation.TRAP_SENSE;
			case POTIONS -> Adaptation.HEALER_HUNTER;
			case SHIELD -> Adaptation.GUARD_BREAKER;
			case FLEE -> Adaptation.RELENTLESS_PURSUIT;
		};
	}

	public static Fate fateFromRoll(int roll, int survivals, int playerLevel, int mobLevel) {
		int total = roll + survivals + Math.max(0, playerLevel - mobLevel) / 2;
		if (total >= 22) return Fate.HIDES_AS_BOSS;
		if (total >= 14) return Fate.RETURNS_HUNTING;
		if (total >= 8) return Fate.JOINS_PACK;
		return Fate.VANISHES;
	}

	public static Fate decideFate(int survivals, int playerLevel, int mobLevel) {
		return fateFromRoll(Dice.d20(), survivals, playerLevel, mobLevel);
	}

	public boolean rollOverpowered() { return Dice.d20() + survivals >= 18; }
}
