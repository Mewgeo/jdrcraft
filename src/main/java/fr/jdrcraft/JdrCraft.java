package fr.jdrcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/** Point d'entrée serveur/commun. Toute la logique de règles vit dans fr.jdrcraft.core (sans dépendance à Minecraft). */
public class JdrCraft implements ModInitializer {
	public static final String MOD_ID = "jdrcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static PlayerStore store;

	public static PlayerStore store() { return store; }

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Path dir = server.getWorldPath(LevelResource.ROOT).resolve(MOD_ID);
			store = new PlayerStore(dir);
			LOGGER.info("JDRCraft : données des personnages dans {}", dir);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			if (store != null) store.saveAll();
		});

		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> JdrCommands.register(dispatcher));

		LOGGER.info("JDRCraft initialisé");
	}
}
