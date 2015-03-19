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

public class GiveTransaction extends Transaction {
	
	private UUID targetPlayer;
	private String targetPlayerName;

	public GiveTransaction(ProxiedPlayer sender, UUID targetPlayer, String targetPlayerName) {
		super(sender);
		this.targetPlayer = targetPlayer;
		this.targetPlayerName = targetPlayerName;
	}
	

	/**
	 * gives karma to a player
	 * 
	 * @return
	 * @throws SQLException
	 */
	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();

		final int MSPERDAY = 86400000;
		final String permission = "MbKarma.give";

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
		if (MbKarmaBungee.getInstance().getProxy().getPlayer(targetPlayer) == null) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.playerOffline")).create());
			return;
		}
		if (sender.getUniqueId().equals(targetPlayer)) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("info.yourself")).create());
			return;
		}
		
		targetPlayerName = MbKarmaBungee.getInstance().getProxy().getPlayer(targetPlayer).getDisplayName();

		MbKarmaBungee.getInstance().getProxy().getScheduler()
				.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

					@Override
					public void run() {
						PreparedStatement statement;
						ResultSet res = null;

						double currentAmount = 0.0;
						double currentRatio = 0.0;
						double currentRelationAmount = 0.0;

						try {

							// check if one day is gone since the last
							// transaction
							statement = connection
									.prepareStatement("SELECT timestamp "
											+ "FROM MbKarma_Relations "
											+ "WHERE playerid = "
											+ "(SELECT playerid FROM MbKarma_Player WHERE uuid = ?) "
											+ "AND targetid = "
											+ "(SELECT playerid FROM MbKarma_Player WHERE uuid = ?);");
							statement.setString(1, sender.getUniqueId()
									.toString());
							statement.setString(2, targetPlayer.toString());
							res = statement.executeQuery();
							long currentTime = System.currentTimeMillis();
							long lastTransactiomTime = !res.first() ? 0 : res
									.getLong("timestamp");
							if ((currentTime >= (lastTransactiomTime + MSPERDAY))
									|| !res.first()) {
								// enter giving Player if he does not exist yet
								statement = connection
										.prepareStatement("INSERT INTO MbKarma_Player (playername, uuid, currentamount, seesParticles) "
												+ "Select d.* from (select ? as playername, ? as uuid, 0 as currentamount, 0 as seesParticles) as d "
												+ "where 0 in (select count(*) from MbKarma_Player where uuid = ? );");
								statement.setString(1, sender.getName());
								statement.setString(2, sender.getUniqueId()
										.toString());
								statement.setString(3, sender.getUniqueId()
										.toString());
								statement.executeUpdate();
								statement.close();

								// enter target Player if he does not exist yet
								statement = connection
										.prepareStatement("INSERT INTO MbKarma_Player (playername, uuid, currentamount, seesParticles) "
												+ "Select d.* from (select ? as playername, ? as uuid, 0 as currentamount, 0 as seesParticles) as d "
												+ "where 0 in (select count(*) from MbKarma_Player where uuid = ? );");
								statement.setString(1, targetPlayerName);
								statement.setString(2, targetPlayer.toString());
								statement.setString(3, targetPlayer.toString());
								statement.executeUpdate();
								statement.close();

								// enter a new relation if it does not exist yet
								statement = connection
										.prepareStatement("INSERT IGNORE INTO MbKarma_Relations("
												+ "playerid, targetid, relationRatio, relationAmount, timesGiven, timestamp) VALUES("
												+ "(SELECT playerid from MbKarma_Player where uuid = ? ), "
												+ "(SELECT playerid from MbKarma_Player where uuid = ? ),"
												+ "1, 0, 0, 0);");
								statement.setString(1, sender.getUniqueId()
										.toString());
								statement.setString(2, targetPlayer.toString());
								statement.executeUpdate();
								statement.close();

								// get target's current amount of karma, ratio
								// and times given
								statement = connection
										.prepareStatement("SELECT p1.playername as giver, "
												+ "p2.playername as target, "
												+ "p2.currentAmount as currentAmountTarget, "
												+ "MbKarma_Relations.relationratio, MbKarma_Relations.timesGiven, MbKarma_Relations.relationAmount "
												+ "from MbKarma_Relations "
												+ "left join MbKarma_Player p1 on p1.playerid = MbKarma_Relations.playerid "
												+ "left join MbKarma_Player p2 on p2.playerid = MbKarma_Relations.targetid "
												+ "where p1.uuid = ? and p2.uuid = ?;");
								statement.setString(1, sender.getUniqueId()
										.toString());
								statement.setString(2, targetPlayer.toString());
								res = statement.executeQuery();

								res.first();
								currentAmount = res
										.getDouble("currentAmountTarget");
								currentRatio = res.getDouble("relationratio");
								currentRelationAmount = res
										.getDouble("relationAmount");
								int timesGiven = res.getInt("timesGiven");
								statement.close();
								// adjust the ratio = 0.4e^(-0.6x)
								currentRatio = Math.pow(0.4 * Math.E, -1
										* timesGiven);

								// insert transaction
								statement = connection
										.prepareStatement("INSERT INTO MbKarma_Transactions (giverid, targetid, amount) "
												+ "VALUES ("
												+ "(SELECT playerid from MbKarma_Player where uuid = ?),"
												+ "(SELECT playerid from MbKarma_Player where uuid = ?), ?);");
								statement.setString(1, sender.getUniqueId()
										.toString());
								statement.setString(2, targetPlayer.toString());
								statement.setDouble(3, currentRatio);
								statement.executeUpdate();
								statement.close();

								// update the Relations
								statement = connection
										.prepareStatement("UPDATE MbKarma_Relations SET "
												+ "relationRatio = ?, "
												+ "timestamp = ?, "
												+ "relationAmount = ?, "
												+ "timesGiven = ? "
												+ "where playerid = (SELECT playerid from MbKarma_Player where uuid = ?) "
												+ "and targetid = (SELECT playerid from MbKarma_Player where uuid = ?)");
								statement.setDouble(1, currentRatio);
								statement.setLong(2, currentTime);
								statement.setDouble(3, currentRelationAmount
										+ currentRatio);
								statement.setInt(4, timesGiven + 1);
								statement.setString(5, sender.getUniqueId()
										.toString());
								statement.setString(6, targetPlayer.toString());
								statement.executeUpdate();
								statement.close();

								// get the target player's playerid as mysql
								// isnt able to update a
								// table when selecting the key for the where
								// clause from the same
								// table
								statement = connection
										.prepareStatement("Select playerid from MbKarma_Player "
												+ "where playerid = (SELECT playerid FROM MbKarma_Player where uuid = ?)");
								statement.setString(1, targetPlayer.toString());
								res = statement.executeQuery();

								res.first();
								
								// update the player's current amount of karma
								statement = connection
										.prepareStatement("UPDATE MbKarma_Player SET currentAmount = ? where playerid = ?;");
								statement.setDouble(1, currentAmount
										+ currentRatio);
								statement.setInt(2, res.getInt("playerid"));
								statement.executeUpdate();
								statement.close();

								// done
								String broadcastMessage = MbKarmaBungee.getInstance()
										.getTextNode("broadcast.transaction")
										.replace("{0}", sender.getName())
										.replace("{1}", targetPlayerName)
										.replace(
												"{2}",
												String.format("%.2f",
														(currentRatio)))
										.replace(
												"{3}",
												String.format(
														"%.2f",
														(currentAmount + currentRatio)));

								// send broadcast only every time the user
								// reaches a new multiple of
								// the step. (e.g. step= 10, then from 9-10,
								// 19-20, 29-30, ...), or
								// the current amount of karma is less than a
								// predefined value
  								// "stepstart"
//								if ((currentAmount + currentRatio)
//										% MbKarmaBungee.getInstance().getConfig().getInt(
//												"broadcast.step", 1) < 1
//										|| (currentAmount + currentRatio) <= MbKarmaBungee.getInstance()
//												.getConfig().getInt(
//														"broadcast.stepstart",
//														1)) {
								MbKarmaBungee.getInstance().sendBroadcast(broadcastMessage);
//								}
								MbKarmaBungee.getInstance().getLogger()
										.info("Karma-Transaktion - GIVE : "
												+ sender.getName()
												+ "("
												+ sender.getUniqueId()
														.toString()
												+ ") --"
												+ (currentAmount + currentRatio)
												+ "--> "
												+ MbKarmaBungee.getInstance().getProxy().getPlayer(
														targetPlayer).getName() + "("
												+ targetPlayer.toString()+")");
							} else {
								sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
										.getTextNode("info.oncePerDay")
										.replace("{0}", targetPlayerName))
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
