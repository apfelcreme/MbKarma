package io.github.apfelcreme.MbKarmaBungee;

import io.github.apfelcreme.MbKarmaBungee.Transactions.GiveTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.Transaction;

import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SimpleThanksCommand extends Command{

	public SimpleThanksCommand(String name) {
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
	 * returns a {@link GiveTransaction}
	 * @param sender
	 * @param args
	 * @return
	 */
	private Transaction getTransaction(CommandSender sender, String[] args) {
		ProxiedPlayer player = (ProxiedPlayer)sender;		
		return createGiveTransaction(args, player);
	}

	/**
	 * creates a new {@link GiveTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private GiveTransaction createGiveTransaction(String[] args, ProxiedPlayer player) {
		String targetUuid;
		if (args.length < 1 || args[0] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		targetUuid = MbKarmaBungee.getInstance().getPluginUuiddb()
				.getUUIDByName(args[0]);
		if (targetUuid != null) {
			return new GiveTransaction(player,
					UUID.fromString(targetUuid), args[0]);
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownPlayer")).create());
		}
		return null;
	}

}
