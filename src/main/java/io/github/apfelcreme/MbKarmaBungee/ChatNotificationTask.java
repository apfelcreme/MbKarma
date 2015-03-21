package io.github.apfelcreme.MbKarmaBungee;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.md_5.bungee.api.ChatColor;

public class ChatNotificationTask implements Runnable {

	private Set<String> names;
	private Integer delayMinutes;
	private Integer count;

	public ChatNotificationTask(Integer delayMinutes) {
		this.delayMinutes = delayMinutes;
		names = new HashSet<String>();
		count = new Integer(0);
	}

	@Override
	public void run() {
		List<String> displayNames = new ArrayList<String>(names);
		Collections.sort(displayNames);
		if (names.size() != 0) {
			String message = MbKarmaBungee.getInstance()
					.getTextNode("broadcast.notificationBroadcast")
					.replace("{0}", delayMinutes.toString()).replace("{1}", count.toString());
			if (names.size() == 1) {
				message = message.replace("{2}", names.iterator().next());
			} else {
				message = message.replace(
						"{2}",
						StringUtils.join(displayNames.toArray(),
								ChatColor.GRAY + ", "
										+ ChatColor.WHITE, 0,
								names.size() - 1)
								+ ChatColor.GRAY
								+ " & "
								+ ChatColor.WHITE
								+ names.iterator().next());
			}
			MbKarmaBungee.getInstance().sendBroadcast(message);
		}
		names.clear(); 
		count = 0;
	}
	
	/**
	 * adds a name to the list
	 * @param name
	 */
	public void add(String name) {
		count++;
		names.add(name);
	}

}
