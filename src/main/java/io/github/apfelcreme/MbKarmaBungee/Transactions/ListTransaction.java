package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ListTransaction extends Transaction {

	private UUID targetPlayer;
	private String targetPlayerName;
	
	public ListTransaction(ProxiedPlayer sender, UUID targetPlayer, String targetPlayerName) {
		super(sender);
		this.targetPlayer = targetPlayer;
		this.targetPlayerName = targetPlayerName;
	}

	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();
		final String permission = "MbKarma.list";

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
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.unknownPlayer")).create());
			return;
		}
		MbKarmaBungee.getInstance().getProxy().getScheduler()
				.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

					@Override
					public void run() {
						try {
							ResultSet res = null;
							PreparedStatement statement;
							statement = connection
									.prepareStatement(""
											+ "Select t.playername, "
											+ "t.currentAmount, "
											+ "r.timestamp, "
											+ "r.relationratio, "
											+ "r.relationamount, "
											+ "(Select "
												+ "relationAmount from MbKarma_Relations "
												+ "where playerId = r.targetid "
												+ "and targetId = r.playerid"
												+ ") as receivedFromTarget "
											+ "from MbKarma_Relations r inner join MbKarma_Player p on r.playerid = p.playerid "
											+ "inner join MbKarma_Player t on r.targetid = t.playerid "
											+ "where p.uuid = ?");
							statement.setString(1, targetPlayer.toString());
							res = statement.executeQuery();
							sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
									.getTextNode("info.listHead").replace("{0}",
											targetPlayerName)).create());
							res.beforeFirst();
							while (res.next()) {
								sender.sendMessage(new ComponentBuilder(
										MbKarmaBungee.getInstance().getTextNode("info.list")
												.replace(
														"{0}",
														res.getString("playername"))
												.replace(
														"{1}",
														new DecimalFormat(
																"#.###").format(res
																.getDouble("relationratio")))
												.replace(
														"{2}",
														new DecimalFormat(
																"#.##").format(res
																.getDouble("relationamount")))
												.replace(
														"{3}",
														new DecimalFormat(
																"#.###").format(res
																.getDouble("receivedFromTarget")))
												.replace(
														"{4}",
														new SimpleDateFormat(
																"dd.MM.yy HH:mm",
																Locale.GERMAN)
																.format(new java.util.Date(
																		res.getLong("timestamp")))))
										.create());
							}
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
