package fr.jdrcraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Fiche de personnage d'un joueur. Toute la logique est ici (aucune dépendance à Minecraft). */
public final class CharacterSheet {
	public static final int HARD_CAP = 20;
	public static final int POINTS_PER_LEVEL = 1;

	public enum CreationState { PENDING, POOL_ROLLED, DONE }

	public String name;
	public CharacterClass characterClass;
	public Race race;
	public Gender gender;
	public int morality;
	public int level = 1;
	public int xp;
	public int maxHp;
	public int pointsToSpend;
	public int overworldDeaths;
	public CreationState state = CreationState.PENDING;
	public final int[] baseScores = new int[Ability.values().length];
	public int[] rolledPool = new int[0];

	private CharacterSheet() {}

	public static CharacterSheet create(String name, CharacterClass cls, Race race, Gender gender, Camp startCamp) {
		CharacterSheet s = new CharacterSheet();
		s.name = name;
		s.characterClass = cls;
		s.race = race;
		s.gender = gender;
		s.morality = Standing.startingMorality(startCamp);
		Arrays.fill(s.baseScores, StatGeneration.MIN);
		return s;
	}

	public int score(Ability a) { return baseScores[a.ordinal()] + race.bonus(a); }

	public int modifier(Ability a) { return Dice.modifier(score(a)); }

	public Camp camp() { return Standing.camp(morality); }

	/** Méthode des 27 points. Les scores sont ceux d'avant bonus de race. */
	public boolean applyPointBuy(int[] scores) {
		if (state == CreationState.DONE || !StatGeneration.isValidPointBuy(scores)) return false;
		System.arraycopy(scores, 0, baseScores, 0, baseScores.length);
		finish();
		return true;
	}

	/** Méthode des dés : tire les 6 valeurs (le serveur est seul à lancer). */
	public int[] rollPool() {
		if (state == CreationState.DONE) return null;
		rolledPool = StatGeneration.rollPool();
		state = CreationState.POOL_ROLLED;
		return rolledPool.clone();
	}

	/** Répartit les valeurs tirées : elles doivent être exactement celles du tirage. */
	public boolean assignPool(int[] scores) {
		if (state != CreationState.POOL_ROLLED || !StatGeneration.isPermutation(rolledPool, scores)) return false;
		System.arraycopy(scores, 0, baseScores, 0, baseScores.length);
		finish();
		return true;
	}

	private void finish() {
		state = CreationState.DONE;
		maxHp = Math.max(1, characterClass.hitDie() + modifier(Ability.CONSTITUTION));
	}

	public boolean isCreated() { return state == CreationState.DONE; }

	/** Ajoute de l'XP et renvoie les niveaux gagnés (liste vide si aucun). */
	public List<LevelUp> addXp(int amount, boolean rollHp) {
		List<LevelUp> gained = new ArrayList<>();
		if (!isCreated() || amount <= 0) return gained;
		xp += amount;
		while (level < PlayerProgression.MAX_LEVEL && xp >= PlayerProgression.xpToNext(level)) {
			xp -= PlayerProgression.xpToNext(level);
			level++;
			int hp = PlayerProgression.hpGain(characterClass.hitDie(), score(Ability.CONSTITUTION), rollHp);
			maxHp += hp;
			pointsToSpend += POINTS_PER_LEVEL;
			gained.add(new LevelUp(level, hp, POINTS_PER_LEVEL));
		}
		if (level >= PlayerProgression.MAX_LEVEL) xp = 0;
		return gained;
	}

	/** Dépense 1 point de niveau dans une caractéristique (plafond : {@link #HARD_CAP}). */
	public boolean spendPoint(Ability a) {
		if (!isCreated() || pointsToSpend <= 0 || score(a) >= HARD_CAP) return false;
		int modBefore = modifier(a);
		baseScores[a.ordinal()]++;
		pointsToSpend--;
		if (a == Ability.CONSTITUTION) maxHp += (modifier(a) - modBefore) * level;
		return true;
	}

	// --- sérialisation (java.util.Properties : aucune bibliothèque externe) ---

	public Properties toProperties() {
		Properties p = new Properties();
		p.setProperty("name", name);
		p.setProperty("class", characterClass.id());
		p.setProperty("race", race.id());
		p.setProperty("gender", gender.id());
		p.setProperty("morality", Integer.toString(morality));
		p.setProperty("level", Integer.toString(level));
		p.setProperty("xp", Integer.toString(xp));
		p.setProperty("maxHp", Integer.toString(maxHp));
		p.setProperty("points", Integer.toString(pointsToSpend));
		p.setProperty("deaths", Integer.toString(overworldDeaths));
		p.setProperty("state", state.name());
		p.setProperty("scores", csv(baseScores));
		p.setProperty("pool", csv(rolledPool));
		return p;
	}

	public static CharacterSheet fromProperties(Properties p) {
		CharacterSheet s = new CharacterSheet();
		s.name = p.getProperty("name", "Sans nom");
		s.characterClass = CharacterClass.parse(p.getProperty("class", "")).orElse(CharacterClass.LAME_LIGE);
		s.race = Race.parse(p.getProperty("race", "")).orElse(Race.HUMAIN);
		s.gender = Gender.parse(p.getProperty("gender", "")).orElse(Gender.NEUTRAL);
		s.morality = intProp(p, "morality", 0);
		s.level = Math.max(1, intProp(p, "level", 1));
		s.xp = intProp(p, "xp", 0);
		s.maxHp = intProp(p, "maxHp", 1);
		s.pointsToSpend = intProp(p, "points", 0);
		s.overworldDeaths = intProp(p, "deaths", 0);
		try {
			s.state = CreationState.valueOf(p.getProperty("state", "PENDING"));
		} catch (IllegalArgumentException e) {
			s.state = CreationState.PENDING;
		}
		int[] scores = parseCsv(p.getProperty("scores", ""));
		for (int i = 0; i < s.baseScores.length; i++) s.baseScores[i] = i < scores.length ? scores[i] : StatGeneration.MIN;
		s.rolledPool = parseCsv(p.getProperty("pool", ""));
		return s;
	}

	private static String csv(int[] a) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); }
		return sb.toString();
	}

	private static int[] parseCsv(String s) {
		if (s == null || s.isBlank()) return new int[0];
		String[] parts = s.split(",");
		int[] out = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			try { out[i] = Integer.parseInt(parts[i].trim()); } catch (NumberFormatException e) { out[i] = StatGeneration.MIN; }
		}
		return out;
	}

	private static int intProp(Properties p, String key, int def) {
		try { return Integer.parseInt(p.getProperty(key, Integer.toString(def)).trim()); }
		catch (NumberFormatException e) { return def; }
	}
}
