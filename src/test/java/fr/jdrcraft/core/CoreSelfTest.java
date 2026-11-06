package fr.jdrcraft.core;

import java.util.*;

/** Tests autonomes du cœur (sans JUnit) : java -cp <classes> fr.jdrcraft.core.CoreSelfTest */
public final class CoreSelfTest {
	private static int failures = 0, checks = 0;

	private static void check(boolean cond, String msg) {
		checks++;
		if (!cond) { failures++; System.out.println("ÉCHEC : " + msg); }
	}

	public static void main(String[] args) {
		Dice.setRandom(new Random(42));

		// --- 27 points
		check(StatGeneration.cost(8) == 0 && StatGeneration.cost(13) == 5 && StatGeneration.cost(14) == 7 && StatGeneration.cost(15) == 9, "table des coûts");
		check(StatGeneration.cost(7) == -1 && StatGeneration.cost(16) == -1, "scores hors plage");
		check(StatGeneration.isValidPointBuy(new int[] {15, 15, 15, 8, 8, 8}), "15/15/15/8/8/8 = 27 points valide");
		check(StatGeneration.isValidPointBuy(new int[] {15, 14, 13, 12, 10, 8}), "tableau standard valide");
		check(!StatGeneration.isValidPointBuy(new int[] {15, 15, 15, 15, 8, 8}), "36 points refusé");
		check(!StatGeneration.isValidPointBuy(new int[] {8, 8, 8}), "mauvaise longueur refusée");

		// --- dés
		check(StatGeneration.dropLowestSum(new int[] {1, 6, 5, 2}) == 13, "4d6 sans le plus petit");
		for (int i = 0; i < 500; i++) {
			int v = StatGeneration.rollOneStat();
			check(v >= 3 && v <= 18, "jet 4d6 dans [3,18]");
		}
		int[] pool = StatGeneration.rollPool();
		boolean sorted = true;
		for (int i = 1; i < pool.length; i++) if (pool[i] > pool[i - 1]) sorted = false;
		check(pool.length == 6 && sorted, "pool de 6 valeurs triées décroissant");
		int[] shuffled = {pool[5], pool[4], pool[3], pool[2], pool[1], pool[0]};
		check(StatGeneration.isPermutation(pool, shuffled), "permutation acceptée");
		int[] cheat = pool.clone(); cheat[0] = 19;
		check(!StatGeneration.isPermutation(pool, cheat), "triche refusée");
		check(Dice.modifier(10) == 0 && Dice.modifier(8) == -1 && Dice.modifier(15) == 2 && Dice.modifier(7) == -2, "modificateurs");

		// --- fiche : création par 27 points
		CharacterSheet s = CharacterSheet.create("Aldric", CharacterClass.LAME_LIGE, Race.HUMAIN, Gender.MALE, Camp.HERO);
		check(!s.isCreated() && s.morality == 15, "fiche initiale, penchant héros +15");
		check(!s.applyPointBuy(new int[] {15, 15, 15, 15, 8, 8}), "achat invalide refusé");
		check(s.applyPointBuy(new int[] {15, 14, 13, 12, 10, 8}), "achat valide accepté");
		check(s.score(Ability.FORCE) == 16, "bonus de race humain +1");
		check(s.maxHp == 10 + Dice.modifier(14), "PV de départ = dé de vie + Con");
		check(!s.applyPointBuy(new int[] {8, 8, 8, 8, 8, 8}), "pas de seconde création");

		// --- niveaux : 175 XP pour le niveau 2, 250 pour le 3
		List<LevelUp> lu = s.addXp(175, false);
		check(lu.size() == 1 && s.level == 2 && s.pointsToSpend == 1, "niveau 2 et 1 point");
		lu = s.addXp(250 + 325, false);
		check(s.level == 4 && s.pointsToSpend == 3, "deux niveaux d'un coup");
		int forceBefore = s.score(Ability.FORCE);
		check(s.spendPoint(Ability.FORCE) && s.score(Ability.FORCE) == forceBefore + 1, "dépense d'un point");
		for (int i = 0; i < 30; i++) { s.pointsToSpend = 1; s.spendPoint(Ability.FORCE); }
		check(s.score(Ability.FORCE) == CharacterSheet.HARD_CAP, "plafond à 20");

		// --- fiche : création par dés
		CharacterSheet d = CharacterSheet.create("Mira", CharacterClass.TISSEUR, Race.SYLVAIN, Gender.FEMALE, Camp.VILLAIN);
		check(d.morality == -15 && Standing.village(d.morality, 0) == Standing.VillageStanding.WELCOME, "futur méchant : commerce ouvert au départ");
		check(!d.assignPool(new int[] {10, 10, 10, 10, 10, 10}), "assigner sans tirage refusé");
		int[] rolled = d.rollPool();
		check(rolled != null && rolled.length == 6, "tirage du pool");
		int[] bad = rolled.clone(); bad[0] = 19;
		check(!d.assignPool(bad), "valeurs inventées refusées");
		int[] ok = {rolled[3], rolled[1], rolled[0], rolled[5], rolled[2], rolled[4]};
		check(d.assignPool(ok) && d.isCreated(), "répartition du tirage acceptée");

		// --- sérialisation
		java.util.Properties p = s.toProperties();
		CharacterSheet back = CharacterSheet.fromProperties(p);
		check(back.name.equals(s.name) && back.level == s.level && back.maxHp == s.maxHp
				&& Arrays.equals(back.baseScores, s.baseScores) && back.state == s.state
				&& back.characterClass == s.characterClass && back.race == s.race, "aller-retour de sérialisation");

		// --- XP des mobs, niveau des mobs
		check(PlayerProgression.xpFromKill(100, 5, 8) == 150 && PlayerProgression.xpFromKill(100, 5, 5) == 100
				&& PlayerProgression.xpFromKill(100, 5, 3) == 50 && PlayerProgression.xpFromKill(100, 10, 2) == 10, "XP selon l'écart de niveau");
		check(MobScaling.computeLevel(1, 0, 0) == 1 && MobScaling.computeLevel(10, 5000, 200) == 19
				&& MobScaling.computeLevel(20, 99999, 99999) == 20, "niveau des mobs borné");
		MobStats ms = MobScaling.statsFor(10, 20);
		check(ms.maxHp() == 50 && ms.armorClass() == 13 && ms.attackBonus() == 4 && ms.damageDice() == 3, "stats de mob niveau 10");

		// --- marchandage
		check(Haggling.resolve(20, 0, 30).result() == Haggling.Result.FREE, "20 naturel = gratuit");
		check(Haggling.resolve(1, 5, 5).result() == Haggling.Result.ANGRY, "1 naturel = colère");
		check(Haggling.resolve(12, 2, 13).result() == Haggling.Result.DISCOUNT, "réussite simple");
		check(Haggling.resolve(15, 2, 10).result() == Haggling.Result.BIG_DISCOUNT, "grosse réussite");
		check(Haggling.resolve(5, 0, 15).result() == Haggling.Result.MARKUP, "échec = prix en hausse");
		check(Haggling.resolve(14, 6, 10).result() == Haggling.Result.FREE, "DD+10 = gratuit");

		// --- engagement
		check(CombatEngagement.resolve(20, 10) == CombatEngagement.Engagement.ADVANTAGE
				&& CombatEngagement.resolve(10, 20) == CombatEngagement.Engagement.DISADVANTAGE
				&& CombatEngagement.resolve(12, 10) == CombatEngagement.Engagement.NEUTRAL, "lancer d'engagement");

		// --- péremption
		check(Spoilage.freshness(0, 1000) == Spoilage.Freshness.FRESH && Spoilage.freshness(300, 1000) == Spoilage.Freshness.GOOD
				&& Spoilage.freshness(800, 1000) == Spoilage.Freshness.STALE && Spoilage.freshness(1000, 1000) == Spoilage.Freshness.ROTTEN
				&& Spoilage.freshness(99999, 0) == Spoilage.Freshness.FRESH, "paliers de fraîcheur");
		check(Spoilage.effectiveAge(0, 2, 0.5) == 24000, "âge en cave");
		check(Spoilage.resolveRottenSave(10, 2) && !Spoilage.resolveRottenSave(5, 2), "sauvegarde de Constitution");

		// --- relations
		Relationship r = new Relationship();
		check(r.stage() == Relationship.Stage.STRANGER, "inconnu au départ");
		check(r.giveGift(Relationship.GiftTaste.LOVED, Spoilage.Freshness.FRESH, 1) == 8 && r.affinity == 8, "cadeau adoré");
		check(r.giveGift(Relationship.GiftTaste.LOVED, Spoilage.Freshness.FRESH, 1) == 0, "un seul cadeau par jour");
		check(Relationship.giftDelta(Relationship.GiftTaste.LOVED, Spoilage.Freshness.ROTTEN) == -8, "cadeau périmé = offense");
		r.affinity = 70;
		check(r.stage() == Relationship.Stage.CLOSE && !new Relationship().confess(20), "étape proche, déclaration trop tôt refusée");
		check(Relationship.resolveConfession(20, 0, 60) && !Relationship.resolveConfession(1, 10, 100), "déclaration : 20 et 1 naturels");

		// --- conjoint
		Spouse sp = new Spouse(); sp.gender = Gender.FEMALE;
		check(sp.stage() == Spouse.BondStage.HARMONY, "harmonie au départ");
		for (int day = 1; day <= 30; day++) sp.dailyTick(day, false, 1);
		check(sp.stage() == Spouse.BondStage.OBSESSION && sp.harmony == 0, "négligence = obsession");
		check(sp.widowName("Aldric").equals("Veuve de Aldric"), "nom du dieu veuve");
		Spouse a = new Spouse(), b = new Spouse();
		for (int day = 1; day <= 4; day++) { a.dailyTick(day, false, 1); b.dailyTick(day, false, 3); }
		check(b.harmony < a.harmony, "plus de conjoints = usure plus rapide");
		check(Spouse.resolveAcceptance(15, 2, 80, 50, true) && !Spouse.resolveAcceptance(5, 0, 20, 0, false), "accord polyamour");
		WidowTransformation wt = new WidowTransformation(10);
		check(wt.state(11) == WidowTransformation.State.MOURNING && wt.state(15) == WidowTransformation.State.TRANSFORMING
				&& wt.state(25) == WidowTransformation.State.DEMON_GOD, "transformation progressive");

		// --- moralité et accueil
		check(Standing.village(-19, 0) == Standing.VillageStanding.WELCOME && Standing.village(-20, 0) == Standing.VillageStanding.WARY
				&& Standing.village(-40, 0) == Standing.VillageStanding.SUSPECT && Standing.village(-60, 0) == Standing.VillageStanding.BANNED
				&& Standing.village(-80, 0) == Standing.VillageStanding.OUTLAW, "paliers d'accueil");
		check(Standing.village(-40, 25) == Standing.VillageStanding.WELCOME, "réputation locale qui compense");
		check(Standing.villagePrice(Standing.VillageStanding.BANNED, -70) == -1 && Standing.villagePrice(Standing.VillageStanding.WARY, -25) == 1.15, "prix selon accueil");
		check(Standing.monsters(0) == Standing.Stance.HOSTILE && Standing.monsters(-30) == Standing.Stance.NEUTRAL && Standing.monsters(-60) == Standing.Stance.ALLY, "attitude des monstres");
		check(Standing.camp(35) == Camp.HERO && Standing.camp(0) == Camp.NEUTRAL && Standing.camp(-35) == Camp.VILLAIN, "camp dérivé de la moralité");
		check(Standing.shiftEvent(-45, -55) == Standing.ShiftEvent.FALL_TO_SHADOW && Standing.shiftEvent(40, 55) == Standing.ShiftEvent.RISE_TO_LIGHT
				&& Standing.shiftEvent(0, -10) == Standing.ShiftEvent.NONE, "événement de basculement");
		int m = 0;
		for (int i = 0; i < 20; i++) m = Standing.applyCrime(m, Crime.KILL_VILLAGER);
		check(m == -100, "moralité plafonnée à -100");
		check(Standing.resolveCrimeSeen(5, 0, 12) && !Standing.resolveCrimeSeen(18, 2, 12), "crime vu ou non");

		// --- épreuve des Limbes
		check(DeathTrial.difficulty(0) == 1.0, "difficulté initiale");
		check(Math.abs(DeathTrial.difficulty(10) - 20.0) < 1e-9 && DeathTrial.difficulty(25) == DeathTrial.difficulty(10), "difficulté plafonnée");
		check(DeathTrial.survivalSeconds(9) == 0 && DeathTrial.survivalSeconds(10) == 300 && DeathTrial.survivalSeconds(11) == 330
				&& DeathTrial.survivalSeconds(500) == 1200, "temps de survie du mode sans fin");
		check(DeathTrial.withMercy(600, 2) == 480 && DeathTrial.withMercy(300, 9) == 300, "indulgence");
		check(DeathTrial.intensity(110, 600) == 0.1 && DeathTrial.intensity(0, 600) == 0.4 && DeathTrial.intensity(400, 600) == 1.0, "rythme et répit");
		check(!DeathTrial.plan(3).endless() && DeathTrial.plan(3).waves() == 4 && DeathTrial.plan(12).endless() && DeathTrial.plan(12).waves() == 0, "plan de l'épreuve");
		check(Persuasion.resolve(() -> 15, 0, 10) && !Persuasion.resolve(() -> 5, 0, 15), "plaidoyer 3 réussites avant 2 échecs");
		check(Persuasion.difficulty(100, 0, true, 1) == 4 && Persuasion.difficulty(0, 20, false, 0) == 22, "difficulté du plaidoyer");

		// --- rescapés, rumeurs
		Survivor sv = new Survivor();
		for (int i = 0; i < 5; i++) sv.observe(Survivor.PlayerTactic.RANGED);
		sv.observe(Survivor.PlayerTactic.FIRE);
		sv.adapt(1);
		check(sv.adaptations.equals(EnumSet.of(Survivor.Adaptation.COVER_AND_CHARGE)), "adaptation à la tactique dominante");
		sv.adapt(3);
		check(sv.adaptations.size() == 2, "plafond d'adaptations respecté");
		check(Survivor.fateFromRoll(20, 3, 10, 5) == Survivor.Fate.HIDES_AS_BOSS && Survivor.fateFromRoll(1, 0, 1, 5) == Survivor.Fate.VANISHES
				&& Survivor.fateFromRoll(12, 3, 5, 5) == Survivor.Fate.RETURNS_HUNTING, "jet de destin");
		Random rnd = new Random(7);
		for (int i = 0; i < 200; i++) {
			int[] h = Hideout.pick(100, -50, rnd, 300, 1500);
			double dist = Math.hypot(h[0] - 100, h[1] + 50);
			check(dist >= 298 && dist <= 1502, "cachette entre 300 et 1500 blocs");
		}
		Rumor ru = new Rumor(UUID.randomUUID(), 100, 0, 20, 0, 0);
		check(ru.precision() == Rumor.Precision.PRECISE && ru.spread().precision() == Rumor.Precision.REGIONAL, "rumeur précise, régionale dès la 2e main");
		check(ru.spread().spread().spread().precision() == Rumor.Precision.REGIONAL && ru.spread().spread().spread().spread().spread().spread().precision() == Rumor.Precision.VAGUE, "rumeur qui se dégrade jusqu'au vague");
		check(new Rumor(null, 0, 0, 500, 0, 0).precision() == Rumor.Precision.VAGUE, "rumeur vague");
		check(ru.direction(0, 0).equals("l'est") && new Rumor(null, 0, 100, 1, 0, 0).direction(0, 0).equals("le sud")
				&& new Rumor(null, -100, 0, 1, 0, 0).direction(0, 0).equals("l'ouest") && new Rumor(null, 0, -100, 1, 0, 0).direction(0, 0).equals("le nord"), "directions cardinales");
		check(RumorLearning.resolve(20, 0, 0) == 2 && RumorLearning.resolve(1, 10, 100) == 0 && RumorLearning.resolve(10, 2, 0) == 1, "interroger un villageois");

		// --- multijoueur
		check(PartyScaling.effectivePartyLevel(List.of(4, 8)) == 7 && PartyScaling.effectivePartyLevel(List.of()) == 1, "niveau de groupe");
		check(PartyScaling.healthMultiplier(1) == 1.0 && PartyScaling.healthMultiplier(3) == 2.0, "PV selon le nombre de joueurs");
		ArenaAllocator arenas = new ArenaAllocator();
		UUID u1 = UUID.randomUUID(), u2 = UUID.randomUUID(), u3 = UUID.randomUUID();
		int[] a1 = arenas.allocate(u1), a2 = arenas.allocate(u2);
		check(!Arrays.equals(a1, a2) && Arrays.equals(a1, arenas.allocate(u1)), "arènes distinctes et stables");
		arenas.release(u1);
		check(Arrays.equals(arenas.allocate(u3), a1), "arène libérée réutilisée");

		// --- compétences
		SkillDef strike = new SkillDef("frappe_fendante", Set.of("lame_lige"), Set.of(), 3, Map.of(Ability.FORCE, 12), 15, 120);
		SkillDef rest = new SkillDef("souffle", Set.of(), Set.of(), 1, Map.of(), 0, 400);
		SkillDef racial = new SkillDef("vue_des_bois", Set.of(), Set.of("sylvain"), 1, Map.of(), 0, 0);
		int[] scores = {14, 10, 10, 10, 10, 10};
		List<SkillDef> all = List.of(strike, rest, racial);
		check(SkillRegistry.unlocked(all, "lame_lige", "humain", 3, scores).size() == 2, "classe + universelle");
		check(SkillRegistry.unlocked(all, "lame_lige", "humain", 2, scores).size() == 1, "niveau insuffisant");
		check(SkillRegistry.unlocked(all, "tisseur", "sylvain", 5, scores).size() == 2, "race + universelle");
		check(SkillRegistry.unlocked(all, "lame_lige", "humain", 3, new int[] {10, 10, 10, 10, 10, 10}).size() == 1, "caractéristique insuffisante");

		System.out.println(checks + " vérifications, " + failures + " échec(s)");
		if (failures > 0) System.exit(1);
	}
}
