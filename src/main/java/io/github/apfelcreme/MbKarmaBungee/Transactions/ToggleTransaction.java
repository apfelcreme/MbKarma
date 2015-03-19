package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ToggleTransaction extends Transaction {

	public ToggleTransaction(ProxiedPlayer sender) {
		super(sender);
	}

	@Override
	public void send() throws SQLException {

		final Connection connection = DatabaseConnectionManager.getInstance()
				.getConnection();
		final String permission = "MbKarma.particles";
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
						ResultSet res;
						boolean toSet = true;

						try {
							statement = connection
									.prepareStatement("SELECT seesParticles, effect FROM MbKarma_Player where uuid = ?");
							statement.setString(1, sender.getUniqueId()
									.toString());
							res = statement.executeQuery();						
							
							if (res.first()) {
								boolean seesParticles = res.getBoolean("seesParticles");
								String effect = res.getString("effect");
								long effectDelay =  MbKarmaBungee.getInstance().getConfig().getLong("effects."+effect+".delay");
								toSet = !seesParticles;
								// Update again
								statement = connection
										.prepareStatement("UPDATE MbKarma_Player SET seesParticles = ? where uuid = ?");
								statement.setBoolean(1, toSet);
								statement.setString(2, sender.getUniqueId()
										.toString());
								statement.executeUpdate();
								sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
										.getTextNode("info.toggle").replace(
												"{0}",
												toSet ? "sichtbar"
														: "unsichtbar"))
										.create());
								MbKarmaBungee.getInstance().
									sendParticleToggleMessage(sender, effect, effectDelay, toSet);
								
								statement.close();
								
								MbKarmaBungee.getInstance().getLogger().info(
										"Karma-Transaktion - TOGGLE : "
												+ sender.getName() + "("
												+ sender.getUniqueId().toString()
												+ ") - " + (!toSet ? "sichtbar"
														: "unsichtbar") +" --> "+(toSet ? "sichtbar"
																: "unsichtbar"));
							} else {
								sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
										.getTextNode("error.unknownPlayerToggle")).create());
							}

						} catch (SQLException e) {
							e.printStackTrace();
						}

					}
				});
	}

}
