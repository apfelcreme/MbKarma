package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ParticleListTransaction extends Transaction {

	public ParticleListTransaction(ProxiedPlayer sender) {
		super(sender);
	}

	@Override
	public void send() throws SQLException {
		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();

		final String permission = "MbKarma.particles";

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

					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						ResultSet res = null;
						PreparedStatement statement;

						try {
							statement = connection
									.prepareStatement("Select * from MbKarma_Player where uuid = ?;");
							statement.setString(1, sender.getUniqueId()
									.toString());
							res = statement.executeQuery();
							sender.sendMessage(new ComponentBuilder(
									MbKarmaBungee
											.getInstance()
											.getTextNode(
													"info.particlesListHead"))
									.create());
							if (res.first()) {		
								double currentAmount = res.getDouble("currentAmount");
								for (Entry<String, String> entry : ((Map<String, String>)MbKarmaBungee.getInstance().getConfig().get("level")).entrySet()) {
									int effectLevel = getEffectLevel(entry.getValue());
									String effectDisplayText = MbKarmaBungee.getInstance().getConfig().getString("effects."+entry.getValue()+".displaytext");
									if (effectLevel <= currentAmount) {
										sender.sendMessage(new ComponentBuilder(
												MbKarmaBungee.getInstance().getTextNode(
														"info.particlesListElementAvailable").replace("{0}", effectDisplayText).replace("{1}", entry.getKey())).create());
									} else {
										sender.sendMessage(new ComponentBuilder(
												MbKarmaBungee.getInstance().getTextNode(
														"info.particlesListElementUnavailable").replace("{0}", effectDisplayText).replace("{1}", entry.getKey())).create());
									}
								}
							} else {
								for (Entry<String, String> entry : ((Map<String, String>)MbKarmaBungee.getInstance().getConfig().get("level")).entrySet()) {
									String effectDisplayText = MbKarmaBungee.getInstance().getConfig().getString("effects."+entry.getValue()+".displaytext");
									sender.sendMessage(new ComponentBuilder(
											MbKarmaBungee.getInstance().getTextNode(
													"info.particlesListElementUnavailable").replace("{0}", effectDisplayText).replace("{1}", entry.getKey())).create());
								}
								
							}
							sender.sendMessage(new ComponentBuilder(
									MbKarmaBungee
											.getInstance()
											.getTextNode(
													"info.particlesListBottom"))
									.create());
							connection.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});

	}
	
}
