package fr.jdrcraft;

import fr.jdrcraft.core.CharacterSheet;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/** Stockage des fiches : un fichier .properties par joueur dans le dossier du monde (propre à chaque monde). */
public final class PlayerStore {
	private final Path dir;
	private final Map<UUID, CharacterSheet> cache = new HashMap<>();

	public PlayerStore(Path dir) { this.dir = dir; }

	/** La fiche du joueur, ou null s'il n'en a pas encore. */
	public synchronized CharacterSheet get(UUID id) {
		CharacterSheet cached = cache.get(id);
		if (cached != null) return cached;
		Path file = dir.resolve(id + ".properties");
		if (!Files.exists(file)) return null;
		try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			Properties p = new Properties();
			p.load(r);
			CharacterSheet sheet = CharacterSheet.fromProperties(p);
			cache.put(id, sheet);
			return sheet;
		} catch (IOException e) {
			JdrCraft.LOGGER.error("Lecture impossible de la fiche {}", id, e);
			return null;
		}
	}

	public synchronized void put(UUID id, CharacterSheet sheet) {
		cache.put(id, sheet);
		save(id);
	}

	public synchronized void save(UUID id) {
		CharacterSheet sheet = cache.get(id);
		if (sheet == null) return;
		try {
			Files.createDirectories(dir);
			try (Writer w = Files.newBufferedWriter(dir.resolve(id + ".properties"), StandardCharsets.UTF_8)) {
				sheet.toProperties().store(w, "JDRCraft - fiche de personnage");
			}
		} catch (IOException e) {
			JdrCraft.LOGGER.error("Écriture impossible de la fiche {}", id, e);
		}
	}

	public synchronized void saveAll() {
		for (UUID id : cache.keySet()) save(id);
	}
}
