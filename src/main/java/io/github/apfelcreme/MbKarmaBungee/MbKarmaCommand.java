package io.github.apfelcreme.MbKarmaBungee;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import io.github.apfelcreme.MbKarmaBungee.Transactions.GiveTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.InfoTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ParticleListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ParticleSetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ResetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.SetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ToggleTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.TopListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.Transaction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class MbKarmaCommand extends Command {

	public MbKarmaCommand(String name) {
		super(name);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			Transaction transaction = getTransaction(sender, args);
			if (transaction != null) {
				try {
					transaction.send();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * returns a {@link Transaction}
	 * @param sender
	 * @param args
	 * @return
	 */
	private Transaction getTransaction(CommandSender sender, String[] args) {
		Transaction transaction = null;
		ProxiedPlayer player = (ProxiedPlayer)sender;
		if (args.length == 0) {
			if (sender.hasPermission("MbKarma.list")) {
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.give")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.info")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.list")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.particlesList")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.particlesSet")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.regenerate")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.reload")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.reset")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.set")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.toggle")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.top")).create());
			} else {
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.give")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.info")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.particlesList")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.particlesSet")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.toggle")).create());
				sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance().getTextNode("help.top")).create());
			}
			return null;
		}
		Operation operation = Operation.getOperation(args[0]);
		if (operation != null) {
			switch (operation) {
			case GIVE:
				transaction = createGiveTransaction(args, player);
				break;
			case INFO:
				transaction = createInfoTransaction(args, player);
				break;
			case LIST:
				transaction = createListTransaction(args, player);
				break;
			case PARTICLES:
				transaction = createParticleTransaction(args, player);
				break;
			case REGENERATE:
				regenerateConfig(sender);
				break;
			case RELOAD:
				reloadConfig(sender);
				break;
			case RESET:
				transaction = createResetTransaction(args, player);
				break;
			case SET:
				transaction = createSetTransaction(args, player);
				break;
			case TOGGLE:
				transaction = new ToggleTransaction((ProxiedPlayer)sender);
				break;
			case TOP:
				transaction = new TopListTransaction((ProxiedPlayer)sender);
				break;
			default:
				break;
			}
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownFunction").replace("{0}", args[0])).create());
		}		
		return transaction;
	}

	/**
	 * creates a new {@link GiveTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private GiveTransaction createGiveTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length < 2 || args[1] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
				.getUUIDByName(args[1]);
		if (targetUuid != null) {
			return new GiveTransaction(player,
					UUID.fromString(targetUuid), args[1]);
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownPlayer")).create());
		}
		return null;
	}
	
	/**
	 * creates a new {@link InfoTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private InfoTransaction createInfoTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length == 1) {
			//player wants to see his own list
			return new InfoTransaction(player,
					player.getUniqueId(), player.getName());
		} else {
			targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
					.getUUIDByName(args[1]);
			if (targetUuid != null) {
				return new InfoTransaction(player,
						UUID.fromString(targetUuid), args[1]);
			} else {
				player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
						.getTextNode("error.unknownPlayer")).create());
			}
		}
		return null;
	}
	
	/**
	 * creates a new {@link ListTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private ListTransaction createListTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length == 1) {
			//player wants to see his own list
			 return new ListTransaction(player,
					player.getUniqueId(), player.getName());
		} else {
			targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
					.getUUIDByName(args[1]);
			if (targetUuid != null) {
				return new ListTransaction(player,
						UUID.fromString(targetUuid), args[1]);
			} else {
				player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
						.getTextNode("error.unknownPlayer")).create());
			}
		}
		return null;
	}
	
	/**
	 * creates a new {@link Transaction}, either a {@link ParticleListTransaction} or a {@link ParticleSetTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private Transaction createParticleTransaction(String[] args,
			ProxiedPlayer player) {
		if (args.length == 1) {
			//player wants to see a list of his selectable Effects
			return new ParticleListTransaction(player);
		} else {
			if (args[1] != null) {
				String effect = args[1].toUpperCase();
				if (parseEffectString(effect) != null) {
					if (Transaction.getEffectLevel(parseEffectString(effect)) != -1) {
						return new ParticleSetTransaction(player, parseEffectString(effect));
					} else {
						player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
								.getTextNode("error.unknownEffect")).create());
					}
				} else {
					player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
							.getTextNode("error.unknownEffect")).create());					
				}
			}
		}
		return null;
	}

	/**
	 * creates a new {@link ResetTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private ResetTransaction createResetTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length < 2 || args[1] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
				.getUUIDByName(args[1]);
		if (targetUuid != null) {
			return new ResetTransaction(player,
					UUID.fromString(targetUuid), args[1]);
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownPlayer")).create());
		}
		return null;
	}
	
	/**
	 * creates a new {@link SetTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private SetTransaction createSetTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length < 3 || args[2] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		double amountToSet = 0;
		if (NumberUtils.isNumber(args[2])) {
			amountToSet = Double.parseDouble(args[2]);
		}
		targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
				.getUUIDByName(args[1]);
		if (targetUuid != null) {
			return new SetTransaction(player,
					UUID.fromString(targetUuid), args[1], amountToSet);
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownPlayer")).create());
		}
		return null;
	}
	

	/**
	 * reloads the config
	 * 
	 * @param commandSender
	 * @throws IOException
	 */
	private void reloadConfig(CommandSender sender) {
		final String permission = "MbKarma.reload";
		// check mandatory Parameters and stuff
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noPermission")).create());
			return;
		}
		try {
			MbKarmaBungee.getInstance().setConfig(null);
			MbKarmaBungee.getInstance().loadConfig();
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("info.configReloaded")).create());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * regenerates the config
	 * 
	 * @param sender
	 * @throws IOException
	 */
	private void regenerateConfig(CommandSender sender) {

		final String permission = "MbKarma.regenerate";
		// check mandatory Parameters and stuff
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noPermission")).create());
			return;
		}
		String dbuser = MbKarmaBungee.getInstance().getConfig()
				.getString("mysql.dbuser", "");
		String dbpassword = MbKarmaBungee.getInstance().getConfig()
				.getString("mysql.dbpassword", "");
		String database = MbKarmaBungee.getInstance().getConfig()
				.getString("mysql.database", "");
		String url = MbKarmaBungee.getInstance().getConfig()
				.getString("mysql.url", "");
		try {
			File configFile = new File(MbKarmaBungee.getInstance()
					.getDataFolder().getAbsoluteFile()
					+ "/config.yml");
			if (new File(MbKarmaBungee.getInstance().getDataFolder()
					.getAbsoluteFile()
					+ "/configOld.yml").exists()) {
				new File(MbKarmaBungee.getInstance().getDataFolder()
						.getAbsoluteFile()
						+ "/configOld.yml").delete();
			}
			configFile.renameTo(new File(MbKarmaBungee.getInstance()
					.getDataFolder().getAbsoluteFile()
					+ "/configOld.yml"));
			MbKarmaBungee.getInstance().setConfig(null);
			MbKarmaBungee.getInstance().loadConfig();
			MbKarmaBungee.getInstance().getConfig().set("mysql.dbuser", dbuser);
			MbKarmaBungee.getInstance().getConfig()
					.set("mysql.dbpassword", dbpassword);
			MbKarmaBungee.getInstance().getConfig()
					.set("mysql.database", database);
			MbKarmaBungee.getInstance().getConfig().set("mysql.url", url);
			MbKarmaBungee.getInstance().saveConfig();
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("info.configRegenerated")).create());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * looks the given string up in the config and returns the matching Effect	
	 * @param effect
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String parseEffectString(String effect) {
		if (effect == null) {
			return null;
		}
		for (String key : ((Map<String, String>)MbKarmaBungee.getInstance().getConfig().get("effects")).keySet()) {
			if (MbKarmaBungee.getInstance().getConfig()
					.getStringList("effects." + key + ".aliases")
					.contains(effect)) {
				return key;
			}
		}
		return null;
	}

	public enum Operation {
		GIVE, LIST, RESET, INFO, TOP, SET, RELOAD, REGENERATE, TOGGLE, PARTICLES;

		public static Operation getOperation(String operation) {
			for (Operation op: Operation.values()) {
				if (op.toString().equalsIgnoreCase(operation)) {
					return op;
				}
			}
			return null;
		}
		
	}

}
