package de.momar.bukkit.ctf;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class Game implements Listener {
	private final GameParameters params;
	private List<CTFPlayer> players;
	
	private CaptureTheFlag plugin;
	
	public Game(GameParameters parameters) {
		plugin = (CaptureTheFlag) Bukkit.getPluginManager().getPlugin("CaptureTheFlag");
		params = parameters;
		
		players = new ArrayList<CTFPlayer>();
		for (Player p : params.getWorld().getPlayers())
			players.add(new CTFPlayer(p));
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, updater, 1, 1);
	}
	
	/**
	 * Update player count and game status every tick. (MAIN LOOP)
	 */
	private Runnable updater = new Runnable() { public void run() {
		
	}};
	
	/**
	 * Update player list 
	 */
	private void updatePlayerList() {
		//New players (found in world but not in players)
		for (Player p : params.getWorld().getPlayers()) {
			boolean found = false;
			for (CTFPlayer cp : players)
				if (cp.player == p)
					found = true;
			if (!found) {
				players.add(new CTFPlayer(p));
			}
		}
		//Players who left (found in players but not in world)
		for (CTFPlayer cp : players) {
			CTFPlayer found = null;
			for (Player p : params.getWorld().getPlayers())
				if (cp.player == p)
					found = cp;
			if (found != null) {
				players.remove(found);
			}
		}
	}
}
