# JDRCraft — mod Fabric pour Minecraft 26.3

Transforme le monde de Minecraft en campagne de jeu de rôle : personnage, dés, niveaux, camps héros/maléfique,
villageois vivants, rescapés qui apprennent, épreuve des Limbes. Style JDR « à la Donjons & Dragons », avec des
noms et un univers originaux.

## État d'avancement

| Phase | Contenu | État |
|---|---|---|
| **1** | **Cœur de règles** (dés, 27 points, tirage 4d6, fiche, niveaux, points à répartir, marchandage, engagement, péremption, relations et harmonie, moralité et accueil des villages, rescapés, rumeurs, Limbes, groupes, arènes, compétences) + commandes de test + sauvegarde par joueur | **Livré** (cœur testé, 784 vérifications) |
| 2 | HUD/GUI façon Diablo 3 / Path of Exile : globes, barre de compétences, barre d'XP, minimap, suivi de quêtes, écran de création et de répartition des points | À faire |
| 3 | Marchandage en jeu avec de vrais villageois, lancer d'engagement avant combat, péremption sur les objets (composants d'objets), marchands monstres | À faire |
| 4 | Villageois vivants : besoins, métiers, économie, construction, défense, dialogues | À faire |
| 5 | Rescapés et rumeurs dans le monde (registre sauvegardé, matérialisation paresseuse), niveaux des mobs | À faire |
| 6 | Dimension des Limbes (data pack), conjoint qui emménage, obsession, dieu veuf/veuve dans le Nether | À faire |
| 7 | Multijoueur complet : groupes, rôle de Maître du Donjon, permissions, configuration serveur | À faire |

Dans la phase 1, **toutes les règles sont déjà écrites et testées**, dans `fr.jdrcraft.core`, sans dépendance à Minecraft.
Les phases suivantes consistent surtout à brancher ces règles sur le jeu (entités, interface, dimension).

## Prérequis

- Java 25
- Gradle 9.6.0 (le wrapper n'est pas fourni : `gradle wrapper --gradle-version 9.6.0`, ou copier le dossier `gradle/` et les scripts du modèle officiel `fabric-example-mod`)
- Fabric Loom 1.17, Fabric Loader 0.19.5, Fabric API 0.165.2+26.3

Les numéros de version sont dans `gradle.properties`. Vérifie-les sur https://fabricmc.net/develop/ avant le premier build.

## Construire et lancer

```
gradle wrapper --gradle-version 9.6.0
./gradlew build           # jar dans build/libs/
./gradlew runClient       # client de développement
./gradlew runServer       # serveur de développement
```

## Commandes (phase 1)

| Commande | Rôle |
|---|---|
| `/jdr creer <nom> <classe> <race> <genre> <camp>` | Crée le personnage (le camp n'est qu'un penchant de départ) |
| `/jdr stats points <force> <dex> <con> <int> <sag> <cha>` | Méthode des 27 points (scores de 8 à 15) |
| `/jdr stats des` puis `/jdr stats assigner <six valeurs>` | Tirage 4d6 (le serveur lance), puis répartition |
| `/jdr fiche` | Affiche la fiche |
| `/jdr repartir <caracteristique>` | Dépense un point gagné en montant de niveau |
| `/jdr tester marchandage <dd>` | Simule un marchandage (d20 + Charisme) |
| `/jdr tester engagement <dex_ennemi>` | Simule le lancer d'engagement avant combat |
| `/jdr xp <montant>` | Développement : lancer le jeu avec `-Djdrcraft.dev=true` |
| `/roll [faces]` | Lance un dé (d20 par défaut) |

Les fiches sont enregistrées dans le dossier du monde (`<monde>/jdrcraft/<uuid>.properties`).

## Tester le cœur sans Minecraft

```
javac -encoding UTF-8 -d out $(find src/main/java/fr/jdrcraft/core src/test/java -name '*.java')
java -cp out fr.jdrcraft.core.CoreSelfTest
```

## Organisation

- `fr.jdrcraft.core` : toutes les règles (testées, sans Minecraft)
- `fr.jdrcraft` : colle avec Minecraft et Fabric (commandes, sauvegarde)
- `src/client/java` : code client (HUD et écrans, à venir)

## Points non vérifiés

- Le code qui touche à Minecraft et à Fabric (`JdrCraft`, `JdrCommands`, `PlayerStore`, `JdrCraftClient`) a été vérifié
  seulement contre des signatures simulées, pas contre le jeu 26.3 réel. Une adaptation de noms peut être nécessaire au
  premier `./gradlew build`.
- Les noms de classes et de races sont provisoires.
