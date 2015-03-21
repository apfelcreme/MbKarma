package io.github.apfelcreme.MbKarmaBungee;

import io.github.apfelcreme.MbKarmaBungee.Listener.BukkitMessageListener;
import io.github.apfelcreme.MbKarmaBungee.Listener.MbKarmaBungeeTabCompleter;
import io.github.apfelcreme.MbKarmaBungee.Listener.ServerSwitchListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 * MbKarma Copyright (C) 2015 Lord36 aka Apfelcreme
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Lord36 aka Apfelcreme
 * 
 */
public class MbKarmaBungee extends Plugin {

	private Configuration config;
	
	private ChatNotificationTask chatNotificationTask; 

	private final static ConfigurationProvider yamlConfig = ConfigurationProvider
			.getProvider(YamlConfiguration.class);

	public static MbKarmaBungee getInstance() {
		return (MbKarmaBungee) net.md_5.bungee.api.ProxyServer.getInstance()
				.getPluginManager().getPlugin("MbKarmaBungee");
	}

	@Override
	public void onEnable() {
		// config
		try {
			loadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// db
		DatabaseConnectionManager.getInstance().initConnection(
				getConfig().getString("mysql.dbuser", ""),
				getConfig().getString("mysql.dbpassword", ""),
				getConfig().getString("mysql.database", ""),
				getConfig().getString("mysql.url", ""));
		
		
		// commands
		getProxy().getPluginManager().registerCommand(this,
				new MbKarmaCommand("karma"));
		getProxy().getPluginManager().registerCommand(this,
				new SimpleThanksCommand("thx"));
		getProxy().getPluginManager().registerCommand(this,
				new ParticlesCommand("particles"));
		
		// listener
		getProxy().getPluginManager().registerListener(this, new ServerSwitchListener());
		getProxy().getPluginManager().registerListener(this, new MbKarmaBungeeTabCompleter());
		
		// pluginmessage channel
		getProxy().registerChannel("Karma");
        this.getProxy().getPluginManager().registerListener(this, new BukkitMessageListener());

		// plugins
		if (getProxy().getPluginManager().getPlugin("UUIDDB") == null) {
			getLogger().severe("Plugin UUIDDB konnte nicht gefunden werden!");
		}
		
		// tasks
		chatNotificationTask = new ChatNotificationTask(getConfig().getInt(
				"broadcast.notificationDelay",5));
		getProxy().getScheduler().schedule(this, chatNotificationTask, 0, 
				getConfig().getInt("broadcast.notificationDelay"),
				TimeUnit.MINUTES);

//		getProxy().getScheduler().schedule(this, chatNotificationTask, 0, 10,
//		TimeUnit.SECONDS);
	}

	@Override
	public void onDisable() {

	}

	/**
	 * sends a message to all servers
	 * 
	 * @param message
	 */
	public void sendBroadcast(String message) {
		for (ProxiedPlayer p : this.getProxy().getPlayers()) {
			p.sendMessage(new ComponentBuilder(ChatColor.WHITE + "["
					+ ChatColor.LIGHT_PURPLE + "\u2665" + ChatColor.WHITE
					+ "] " + message).create());
		}
	}

	/**
	 * sends a plugin message to the particle plugin notifying it that a players
	 * karma has changed
	 * 
	 * @param targetPlayer
	 * @param effect
	 * @param effectDelay
	 * @param seesParticles
	 */
	public void sendEffectChangeMessage(ProxiedPlayer targetPlayer,
			String effect, long effectDelay, boolean seesParticles) {
		if (targetPlayer == null) {
			// the player is offline and no particles need to be applied
			return;
		}
		ServerInfo serverInfo = targetPlayer.getServer().getInfo();
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("EffectChange");
		out.writeUTF(targetPlayer.getUniqueId().toString());
		out.writeUTF(effect);
		out.writeLong(effectDelay);
		out.writeBoolean(seesParticles);
		serverInfo.sendData("Karma", out.toByteArray());
	}

	/**
	 * sends a plugin message to the particle plugin notifying it that a players
	 * karma has changed
	 * 
	 * @param targetPlayer
	 * @param isVisible
	 */
	public void sendParticleToggleMessage(ProxiedPlayer targetPlayer,
			String effect, long effectDelay, boolean seesParticles) {
		if (targetPlayer == null) {
			// the player is offline and no particles need to be applied
			return;
		}
		ServerInfo serverInfo = targetPlayer.getServer().getInfo();
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("ToggleParticleVisibility");
		out.writeUTF(targetPlayer.getUniqueId().toString());
		out.writeUTF(effect);
		out.writeLong(effectDelay);
		out.writeBoolean(seesParticles);
		serverInfo.sendData("Karma", out.toByteArray());
	}

	/**
	 * initializes the plugins configuration
	 * 
	 * @throws IOException
	 */
	public void loadConfig() throws IOException {
		if (config == null) {
			// load config
			File configFile = new File(getDataFolder().getAbsoluteFile()
					+ "/config.yml");

			if (!configFile.exists()) {
				if (!configFile.getParentFile().exists()) {
					configFile.getParentFile().mkdirs();
				}
				configFile.createNewFile();
				config = yamlConfig.load(configFile);
				InputStreamReader isrConfig = new InputStreamReader(
						new FileInputStream(configFile));
				config = yamlConfig.load(isrConfig);
				config.set("mysql.dbuser", "");
				config.set("mysql.dbpassword", "");
				config.set("mysql.database", "");
				config.set("mysql.url", "");
				config.set("broadcast.step", 1);
				config.set("broadcast.stepstart", 1);
				config.set("broadcast.notificationDelay", 5);
				config = createTextNodes(config);
				yamlConfig.save(config, configFile);
				this.getLogger()
						.severe("THE CONFIG.YML FILE HAS BEEN CREATED. PLEASE FILL IT WITH INFORMATION AND RESTART THE PROXY IN ORDER TO INSTALL THE PLUGIN!");
			} else {
				config = yamlConfig.load(configFile);
			}
		}
	}

	/**
	 * returns the plugins configuration
	 * 
	 * @return
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public void setConfig(Configuration config) {
		this.config = config;
	}

	/**
	 * saves the config
	 */
	public void saveConfig() {
		File configFile = new File(getDataFolder().getAbsoluteFile()
				+ "/config.yml");
		try {
			yamlConfig.save(config, configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * enters the standard texts into the config.yml
	 * 
	 * @param config
	 * @return
	 */
	private Configuration createTextNodes(Configuration config) {
		// Error
		config.set("texts.error.noPermission",
				"{RED}Du hast keine ausreichende Berechtigung!");
		config.set("texts.error.missingParameters",
				"{RED}Falsche Eingabe! Es fehlt ein Parameter!");
		config.set("texts.error.unknownFunction",
				"{RED}Unbekannte Funktion: {0}");
		config.set("texts.error.playerOffline",
				"{RED}Der angegebene Spieler ist nicht online!");
		config.set("texts.error.unknownPlayer",
				"{RED}Der angegebene Spieler ist unbekannt!");
		config.set(
				"texts.error.unknownPlayerToggle",
				"{RED}Du hast noch nie Karma erhalten, daher kannst du deine Partikel nicht ausblenden!");
		config.set("texts.error.unknownEffect",
				"{RED}Der gewählte Effekt existiert nicht!");
		config.set("texts.error.noDbConnection",
				"{RED}Bei der Verbindung zur Datenbank ist ein Fehler aufgetreten!");
		config.set("texts.error.particlesTooHigh",
				"{RED}Du hast nicht genügend Karma um diese Partikel auszuwählen!");

		// Broadcast
		// Info: Zeilenumbrüche sind mit ChatColors buggy, sie springen wieder
		// auf weiß. deswegen
		// wird jedes Wort vorher "neu gefärbt"
		config.set(
				"texts.broadcast.transaction",
				"{GRAY}{0}{DARK_GRAY} bedankt sich bei {GRAY}{1}{DARK_GRAY} mit {GRAY}{2}{DARK_GRAY} {DARK_GRAY}Karma. {GRAY}{1} {DARK_GRAY}hat {DARK_GRAY}nun {GRAY}{3} {DARK_GRAY}Karma");
		config.set(
				"texts.broadcast.notificationBroadcast",
				"{GRAY}In den letzten {WHITE}{0}{GRAY} Minuten haben sich insgesamt {WHITE}{1} {GRAY}Spieler {GRAY}bei {WHITE}{2} {GRAY}bedankt!");

		// Info
		config.set(
				"texts.info.oncePerDay",
				"{LIGHT_PURPLE}Du kannst {WHITE}{0}{LIGHT_PURPLE} nur einmal am Tag Karma geben.");
		config.set("texts.info.yourself",
				"{LIGHT_PURPLE}Du kannst dich nicht bei dir selbst bedanken.");
		config.set(
				"texts.info.listHead",
				"{LIGHT_PURPLE}Spieler: {WHITE}{0}{LIGHT_PURPLE}, P={WHITE}Player{LIGHT_PURPLE}, R={WHITE}Ratio{LIGHT_PURPLE}, K={WHITE}verg. Karma{LIGHT_PURPLE}, E={WHITE}erh. Karma{LIGHT_PURPLE}, {LIGHT_PURPLE}Z={WHITE}Letzte Transakt.");
		config.set(
				"texts.info.list",
				"{LIGHT_PURPLE}P: {WHITE}{0} {LIGHT_PURPLE}R: {WHITE}{1} {LIGHT_PURPLE}K: {WHITE}{2} {LIGHT_PURPLE}E: {WHITE}{3} {LIGHT_PURPLE}Z: {WHITE}{4}");
		config.set("texts.info.topListHead",
				"{LIGHT_PURPLE}Folgende Spieler führen die Liste an: ");
		config.set("texts.info.topList",
				"{LIGHT_PURPLE}Spieler: {WHITE}{0} {LIGHT_PURPLE}Karma: {WHITE}{1}");
		config.set("texts.info.reset",
				"{WHITE}{0}{LIGHT_PURPLE}'s Karma wurde zurückgesetzt.");
		config.set(
				"texts.info.set",
				"{WHITE}{0}{LIGHT_PURPLE}'s Karma wurde auf {WHITE}{1}{LIGHT_PURPLE} Karma gesetzt.");
		config.set("texts.info.toggle",
				"{LIGHT_PURPLE}Karma-Partikel sind nun für dich {WHITE}{0}{LIGHT_PURPLE}!");
		config.set(
				"texts.info.info",
				"{LIGHT_PURPLE}Info für Spieler {WHITE}{0}{LIGHT_PURPLE}: Karma: {WHITE}{1}{LIGHT_PURPLE} Für mehr Infos: {WHITE}/karma list {0}");
		config.set("texts.info.noInfo",
				"{LIGHT_PURPLE}Dieser Spieler hat weder Karma vergeben noch erhalten!");
		config.set("texts.info.configReloaded",
				"{LIGHT_PURPLE}Die Config wurde neu geladen!");
		config.set("texts.info.configRegenerated",
				"{LIGHT_PURPLE}Die Config wurde neu generiert!");
		config.set("texts.info.particlesListHead",
				"{LIGHT_PURPLE}Folgende Partikel kannst du wählen: ");
		config.set("texts.info.particlesListElementAvailable",
				"{LIGHT_PURPLE}{0}{WHITE}: ab {GREEN}{1}{WHITE} Karma");
		config.set("texts.info.particlesListElementUnavailable",
				"{GRAY}{0}{WHITE}: ab {RED}{1}{WHITE} Karma");
		config.set(
				"texts.info.particlesListBottom",
				"{LIGHT_PURPLE}Wähle verfügbare Partikel mit {WHITE}/particles <Partikel>{LIGHT_PURPLE}! ");
		config.set("texts.info.particlesChanged",
				"{LIGHT_PURPLE}Deine Partikel wurden erfolgreich geändert");

		config.set("texts.info.karmaReceived",
				"{LIGHT_PURPLE}Du hast {WHITE}{0}{LIGHT_PURPLE} Karma von {WHITE}{1}{LIGHT_PURPLE} erhalten!");
		config.set("texts.info.karmaSent",
				"{LIGHT_PURPLE}Du hast {WHITE}{0}{LIGHT_PURPLE} Karma an {WHITE}{1}{LIGHT_PURPLE} gegeben!");
		// Help
		config.set(
				"texts.help.give",
				"{LIGHT_PURPLE}/karma give <Spieler> {WHITE}/{LIGHT_PURPLE} /thx <Spieler>{WHITE}: Gibt einem Spieler Karma");
		config.set(
				"texts.help.list",
				"{LIGHT_PURPLE}/karma list <Spieler> {WHITE}: Zeigt eine Liste aller Beziehungen dieses Spielers");
		config.set("texts.help.particlesList",
				"{LIGHT_PURPLE}/particles {WHITE}: Zeigt deine auswählbaren Partikel an");
		config.set(
				"texts.help.particlesSet",
				"{LIGHT_PURPLE}/particles <Partikel> {WHITE}: Wählt die Partikel aus, die dich umgeben");
		config.set(
				"texts.help.reset",
				"{LIGHT_PURPLE}/karma reset <Spieler> {WHITE}: Setzt den Karma-Wert des Spielers auf 0. Alle Beziehungen werden beibehalten");
		config.set(
				"texts.help.info",
				"{LIGHT_PURPLE}/karma info <Spieler> {WHITE}: Zeigt eine kurze Info des Spielers an");
		config.set(
				"texts.help.top",
				"{LIGHT_PURPLE}/karma top {WHITE}: Zeigt die 10 Spieler mit dem meisten Karma an");
		config.set(
				"texts.help.set",
				"{LIGHT_PURPLE}/karma set <Spieler> <Menge> {WHITE}: Setzt den Karma-Wert des Spielers auf die angegebene Menge");
		config.set("texts.help.reload",
				"{LIGHT_PURPLE}/karma reload {WHITE}: Lädt die Config neu");
		config.set(
				"texts.help.regenerate",
				"{LIGHT_PURPLE}/karma regenerate {WHITE}: Generiert die Config neu. DB-Daten werden beibehalten und eine Kopie der alten Config wird erzeugt");
		config.set(
				"texts.help.toggle",
				"{LIGHT_PURPLE}/particles toggle {WHITE}: Togglet die Sichtbarkeit der eigenen Partikel für andere Spieler");


		// effect specifications
		config.set("effects.SMOKE.delay", 10L);
		config.set("effects.SMOKE.displaytext", "Smoke");
		config.set("effects.SMOKE.aliases", Arrays.asList("SMOKE", "RAUCH"));
		config.set("effects.POTION_BREAK.delay", 10L);
		config.set("effects.POTION_BREAK.displaytext", "Potion-Break");
		config.set("effects.POTION_BREAK.aliases",
				Arrays.asList("POTION_BREAK", "POTION-BREAK", "POTIONBREAK"));
		config.set("effects.ENDER_SIGNAL.delay", 10L);
		config.set("effects.ENDER_SIGNAL.displaytext", "Endersignal");
		config.set("effects.ENDER_SIGNAL.aliases",
				Arrays.asList("ENDER_SIGNAL", "ENDERSIGNAL", "ENDER-SIGNAL"));
		config.set("effects.MOBSPAWNER_FLAMES.delay", 10L);
		config.set("effects.MOBSPAWNER_FLAMES.displaytext", "Mobspawner-Flames");
		config.set("effects.MOBSPAWNER_FLAMES.aliases", Arrays.asList(
				"MOBSPAWNER_FLAMES", "MOBSPAWNER-FLAMES", "MOBSPAWNER",
				"MOBSPAWNERFLAMES"));
		config.set("effects.FIREWORKS_SPARK.delay", 10L);
		config.set("effects.FIREWORKS_SPARK.displaytext", "Fireworks-Spark");
		config.set("effects.FIREWORKS_SPARK.aliases", Arrays.asList(
				"FIREWORKS_SPARK", "FIREWORKS-SPARK", "FIREWORKSSPARK",
				"FEUERWERK"));
		config.set("effects.CRIT.delay", 10L);
		config.set("effects.CRIT.displaytext", "Crit");
		config.set("effects.CRIT.aliases", Arrays.asList("CRIT", "KRIT"));
		config.set("effects.MAGIC_CRIT.delay", 10L);
		config.set("effects.MAGIC_CRIT.displaytext", "Magiccrit");
		config.set("effects.MAGIC_CRIT.aliases", Arrays.asList("MAGIC_CRIT",
				"MAGICCRIT", "MAGIC-CRIT", "MAGIC-KRIT", "MAGIE-KRIT",
				"MAGIEKRIT"));
		config.set("effects.POTION_SWIRL.delay", 10L);
		config.set("effects.POTION_SWIRL.displaytext", "Potionswirl");
		config.set("effects.POTION_SWIRL.aliases",
				Arrays.asList("POTION_SWIRL", "POTIONSWIRL", "POTION-SWIRL"));
		config.set("effects.POTION_SWIRL_TRANSPARENT.delay", 10L);
		config.set("effects.POTION_SWIRL_TRANSPARENT.displaytext",
				"Potionswirl-Transparent");
		config.set("effects.POTION_SWIRL_TRANSPARENT.aliases", Arrays.asList(
				"POTION_SWIRL_TRANSPARENT", "POTION_SWIRL-TRANSPARENT",
				"POTIONSWIRL-TRANSPARENT", "POTION-SWIRL-TRANSPARENT",
				"POTIONSWIRLTRANSPARENT"));
		config.set("effects.SPELL.delay", 5L);
		config.set("effects.SPELL.displaytext", "Spell");
		config.set("effects.SPELL.aliases", Arrays.asList("SPELL", "SPRUCH"));
		config.set("effects.INSTANT_SPELL.delay", 5L);
		config.set("effects.INSTANT_SPELL.displaytext", "Instantspell");
		config.set("effects.INSTANT_SPELL.aliases", Arrays.asList(
				"INSTANT_SPELL", "INSTANTSPELL", "INSTANT-SPELL",
				"INSTANT-SPRUCH"));
		config.set("effects.WITCH_MAGIC.delay", 5L);
		config.set("effects.WITCH_MAGIC.displaytext", "Witchmagic");
		config.set("effects.WITCH_MAGIC.aliases", Arrays.asList("WITCH_MAGIC",
				"WITCHMAGIC", "WITCH-MAGIC", "WITCH", "HEXE", "HEXENMAGIE",
				"HEXEN-MAGIE", "HEXEN-MAGIC"));
		config.set("effects.NOTE.delay", 5L);
		config.set("effects.NOTE.displaytext", "Note");
		config.set("effects.NOTE.aliases", Arrays.asList("NOTE", "MUSIK"));
		config.set("effects.PORTAL.delay", 2L);
		config.set("effects.PORTAL.displaytext", "Portal");
		config.set("effects.PORTAL.aliases",
				Arrays.asList("PORTAL", "NETHERPORTAL", "NETHER-PORTAL"));
		config.set("effects.FLYING_GLYPH.delay", 5L);
		config.set("effects.FLYING_GLYPH.displaytext", "Flying-Glyph");
		config.set("effects.FLYING_GLYPH.aliases", Arrays.asList(
				"FLYING_GLYPH", "FLYING-GLYPH", "GLYPH", "FLIEGENDER-GLYPH",
				"GLYPHE"));
		config.set("effects.FLAME.delay", 3L);
		config.set("effects.FLAME.displaytext", "Flame");
		config.set("effects.FLAME.aliases",
				Arrays.asList("FLAME", "FLAMME", "FEUER"));
		config.set("effects.LAVA_POP.delay", 4L);
		config.set("effects.LAVA_POP.displaytext", "Lava-Pop");
		config.set("effects.LAVA_POP.aliases", Arrays.asList("LAVA_POP",
				"LAVA-POP", "LAVAPOP", "LAVAEXPLOSION"));
		config.set("effects.SPLASH.delay", 5L);
		config.set("effects.SPLASH.displaytext", "Splash");
		config.set("effects.SPLASH.aliases", Arrays.asList("SPLASH", "SPLASCH"));
		config.set("effects.PARTICLE_SMOKE.delay", 5L);
		config.set("effects.PARTICLE_SMOKE.displaytext", "Particle-Smoke");
		config.set("effects.PARTICLE_SMOKE.aliases", Arrays.asList(
				"PARTICLE_SMOKE", "PARTICLE-SMOKE", "PARTIKEL-RAUCH",
				"PARTIKELRAUCH", "PARTICLESMOKE"));
		config.set("effects.EXPLOSION_HUGE.delay", 20L);
		config.set("effects.EXPLOSION_HUGE.displaytext", "Explosion-Sehr-Groß");
		config.set("effects.EXPLOSION_HUGE.aliases", Arrays.asList(
				"EXPLOSION_HUGE", "EXPLOSION-SEHR-GROß", "EXPLOSION-HUGE"));
		config.set("effects.EXPLOSION_LARGE.delay", 15L);
		config.set("effects.EXPLOSION_LARGE.displaytext", "Explosion-Groß");
		config.set("effects.EXPLOSION_LARGE.aliases", Arrays.asList(
				"EXPLOSION_LARGE", "EXPLOSION-GROß", "EXPLOSIONLARGE"));
		config.set("effects.EXPLOSION.delay", 10L);
		config.set("effects.EXPLOSION.displaytext", "Explosion");
		config.set("effects.EXPLOSION.aliases",
				Arrays.asList("EXPLOSION", "EXPLOSION-NORMAL"));
		config.set("effects.VOID_FOG.delay", 1L);
		config.set("effects.VOID_FOG.displaytext", "Void-Fog");
		config.set("effects.VOID_FOG.aliases", Arrays.asList("VOID_FOG",
				"VOIDFOG", "VOID-FOG", "VOIDNEBEL", "VOID"));
		config.set("effects.SMALL_SMOKE.delay", 5L);
		config.set("effects.SMALL_SMOKE.displaytext", "Wenig-Rauch");
		config.set("effects.SMALL_SMOKE.aliases", Arrays.asList("SMALL_SMOKE",
				"SMALLSMOKE", "WENIG-RAUCH", "WENIG-SMOKE"));
		config.set("effects.CLOUD.delay", 5L);
		config.set("effects.CLOUD.displaytext", "Cloud");
		config.set("effects.CLOUD.aliases", Arrays.asList("CLOUD", "WOLKE"));
		config.set("effects.COLOURED_DUST.delay", 3L);
		config.set("effects.COLOURED_DUST.displaytext", "Coloured-Dust");
		config.set("effects.COLOURED_DUST.aliases", Arrays.asList(
				"COLOURED_DUST", "COLOURED-DUST", "REDSTONE", "FARBSTAUB",
				"COLOUREDDUST"));
		config.set("effects.SNOWBALL_BREAK.delay", 4L);
		config.set("effects.SNOWBALL_BREAK.displaytext", "Schneeball-Break");
		config.set("effects.SNOWBALL_BREAK.aliases", Arrays.asList(
				"SNOWBALL_BREAK", "SCHNEEBALL-BREAK", "SNOWBALL-BREAK",
				"SCHNEEBALL"));
		config.set("effects.WATERDRIP.delay", 5L);
		config.set("effects.WATERDRIP.displaytext", "Water-Drip");
		config.set("effects.WATERDRIP.aliases", Arrays.asList("WATERDRIP",
				"WATER-DRIP", "WASSERTROPFEN", "REGEN", "RAIN", "WATERDROP"));
		config.set("effects.LAVADRIP.delay", 5L);
		config.set("effects.LAVADRIP.displaytext", "Lava-Drip");
		config.set("effects.LAVADRIP.aliases",
				Arrays.asList("LAVADRIP", "LAVA-DRIP", "LAVADROP"));
		config.set("effects.SNOW_SHOVEL.delay", 5L);
		config.set("effects.SNOW_SHOVEL.displaytext", "Schneeschaufel");
		config.set("effects.SNOW_SHOVEL.aliases",
				Arrays.asList("SNOW_SHOVEL", "SCHNEESCHAUFEL"));
		config.set("effects.SLIME.delay", 5L);
		config.set("effects.SLIME.displaytext", "Slime");
		config.set("effects.SLIME.aliases", Arrays.asList("SLIME", "SCHLEIM"));
		config.set("effects.HEART.delay", 5L);
		config.set("effects.HEART.displaytext", "Heart");
		config.set("effects.HEART.aliases",
				Arrays.asList("HEART", "HERZ", "HERZCHEN"));
		config.set("effects.VILLAGER_THUNDERCLOUD.delay", 5L);
		config.set("effects.VILLAGER_THUNDERCLOUD.displaytext",
				"Villager-Thundercloud");
		config.set("effects.VILLAGER_THUNDERCLOUD.aliases",
				Arrays.asList("VILLAGER_THUNDERCLOUD", "VILLAGER-THUNDERCLOUD"));
		config.set("effects.HAPPY_VILLAGER.delay", 5L);
		config.set("effects.HAPPY_VILLAGER.displaytext", "Happy-Villager");
		config.set("effects.HAPPY_VILLAGER.aliases",
				Arrays.asList("HAPPY_VILLAGER", "HAPPY-VILLAGER"));
		config.set("effects.LARGE_SMOKE.delay", 8L);
		config.set("effects.LARGE_SMOKE.displaytext", "Viel-Rauch");
		config.set("effects.LARGE_SMOKE.aliases",
				Arrays.asList("LARGE_SMOKE", "LARGE-SMOKE", "VIEL-RAUCH"));

		// effect ranges
		config.set("level.5", "VOID_FOG");
		config.set("level.10", "WATERDRIP");
		config.set("level.15", "HEART");
		config.set("level.20", "NOTE");
		config.set("level.25", "LAVADRIP");
		config.set("level.30", "FLAME");
		config.set("level.35", "FIREWORKS_SPARK");
		config.set("level.40", "PORTAL");
		config.set("level.45", "SMOKE");
		config.set("level.50", "COLOURED_DUST");

		return config;
	}

	/**
	 * returns the field specified by the given key Colors in the pattern
	 * {COLOR} are converted to the ChatColor equivalent. e.g.: {RED} is
	 * replaced by ChatColor.RED
	 * 
	 * @param string
	 * @return
	 */
	public String getTextNode(String key) {
		String ret = (String) this.getConfig().get("texts." + key);
		if (ret != null && !ret.isEmpty()) {
			for (int i = 0; i < ChatColor.values().length; i++) {
				// the enum contains no element called "ChatColor.OBFUSCATED",
				// but ChatColor.values()[] does !?
				if (!ChatColor.values()[i].getName().equalsIgnoreCase(
						"obfuscated")) {
					String replace = "{"
							+ ChatColor.values()[i].getName().toUpperCase()
							+ "}";
					ret = ret.replace(
							replace,
							ChatColor.valueOf(
									ChatColor.values()[i].getName()
											.toUpperCase()).toString());
				}
			}
			return ret;
		} else {
			return "Missing text node: " + key;
		}
	}

	/**
	 * @return the chatNotificationTask
	 */
	public ChatNotificationTask getChatNotificationTask() {
		return chatNotificationTask;
	}

	/**
	 * looks the given string up in the config and returns the matching Effect
	 * 
	 * @param effect
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseEffectString(String effect) {
		if (effect == null) {
			return null;
		}
		for (String key : ((Map<String, String>) MbKarmaBungee.getInstance()
				.getConfig().get("effects")).keySet()) {
			if (MbKarmaBungee.getInstance().getConfig()
					.getStringList("effects." + key + ".aliases")
					.contains(effect)) {
				return key;
			}
		}
		return null;
	}
}