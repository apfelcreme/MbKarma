package io.github.apfelcreme.MbKarmaBungee.Listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BukkitMessageListener implements Listener {

	@EventHandler
	public void onPluginMessageReceived(PluginMessageEvent e)
			throws IOException {
		if (!e.getTag().equals("Karma")) {
			return;
		}
		if (!(e.getSender() instanceof Server)) {
			return;
		}
		ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
		DataInputStream in = new DataInputStream(stream);

		final String channel = in.readUTF();
		final String playerUUID = in.readUTF();
		final Boolean isVisible = in.readBoolean();

		System.out.println(channel + "/" + playerUUID + "/" + isVisible);

		if (channel.equalsIgnoreCase("PlayerVanished")) {

			MbKarmaBungee.getInstance().getProxy().getScheduler()
					.runAsync(MbKarmaBungee.getInstance(), new Runnable() {

						Connection connection = DatabaseConnectionManager
								.getInstance().getConnection();
						PreparedStatement statement;
						ResultSet res;
						
						@Override
						public void run() {

							try {
								statement = connection
										.prepareStatement("Select * from MbKarma_Player where uuid = ?");
								statement.setString(1, playerUUID);
								res = statement.executeQuery();
								if (res.first()) {
									String effect = res.getString("effect");
									long effectDelay = MbKarmaBungee
											.getInstance()
											.getConfig()
											.getLong(
													"effects." + effect
															+ ".delay");
									MbKarmaBungee
											.getInstance()
											.sendEffectChangeMessage(
													MbKarmaBungee
															.getInstance()
															.getProxy()
															.getPlayer(
																	UUID.fromString(playerUUID)),
													effect,
													effectDelay,
													isVisible
															&& res.getBoolean("seesParticles"));
									System.out.println("sP: "+res.getBoolean("seesParticles")+"/ vis: "+isVisible);
								}
								connection.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}

}
