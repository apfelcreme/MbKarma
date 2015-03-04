package io.github.apfelcreme.MbKarmaBungee.Transactions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

public class ResetTransaction extends Transaction {

	private UUID targetPlayer;
	private String targetPlayerName;
	
	public ResetTransaction(ProxiedPlayer sender, UUID targetPlayer, String targetPlayerName) {
		super(sender);
		this.targetPlayer = targetPlayer;
		this.targetPlayerName = targetPlayerName;
	}

	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();
		final String permission = "MbKarma.reset";

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
									.prepareStatement("UPDATE MbKarma_Player SET currentAmount = 0 where uuid = ?");
							statement.setString(1, targetPlayer.toString());
							statement.executeUpdate();
							sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
									.getTextNode("info.reset").replace("{0}",
											targetPlayerName)).create());
							MbKarmaBungee.getInstance().getLogger().info(
									"Karma-Transaktion - RESET : "											
											+ targetPlayer.toString()
											+ " - ausgeführt von "
											+ sender.getName() + "("
											+ sender.getUniqueId().toString()
											+ ")");
							connection.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});

	}

	/**
	 * @return the targetPlayer
	 */
	public UUID getTargetPlayer() {
		return targetPlayer;
	}

	/**
	 * @param targetPlayer the targetPlayer to set
	 */
	public void setTargetPlayer(UUID targetPlayer) {
		this.targetPlayer = targetPlayer;
	}

	/**
	 * @return the targetPlayerName
	 */
	public String getTargetPlayerName() {
		return targetPlayerName;
	}

	/**
	 * @param targetPlayerName the targetPlayerName to set
	 */
	public void setTargetPlayerName(String targetPlayerName) {
		this.targetPlayerName = targetPlayerName;
	}

}
