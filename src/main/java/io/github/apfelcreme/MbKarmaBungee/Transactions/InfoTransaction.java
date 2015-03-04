package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class InfoTransaction extends Transaction {

	private UUID targetPlayer;
	private String targetPlayerName;
	
	public InfoTransaction(ProxiedPlayer sender, UUID targetPlayer, String targetPlayerName) {
		super(sender);
		this.targetPlayer = targetPlayer;
		this.targetPlayerName = targetPlayerName;
	}

	/**
	 * shows info of the given player
	 */
	@Override
	public void send() {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();

		final String permission = "MbKarma.info";

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
		if (targetPlayer == null) {
			targetPlayer = sender.getUniqueId();
			targetPlayerName = sender.getName();
		}
		MbKarmaBungee.getInstance().getProxy().getScheduler()
				.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

					@Override
					public void run() {

						ResultSet res = null;
						PreparedStatement statement;

						try {
							statement = connection
									.prepareStatement("SELECT currentAmount from MbKarma_Player where uuid = ?;");
							statement.setString(1, targetPlayer.toString());
							res = statement.executeQuery();
							double currentAmount = 0;
							if (res.first()) {
								currentAmount = res.getDouble("currentAmount");
							} else {
								sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
										.getTextNode("info.noInfo")).create());
								return;
							}

							sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
									.getTextNode("info.info")
									.replace("{0}", targetPlayerName)
									.replace("{1}",
											Double.toString(currentAmount)))
									.create());
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
