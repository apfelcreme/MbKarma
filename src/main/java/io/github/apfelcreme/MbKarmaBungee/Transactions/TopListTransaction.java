package io.github.apfelcreme.MbKarmaBungee.Transactions;


import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TopListTransaction extends Transaction {

	public TopListTransaction(ProxiedPlayer sender) {
		super(sender);
	}

	@Override
	public void send() throws SQLException {
		final Connection con = DatabaseConnectionManager.getInstance()
				.getConnection();

		final String permission = "MbKarma.top";

		// check mandatory Parameters and stuff
		if (!sender.hasPermission(permission)) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noPermission")).create());
			return;
		}
		if (con == null) {
			sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
					.getTextNode("error.noDbConnection")).create());
			return;
		}
		MbKarmaBungee.getInstance().getProxy().getScheduler()
				.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

					@Override
					public void run() {
						ResultSet res = null;
						PreparedStatement statement;
						try {
							statement = con
									.prepareStatement("Select playername, currentAmount from MbKarma_Player order by currentAmount desc limit 0, 10");
							res = statement.executeQuery();
							sender.sendMessage(new ComponentBuilder(MbKarmaBungee.getInstance()
									.getTextNode("info.topListHead")).create());
							res.beforeFirst();
							while (res.next()) {
								sender.sendMessage(new ComponentBuilder(
										MbKarmaBungee.getInstance().getTextNode("info.topList")
												.replace(
														"{0}",
														res.getString("playername"))
												.replace(
														"{1}",
														String.format(
																"%.2f",
																res.getDouble("currentAmount"))))
										.create());

							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});
	}

}
