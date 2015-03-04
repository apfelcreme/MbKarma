package io.github.apfelcreme.MbKarmaBungee.Transactions;

import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class Transaction {

	public ProxiedPlayer sender;
	
	public Transaction(ProxiedPlayer sender) {
		this.sender = sender;
	}
	
	/**
	 * @return the sender
	 */
	public ProxiedPlayer getSender() {
		return sender;
	}
	
	/**
	 * @param sender the sender to set
	 */
	public void setSender(ProxiedPlayer sender) {
		this.sender = sender;
	}
	
	/**
	 * does the thing
	 * @throws SQLException
	 */
	public abstract void send() throws SQLException;
	

	
	/**
	 * returns the Level of the given particle by looping through the list in the config. 
	 * @param effect
	 * @return the level of the effect or -1 if the effect isnt in the list
	 */
	@SuppressWarnings("unchecked")
	public static int getEffectLevel(String effect) {
		for (Entry<String, String> entry : ((Map<String, String>)MbKarmaBungee.getInstance().getConfig().get("level")).entrySet()) {
			if (entry.getValue().equals(effect.toUpperCase())) {
				return Integer.parseInt(entry.getKey());	
			}
		}
		return -1;
	}
}
