package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ParticleSetTransaction extends Transaction {

	private String effect;

	public ParticleSetTransaction(ProxiedPlayer sender, String effect) {
		super(sender);
		this.effect = effect;
	}

	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();

		final String permission = "MbKarma.setParticlesManually";

		// check mandatory Parameters and stuff
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noPermission")).create());
			return;
		}
		if (connection == null) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noDbConnection")).create());
			return;
		}
		MbKarmaBungee.getInstance().getProxy().getScheduler()
				.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

					@Override
					public void run() {

						PreparedStatement statement;

						try {
							statement = connection
									.prepareStatement("UPDATE MbKarma_Player SET effect = ? where uuid = ?;");
							statement.setString(1, effect);
							statement.setString(2, sender.getUniqueId()
									.toString());
							statement.executeUpdate();
							MbKarmaBungee
									.getInstance()
									.sendEffectChangeMessage(
											sender,
											effect,
											MbKarmaBungee
													.getInstance()
													.getConfig()
													.getLong(
															"effects." + effect
																	+ ".delay"),
											true);
							sender.sendMessage(new ComponentBuilder(
									MbKarmaBungee.getInstance()
											.getTextNode(
													"info.particlesChanged"))
									.create());
							MbKarmaBungee
									.getInstance()
									.getLogger()
									.info("Karma-Transaktion - SETPARTICLES : "
											+ sender.getName()
											+ "("
											+ sender.getUniqueId()
													.toString()
											+ ") --> " + "effect");
							connection.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});

	}

	/**
	 * @return the effect
	 */
	public String getEffect() {
		return effect;
	}

	/**
	 * @param effect
	 *            the effect to set
	 */
	public void setEffect(String effect) {
		this.effect = effect;
	}

}
