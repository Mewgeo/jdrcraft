package fr.jdrcraft;

import net.fabricmc.api.ClientModInitializer;

/** Point d'entrée client. Le HUD, les écrans (fiche, compétences, quêtes) et la minimap viendront ici (phase 2). */
public class JdrCraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		JdrCraft.LOGGER.info("JDRCraft (client) initialisé");
	}
}
