package de.momar.bukkit.ctf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import me.Truetrojan.Spectate.Spectate;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.scoreboard.Team;

public class Game implements Listener {
	private final GameParameters params;
	private volatile List<CTFPlayer> players;
	private Flag[] flags;
	private GameState gameState;
	private int[] flagsCollected;
	private Map<UUID, Integer> flagsCollectedPlayer;
	private GameScoreboard scobo;
	
	private CaptureTheFlag plugin;
	
	public Game(GameParameters parameters) {
		plugin = (CaptureTheFlag) Bukkit.getPluginManager().getPlugin("CaptureTheFlag");
		params = parameters;
		gameState = GameState.COUNTDOWN;
		
		players = new ArrayList<CTFPlayer>();
		for (Player p : params.getWorld().getPlayers())
			players.add(new CTFPlayer(p));
		
		flagsCollected = new int[params.getTeamCount() + 1];
		flagsCollectedPlayer = new HashMap<UUID, Integer>();
		
		flags = new Flag[params.getTeamCount() + 1];
		for (int i = 1; i <= params.getTeamCount(); i++) {
			flags[i] = new Flag(params.getFlags().get(i), (byte) 2, i);
		}
		
		scobo = new GameScoreboard(this);
		
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, updater, 1, 1);
	}
	
	////////////////
	// Event Loop //
	////////////////
	
	/**
	 * Update player count and game status every tick. (MAIN LOOP)
	 */
	private Runnable updater = new Runnable() { public void run() {
		updatePlayerList();
		
		//Collect flag
		for (int i = 1; i <= params.getTeamCount(); i++) {
			for (int j = 0; j < players.size(); j++)
				if (players.get(j).getFlag() == null && //The player currently has no flag
					players.get(j).getTeam() != i && //It's not the team's own flag
					flags[i].isVisible() && //The flag is visible
					players.get(j).player.getLocation().distance(params.getFlags().get(i)) < 1.5
				) {
					flags[i].collect(players.get(j));
					ParticleEffects.spawn(ParticleEffectType.FLAG_COLLECTED, params.getFlags().get(i));
					announce($.t("flagCollected", "%1 collected the flag of %2!", players.get(j).player.getName(), getTeamName(i)));
					j = players.size(); //This particular flag cannot be collected by another player!
			}
		}
		
		//Bring flag to spawn
		for (int i = 1; i <= params.getTeamCount(); i++) {
			for (int j = 0; j < players.size(); j++)
				if (players.get(j).getFlag() != null && //The player currently has a flag
					players.get(j).getTeam() == i //It's the team's own spawn point
				) {
					boolean atSpawn = false;
					for (Location spawn : params.getSpawns().get(i))
						if (players.get(j).player.getLocation().distance(spawn) < 1.5) atSpawn = true;
					if (atSpawn) {
						ParticleEffects.spawn(ParticleEffectType.FLAG_BROUGHT_HOME, players.get(j).player.getLocation());
						announce($.t("flagBroughtHome", "%1 brought the flag of %2 home!", players.get(j).player.getName(), getTeamName(players.get(j).getFlag().getTeam())));
						scobo.update();
						players.get(j).getFlag().show();
						players.get(j).player.getInventory().setHelmet(null);
						players.get(j).setFlag(null);
						flagsCollected[i]++;
						if (flagsCollectedPlayer.containsKey(players.get(j).player.getUniqueId()))
							flagsCollectedPlayer.put(players.get(j).player.getUniqueId(), flagsCollectedPlayer.get(players.get(j).player.getUniqueId()) + 1);
						else
							flagsCollectedPlayer.put(players.get(j).player.getUniqueId(), 1);
					}
			}
		}
		
		//Countdown
		if (gameState == GameState.COUNTDOWN) countdown();
	}};
	
	/**
	 * Update player list 
	 */
	private void updatePlayerList() {
		//New players (found in world but not in players)
		for (Player p : params.getWorld().getPlayers()) {
			boolean found = false;
			for (CTFPlayer cp : players) if (cp.player == p) found = true;
			if (!found) playerJoined(new CTFPlayer(p));
		}
		//Players who left (found in players but not in world)
		for (int i = 0; i < players.size(); i++) if (!players.get(i).player.isOnline()) {
			playerLeft(players.get(i));
			players.remove(i); i--;
		}
	}
	
	/////////////////
	// Game Events //
	/////////////////
	
	private void playerJoined(CTFPlayer p) {
		if (players.size() >= params.getMaxPlayersPerTeam() * params.getTeamCount()) {
			announce("No space in any team, you're a spectator!");
			spectate(p);
		}
		if (gameState != GameState.COUNTDOWN) {
			announce("The game is already running, you're a spectator!");
			spectate(p);
		}
		
		players.add(p);
		
		p.setFlag(null);
		p.setTeam(1);
		System.out.println("teleporting " + p.player.getName() + " to waiting room.");
		p.player.teleport(params.getWaitingRoom());
		
		p.player.setScoreboard(scobo.getScoreboard());
		
		announce($.t("playerJoined", "%1 joined the game.", p.player.getName()));
	}

	private void playerLeft(CTFPlayer p) {
		if (p.getFlag() != null) {
			p.getFlag().update(p.player.getLocation());
			announce($.t("playerLeftFlagDropped", "%1 left the game and dropped the flag of %2 at %3 %4 %5", p.player.getName(), getTeamName(p.getFlag().getTeam()), p.player.getLocation().getBlockX(), p.player.getLocation().getBlockY(), p.player.getLocation().getBlockZ()));
		} else announce($.t("playerLeft", "%1 left the game.", p.player.getName()));
		
		p.player.setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
	}
	
	private void won(int[] winnerTeams) {
		announce("We got some winners here: ");
		for (int i = 0; i < winnerTeams.length; i++) announce(" - Team #" + winnerTeams[i] + " (" + getTeamName(i) + ")");
	}
	private void start() {
		//Distribute players to teams
		List<Integer> availablePlayers = new ArrayList<Integer>();
		for (int i = 0; i < players.size(); i++) availablePlayers.add(i);
		announce("Team Distribution:");
		for (int i = 1; i <= params.getTeamCount(); i++) {
			announce("  - Team #" + i + " (" + getTeamName(i) + ")");
			//Spieler / Teams = Grundanzahl in jedem Team
			//Spieler % Teams = Anzahl der Teams in denen ein zusätzlicher Spieler ist
			int playersPerTeam = (int) Math.floor(players.size() / params.getTeamCount());
			int additionalPlayer = (i > players.size() % params.getTeamCount() ? 0 : 1);
			//announce("    Players: " + playersPerTeam + " + " + additionalPlayer);
			for (int n  = 0; n < playersPerTeam + additionalPlayer; n++) {
				//TODO: Currently: Random, later: Team Selection
				int playerToUse = (int) Math.floor(Math.random() * availablePlayers.size());
				players.get(availablePlayers.get(playerToUse)).setTeam(i);
				announce("    + " + players.get(availablePlayers.get(playerToUse)).player.getName());
				availablePlayers.remove(playerToUse);
			}
		}
		
		//Teleport players to their spawn
		//TODO: Better distribution, currently randomly
		for (int i = 0; i < players.size(); i++) {
			Location[] spawnPoints = params.getSpawns().get(players.get(i).getTeam());
			System.out.println("teleporting " + players.get(i).player.getName() + " to spawn.");
			players.get(i).player.teleport(randomElement(spawnPoints));
		}
	}
	
	private int minPlayersJoinedSeconds = -1;
	private int ticksSinceStart = 0;
	private void countdown() {
		if (++ticksSinceStart % 20 != 0) return;
		int secondsSinceStart = (int) Math.floor(ticksSinceStart / 20);
		
		// MinWaitTime not over.
		if (secondsSinceStart < params.getMinWaitTime()) {
			return;
		}
		
		if (minPlayersJoinedSeconds == -1)
			//Minimum player count joined
			if (players.size() >= params.getMinPlayersPerTeam() * params.getTeamCount()) minPlayersJoinedSeconds = secondsSinceStart;
			//Minimum player count not yet present
			else return;
		//Someone left so the minimum player count rule isn't fulfilled anymore.
		if (players.size() < params.getMinPlayersPerTeam() * params.getTeamCount()) { minPlayersJoinedSeconds = -1; return; }
		
		//Start the game if it's full or if everybody joined
		if (players.size() >= params.getMaxPlayersPerTeam() * params.getTeamCount() ||
			secondsSinceStart >= minPlayersJoinedSeconds + params.getMaxWaitTime()
		) {
			gameState = GameState.RUNNING;
			start();
		}
	}
	
	////////////////////////////////////////////////
	// Disable Helmet change (it could be a flag) //
	////////////////////////////////////////////////
	
	//TODO Shift+Klick, wie auch immer...
	@EventHandler public void invC(InventoryClickEvent e) {
		if (e.getSlot() == 39) e.setCancelled(true);
	}
	@EventHandler public void invD(InventoryDragEvent e) {
		if (e.getInventorySlots().contains(39)) e.setCancelled(true);
	}

	/////////////
	// Helpers //
	/////////////

	private void spectate(CTFPlayer p) {
		Spectate.getAPI().startSpectating(p.player, players.get(0).player, true);
	}
	
	//TODO: Update for /tellraw?! 
	private void announce(String text) {
		for (Player p : params.getWorld().getPlayers())
			p.sendMessage(text);
		System.out.println(text);
	}

	public String getTeamName(int i) {
		return $.t("team" + i + "_name", Integer.toString(i));
		//return randomString(6, i);
	}
	public String getTeamPrefix(int i) {
		return $.t("team" + i + "_prefix", "");
	}
	
	public static String randomString(int length, long seed) {
		String r = "";
		Random rn = new Random(seed); for (int i = 0; i < length; i++) r += (i % 2 == 1 ? "bcdfghjklmnpqrstvwxyz".charAt(rn.nextInt(21)) : "aeiou".charAt(rn.nextInt(5)));
		return r;
	}
	public static <T> T randomElement(T[] array) {
		return array[(int) Math.floor(Math.random() * array.length)];
	}
	
	public GameParameters getParameters() { return params; }
	public List<CTFPlayer> getPlayers() { return players; }
	public int[] getFlagsCollected() { return flagsCollected; }
	public int getFlagsCollectedByPlayer(UUID uniqueId) { return (flagsCollectedPlayer.get(uniqueId) == null ? 0 : flagsCollectedPlayer.get(uniqueId)); }
}
