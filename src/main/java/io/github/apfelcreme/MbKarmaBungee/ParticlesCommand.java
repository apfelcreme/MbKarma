package io.github.apfelcreme.MbKarmaBungee;

import java.sql.SQLException;

import io.github.apfelcreme.MbKarmaBungee.Transactions.ParticleListTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ParticleSelectTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ParticleSetTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.ToggleTransaction;
import io.github.apfelcreme.MbKarmaBungee.Transactions.Transaction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ParticlesCommand extends Command {

	public ParticlesCommand(String name) {
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
	 * 
	 * @param sender
	 * @param args
	 * @return
	 */
	private Transaction getTransaction(CommandSender sender, String[] args) {
		Transaction transaction = null;
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (args.length == 0) {
			transaction = new ParticleListTransaction(player);
		} else {
			ParticlesOperation basicOperation = ParticlesOperation.getOperation(args[0]);
			if (basicOperation != null) {
				switch (basicOperation) {
				case SET:
					transaction = createParticleSetTransaction(args, player);
					break;
				case TOGGLE:
					transaction = new ToggleTransaction(player);
					break;
				default:
					break;
				}
			} else {
				transaction = createParticleSelectTransaction(args, player);
			}		
		}
		return transaction;
	}
	
	/**
	 * creates a {@link ParticleSetTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private Transaction createParticleSetTransaction(String[] args, ProxiedPlayer player) {
		if (args.length < 2 || args[1] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		String effect = args[1].toUpperCase();
		if (MbKarmaBungee.parseEffectString(effect) != null) {
			if (Transaction
					.getEffectLevel(MbKarmaBungee.parseEffectString(effect)) != -1) {
				return new ParticleSetTransaction(player,
						MbKarmaBungee.parseEffectString(effect));
			}
		} else {
			player.sendMessage(new ComponentBuilder(
					MbKarmaBungee.getInstance().getTextNode(
							"error.unknownEffect")).create());
		}
		return null;
	}

	/**
	 * creates a {@link ParticleSelectTransaction}
	 * @param args
	 * @param player
	 * @return
	 */
	private Transaction createParticleSelectTransaction(String[] args, ProxiedPlayer player) {		
		if (args.length < 1 || args[0] == null) {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.missingParameters")).create());
			return null;
		}
		String effect = args[0].toUpperCase();
		if (MbKarmaBungee.parseEffectString(effect) != null) {
			if (Transaction.getEffectLevel(MbKarmaBungee.parseEffectString(effect)) != -1) {
				return new ParticleSelectTransaction(player,
						MbKarmaBungee.parseEffectString(effect));
			} else {
				player.sendMessage(new ComponentBuilder(MbKarmaBungee
						.getInstance().getTextNode(
								"error.unknownEffect")).create());
			}
		} else {
			player.sendMessage(new ComponentBuilder(MbKarmaBungee
					.getInstance().getTextNode("error.unknownEffect"))
					.create());
		}
		return null;
	}

	
	public enum ParticlesOperation {
		SET, LIST, TOGGLE;

		public static ParticlesOperation getOperation(String operation) {
			for (ParticlesOperation op: ParticlesOperation.values()) {
				if (op.toString().equalsIgnoreCase(operation)) {
					return op;
				}
			}
			return null;
		}
		
	}
}
