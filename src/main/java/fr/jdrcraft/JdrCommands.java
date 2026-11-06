package fr.jdrcraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fr.jdrcraft.core.Ability;
import fr.jdrcraft.core.Camp;
import fr.jdrcraft.core.CharacterClass;
import fr.jdrcraft.core.CharacterSheet;
import fr.jdrcraft.core.CombatEngagement;
import fr.jdrcraft.core.Dice;
import fr.jdrcraft.core.Gender;
import fr.jdrcraft.core.Haggling;
import fr.jdrcraft.core.LevelUp;
import fr.jdrcraft.core.PlayerProgression;
import fr.jdrcraft.core.Race;
import fr.jdrcraft.core.Standing;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Commandes de la phase 1 : elles permettent de jouer et de tester toutes les règles du cœur
 * avant que les écrans (GUI) n'existent.
 */
public final class JdrCommands {
	private static final String[] SCORE_ARGS = {"force", "dex", "con", "int", "sag", "cha"};

	private JdrCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("roll")
				.executes(ctx -> roll(ctx, 20))
				.then(Commands.argument("faces", IntegerArgumentType.integer(2, 1000))
						.executes(ctx -> roll(ctx, IntegerArgumentType.getInteger(ctx, "faces")))));

		LiteralArgumentBuilder<CommandSourceStack> jdr = Commands.literal("jdr");

		jdr.then(Commands.literal("creer")
				.then(Commands.argument("nom", StringArgumentType.word())
						.then(Commands.argument("classe", StringArgumentType.word()).suggests(suggest(CharacterClass.ids()))
								.then(Commands.argument("race", StringArgumentType.word()).suggests(suggest(Race.ids()))
										.then(Commands.argument("genre", StringArgumentType.word()).suggests(suggest(Gender.ids()))
												.then(Commands.argument("camp", StringArgumentType.word()).suggests(suggest(Camp.ids()))
														.executes(JdrCommands::create)))))));

		jdr.then(Commands.literal("stats")
				.then(Commands.literal("points").then(sixScores(JdrCommands::pointBuy)))
				.then(Commands.literal("des").executes(JdrCommands::rollPool))
				.then(Commands.literal("assigner").then(sixScores(JdrCommands::assignPool))));

		jdr.then(Commands.literal("fiche").executes(JdrCommands::showSheet));

		jdr.then(Commands.literal("repartir")
				.then(Commands.argument("caracteristique", StringArgumentType.word()).suggests(suggest(Ability.ids()))
						.executes(JdrCommands::spendPoint)));

		jdr.then(Commands.literal("tester")
				.then(Commands.literal("marchandage")
						.then(Commands.argument("dd", IntegerArgumentType.integer(1, 40)).executes(JdrCommands::testHaggle)))
				.then(Commands.literal("engagement")
						.then(Commands.argument("dex_ennemi", IntegerArgumentType.integer(1, 30)).executes(JdrCommands::testEngagement))));

		// Commande de développement : lancer le jeu avec -Djdrcraft.dev=true pour l'activer.
		jdr.then(Commands.literal("xp")
				.then(Commands.argument("montant", IntegerArgumentType.integer(1, 100000)).executes(JdrCommands::addXp)));

		dispatcher.register(jdr);
	}

	// ---------------------------------------------------------------- outils

	private static SuggestionProvider<CommandSourceStack> suggest(List<String> values) {
		return (ctx, builder) -> SharedSuggestionProvider.suggest(values, builder);
	}

	private static RequiredArgumentBuilder<CommandSourceStack, Integer> sixScores(Command<CommandSourceStack> exec) {
		RequiredArgumentBuilder<CommandSourceStack, Integer> tail =
				Commands.argument(SCORE_ARGS[5], IntegerArgumentType.integer(1, 30)).executes(exec);
		for (int i = 4; i >= 0; i--) {
			tail = Commands.argument(SCORE_ARGS[i], IntegerArgumentType.integer(1, 30)).then(tail);
		}
		return tail;
	}

	private static int[] readSix(CommandContext<CommandSourceStack> ctx) {
		int[] scores = new int[6];
		for (int i = 0; i < 6; i++) scores[i] = IntegerArgumentType.getInteger(ctx, SCORE_ARGS[i]);
		return scores;
	}

	private static int ok(CommandContext<CommandSourceStack> ctx, String message) {
		ctx.getSource().sendSuccess(() -> Component.literal(message), false);
		return 1;
	}

	private static int fail(CommandContext<CommandSourceStack> ctx, String message) {
		ctx.getSource().sendFailure(Component.literal(message));
		return 0;
	}

	private static UUID uuid(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer p = ctx.getSource().getPlayerOrException();
		return p.getUUID();
	}

	private static String campLabel(Camp c) {
		return switch (c) { case HERO -> "héros"; case NEUTRAL -> "neutre"; case VILLAIN -> "maléfique"; };
	}

	private static String join(int[] values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) { if (i > 0) sb.append(", "); sb.append(values[i]); }
		return sb.toString();
	}

	private static final String NO_SHEET = "Tu n'as pas encore de personnage. Commence par /jdr creer <nom> <classe> <race> <genre> <camp>";
	private static final String NOT_READY = "Ton personnage n'est pas terminé : choisis tes caractéristiques avec /jdr stats points ... ou /jdr stats des.";

	// ---------------------------------------------------------------- commandes

	private static int roll(CommandContext<CommandSourceStack> ctx, int faces) {
		int result = Dice.roll(1, faces);
		String who = ctx.getSource().getTextName();
		ctx.getSource().sendSuccess(() -> Component.literal("🎲 " + who + " lance un d" + faces + " : " + result), true);
		return result;
	}

	private static int create(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID id = uuid(ctx);
		CharacterSheet existing = JdrCraft.store().get(id);
		if (existing != null && existing.isCreated()) {
			return fail(ctx, "Tu as déjà un personnage (" + existing.name + "). Utilise /jdr fiche.");
		}
		Optional<CharacterClass> cls = CharacterClass.parse(StringArgumentType.getString(ctx, "classe"));
		Optional<Race> race = Race.parse(StringArgumentType.getString(ctx, "race"));
		Optional<Gender> gender = Gender.parse(StringArgumentType.getString(ctx, "genre"));
		Optional<Camp> camp = Camp.parse(StringArgumentType.getString(ctx, "camp"));
		if (cls.isEmpty()) return fail(ctx, "Classe inconnue. Choix : " + String.join(", ", CharacterClass.ids()));
		if (race.isEmpty()) return fail(ctx, "Race inconnue. Choix : " + String.join(", ", Race.ids()));
		if (gender.isEmpty()) return fail(ctx, "Genre inconnu. Choix : " + String.join(", ", Gender.ids()));
		if (camp.isEmpty()) return fail(ctx, "Camp inconnu. Choix : " + String.join(", ", Camp.ids()));

		CharacterSheet sheet = CharacterSheet.create(StringArgumentType.getString(ctx, "nom"),
				cls.get(), race.get(), gender.get(), camp.get());
		JdrCraft.store().put(id, sheet);
		return ok(ctx, "Personnage « " + sheet.name + " » créé (" + cls.get().label() + ", " + race.get().label() + ").\n"
				+ "Choisis tes caractéristiques :\n"
				+ " • /jdr stats points <force> <dex> <con> <int> <sag> <cha>  (27 points, scores de 8 à 15)\n"
				+ " • /jdr stats des  (tirage 4d6, puis /jdr stats assigner ...)");
	}

	private static int pointBuy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID id = uuid(ctx);
		CharacterSheet sheet = JdrCraft.store().get(id);
		if (sheet == null) return fail(ctx, NO_SHEET);
		if (sheet.isCreated()) return fail(ctx, "Ton personnage est déjà terminé.");
		int[] scores = readSix(ctx);
		if (!sheet.applyPointBuy(scores)) {
			int cost = fr.jdrcraft.core.StatGeneration.totalCost(scores);
			return fail(ctx, cost < 0
					? "Chaque score doit être compris entre 8 et 15."
					: "Répartition refusée : " + cost + " points dépensés, le maximum est 27.");
		}
		JdrCraft.store().save(id);
		return ok(ctx, "Caractéristiques enregistrées (27 points). Utilise /jdr fiche pour voir ton personnage.");
	}

	private static int rollPool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID id = uuid(ctx);
		CharacterSheet sheet = JdrCraft.store().get(id);
		if (sheet == null) return fail(ctx, NO_SHEET);
		int[] pool = sheet.rollPool();
		if (pool == null) return fail(ctx, "Ton personnage est déjà terminé.");
		JdrCraft.store().save(id);
		return ok(ctx, "🎲 Tirage (4d6, le plus petit dé retiré) : " + join(pool) + "\n"
				+ "Répartis ces six valeurs : /jdr stats assigner <force> <dex> <con> <int> <sag> <cha>");
	}

	private static int assignPool(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID id = uuid(ctx);
		CharacterSheet sheet = JdrCraft.store().get(id);
		if (sheet == null) return fail(ctx, NO_SHEET);
		if (!sheet.assignPool(readSix(ctx))) {
			return fail(ctx, "Utilise exactement les six valeurs de ton tirage (" + join(sheet.rolledPool) + "), dans l'ordre que tu veux.");
		}
		JdrCraft.store().save(id);
		return ok(ctx, "Caractéristiques enregistrées. Utilise /jdr fiche pour voir ton personnage.");
	}

	private static int showSheet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CharacterSheet s = JdrCraft.store().get(uuid(ctx));
		if (s == null) return fail(ctx, NO_SHEET);
		StringBuilder sb = new StringBuilder();
		sb.append("— ").append(s.name).append(" · ").append(s.characterClass.label()).append(' ')
				.append(s.race.label()).append(" · niveau ").append(s.level).append(" —\n");
		if (!s.isCreated()) {
			sb.append(NOT_READY);
			return ok(ctx, sb.toString());
		}
		int needed = s.level >= PlayerProgression.MAX_LEVEL ? 0 : PlayerProgression.xpToNext(s.level);
		sb.append("PV ").append(s.maxHp).append(" · XP ").append(s.xp).append('/').append(needed)
				.append(" · points à répartir : ").append(s.pointsToSpend).append('\n');
		sb.append("Camp : ").append(campLabel(s.camp())).append(" (moralité ").append(s.morality).append(")\n");
		for (Ability a : Ability.values()) {
			int mod = s.modifier(a);
			sb.append(a.label()).append(' ').append(s.score(a)).append(" (").append(mod >= 0 ? "+" : "").append(mod).append(")  ");
		}
		return ok(ctx, sb.toString().trim());
	}

	private static int spendPoint(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID id = uuid(ctx);
		CharacterSheet s = JdrCraft.store().get(id);
		if (s == null) return fail(ctx, NO_SHEET);
		Optional<Ability> ability = Ability.parse(StringArgumentType.getString(ctx, "caracteristique"));
		if (ability.isEmpty()) return fail(ctx, "Caractéristique inconnue. Choix : " + String.join(", ", Ability.ids()));
		if (!s.spendPoint(ability.get())) {
			return fail(ctx, "Impossible : aucun point à répartir, ou cette caractéristique est déjà au maximum (" + CharacterSheet.HARD_CAP + ").");
		}
		JdrCraft.store().save(id);
		return ok(ctx, ability.get().label() + " passe à " + s.score(ability.get()) + ". Points restants : " + s.pointsToSpend);
	}

	private static int testHaggle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CharacterSheet s = JdrCraft.store().get(uuid(ctx));
		if (s == null) return fail(ctx, NO_SHEET);
		if (!s.isCreated()) return fail(ctx, NOT_READY);
		int dc = IntegerArgumentType.getInteger(ctx, "dd");
		Haggling.Outcome o = Haggling.attempt(s.score(Ability.CHARISME), dc);
		String verdict = switch (o.result()) {
			case FREE -> "le produit est offert !";
			case BIG_DISCOUNT -> "prix réduit de moitié";
			case DISCOUNT -> "prix réduit de 25 %";
			case MARKUP -> "prix augmenté de 25 %";
			case ANGRY -> "prix augmenté de 50 % et le villageois s'énerve !";
		};
		return ok(ctx, "🎲 d20 = " + o.roll() + " + " + (o.total() - o.roll()) + " Charisme = " + o.total() + " contre DD " + dc + " → " + verdict);
	}

	private static int testEngagement(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		CharacterSheet s = JdrCraft.store().get(uuid(ctx));
		if (s == null) return fail(ctx, NO_SHEET);
		if (!s.isCreated()) return fail(ctx, NOT_READY);
		int enemyDex = IntegerArgumentType.getInteger(ctx, "dex_ennemi");
		int p = Dice.d20() + s.modifier(Ability.DEXTERITE);
		int e = Dice.d20() + Dice.modifier(enemyDex);
		String verdict = switch (CombatEngagement.resolve(p, e)) {
			case ADVANTAGE -> "tu es avantagé : première attaque avec avantage";
			case NEUTRAL -> "départ équilibré";
			case DISADVANTAGE -> "tu es désavantagé : l'ennemi frappe en premier";
		};
		return ok(ctx, "🎲 Toi " + p + " contre ennemi " + e + " → " + verdict);
	}

	private static int addXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (!Boolean.getBoolean("jdrcraft.dev")) {
			return fail(ctx, "Commande de développement désactivée (lancer le jeu avec -Djdrcraft.dev=true).");
		}
		UUID id = uuid(ctx);
		CharacterSheet s = JdrCraft.store().get(id);
		if (s == null) return fail(ctx, NO_SHEET);
		if (!s.isCreated()) return fail(ctx, NOT_READY);
		List<LevelUp> gained = s.addXp(IntegerArgumentType.getInteger(ctx, "montant"), true);
		JdrCraft.store().save(id);
		StringBuilder sb = new StringBuilder("XP ajoutée.");
		for (LevelUp l : gained) {
			sb.append("\n✨ Niveau ").append(l.newLevel()).append(" ! +").append(l.hpGained()).append(" PV, +")
					.append(l.pointsGained()).append(" point à répartir (/jdr repartir <caracteristique>)");
		}
		return ok(ctx, sb.toString());
	}
}
