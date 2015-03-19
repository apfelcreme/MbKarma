package io.github.apfelcreme.MbKarmaBungee.Listener;

import io.github.apfelcreme.MbKarmaBungee.MbKarmaBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MbKarmaBungeeTabCompleter implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onTab(TabCompleteEvent event) {
		if (!event.getSuggestions().isEmpty()) {
			return; 
		}
		String[] args = event.getCursor().split(" ");
		final String checked = (args.length > 0 ? args[args.length - 1] : event
				.getCursor()).toLowerCase();
		for (ProxiedPlayer player : MbKarmaBungee.getInstance().getProxy().getPlayers()) {
			if (player.getName().toLowerCase().startsWith(checked)) {
				event.getSuggestions().add(player.getName());
			}
		}
	}
}
