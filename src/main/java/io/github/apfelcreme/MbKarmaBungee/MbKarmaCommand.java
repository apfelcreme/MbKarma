package io.github.apfelcreme.MbKarmaBungee;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import io.github.apfelcreme.MbKarmaBungee.Transactions.GiveTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.InfoTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ResetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.SetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.TopListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.Transaction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.zaiyers.UUIDDB.bungee.UUIDDB;

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
		BasicOperation basicOperation = BasicOperation.getOperation(args[0]);
		if (basicOperation != null) {
			switch (basicOperation) {
			case GIVE:
				transaction = createGiveTransaction(args, player);
				break;
			case INFO:
				transaction = createInfoTransaction(args, player);
				break;
			case LIST:
				transaction = createListTransaction(args, player);
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
		targetUuid = UUIDDB.getInstance().getStorage()
				.getUUIDByName(args[1], false);
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
			targetUuid = UUIDDB.getInstance().getStorage()
					.getUUIDByName(args[1], false);
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
			targetUuid = UUIDDB.getInstance().getStorage()
					.getUUIDByName(args[1], false);
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
		targetUuid = UUIDDB.getInstance().getStorage()
				.getUUIDByName(args[1], false);
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
		targetUuid = UUIDDB.getInstance().getStorage()
				.getUUIDByName(args[1], false);
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

	public enum BasicOperation {
		GIVE, LIST, RESET, INFO, TOP, SET, RELOAD, REGENERATE;

		public static BasicOperation getOperation(String operation) {
			for (BasicOperation op: BasicOperation.values()) {
				if (op.toString().equalsIgnoreCase(operation)) {
					return op;
				}
			}
			return null;
		}
		
	}

}
