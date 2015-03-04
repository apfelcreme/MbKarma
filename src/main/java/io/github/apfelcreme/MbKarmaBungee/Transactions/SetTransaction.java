package io.github.apfelcreme.MbKarmaBungee.Transactions;


import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SetTransaction extends Transaction {

	private UUID targetPlayer;
	private String targetPlayerName;
	private double amountToSet;
	
	public SetTransaction(ProxiedPlayer sender, UUID targetPlayer, String targetPlayerName, double amountToSet) {
		super(sender);
		this.targetPlayer = targetPlayer;
		this.targetPlayerName = targetPlayerName;
		this.amountToSet = amountToSet;
	}

	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();
		final String permission = "MbKarma.set";

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
							//insert a player if he does not exist yet
							statement = connection
									.prepareStatement("INSERT INTO MbKarma_Player (playername, uuid, currentamount, seesParticles) "
											+ "Select d.* from (select ? as playername, ? as uuid, 0 as currentamount, 1 as seesParticles) as d "
											+ "where 0 in (select count(*) from MbKarma_Player where uuid = ? );");
							statement.setString(1, targetPlayerName);
							statement.setString(2, targetPlayer.toString());
							statement.setString(3, targetPlayer.toString());
							statement.executeUpdate();
							statement.close();

							//update the karma amount
							statement = connection
									.prepareStatement("UPDATE MbKarma_Player SET currentAmount = ? where uuid = ?");
							statement.setDouble(1, amountToSet);
							statement.setString(2, targetPlayer.toString());
							statement.executeUpdate();
							sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
									.getTextNode("info.set")
									.replace("{0}", targetPlayerName)
									.replace("{1}",
											String.format("%.2f", amountToSet)))
									.create());

							//TODO
							MbKarmaBungee.getInstance().getLogger().info(
									"Karma-Transaktion - SET : "
											+ targetPlayer.toString() + " = "
											+ amountToSet + " "
											+ "- ausgeführt von "
											+ sender.getName() + "("
											+ sender.getUniqueId().toString()
											+ ")");
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
