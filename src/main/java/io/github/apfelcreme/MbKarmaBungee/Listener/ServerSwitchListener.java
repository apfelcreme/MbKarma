package io.github.apfelcreme.MbKarmaBungee.Listener;

import io.github.apfelcreme.MbKarmaBungee.DatabaseConnectionManager;
import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.Connection;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerSwitchListener implements Listener {

	@EventHandler
	public void onServerSwitch(final ServerSwitchEvent e) {
		final Connection connection = DatabaseConnectionManager.getInstance().getConnection();
		if (connection == null) {
			return;
		}
		MbKarmaBungee.getInstance().getProxy().getScheduler().runAsync(MbKarmaBungee.getInstance(), new Runnable() {

			ResultSet res;
			PreparedStatement statement;
			
			@Override
			public void run() {
				try {
					statement = connection.prepareStatement("Select effect, seesParticles from MbKarma_Player where uuid = ?");
					statement.setString(1, e.getPlayer().getUniqueId().toString());
					res = statement.executeQuery();
					if (res.first()) {
						String effect = res.getString("effect");
						if (effect != null) {
							effect = effect.toUpperCase();
							boolean seesParticles = res.getBoolean("seesParticles");
							long effectDelay = MbKarmaBungee.getInstance().getConfig().getLong("effects."+effect+".delay");
							MbKarmaBungee.getInstance().sendEffectChangeMessage(e.getPlayer(), 
									effect, effectDelay, seesParticles);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}});
	}
}
