package de.momar.bukkit.ctf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

public class GameParameters {

	private World world;
	public World getWorld() { return world; }
	
	private int teamCount;
	public int getTeamCount() { return teamCount; }
	
	private int minPlayersPerTeam;
	public int getMinPlayersPerTeam() { return minPlayersPerTeam; }
	
	private int maxPlayersPerTeam;
	public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
	
	private int minWaitTime;
	public int getMinWaitTime() { return minWaitTime; }
	
	private int maxWaitTime;
	public int getMaxWaitTime() { return maxWaitTime; }
	
	private Location waitingRoom;
	public Location getWaitingRoom() { return waitingRoom; }
	
	private Map<Integer, Location[]> spawns;
	public Map<Integer, Location[]> getSpawns() { return spawns; }
	
	private Map<Integer, Location> flags;
	public Map<Integer, Location> getFlags() { return flags; }
	
	private List<Location> shops;
	public List<Location> getShops() { return shops; }
	
////////////////////////////////////////////////////////////////////////////////
	
	public GameParameters(String worldname, ConfigurationSection config) throws InvalidConfigurationException {
		world             = Bukkit.getWorld(worldname);
		if (world == null) throw new InvalidConfigurationException
		("Invalid world name (World \"" + worldname + "\" doesn't exist)");
		
		teamCount         = config.getInt("teams", -1);
		if (teamCount <= 1) throw new InvalidConfigurationException
		("Invalid teamCount: " + teamCount);
		
		minPlayersPerTeam = config.getInt("minPlayersPerTeam", -1);
		if (minPlayersPerTeam <= 0) throw new InvalidConfigurationException
		("Invalid minPlayersPerTeam: " + minPlayersPerTeam);
		
		maxPlayersPerTeam = config.getInt("maxPlayersPerTeam", -1);
		if (maxPlayersPerTeam <= 0) throw new InvalidConfigurationException
		("Invalid maxPlayersPerTeam: " + maxPlayersPerTeam);
		if (maxPlayersPerTeam < minPlayersPerTeam) throw new InvalidConfigurationException
		("Invalid maxPlayersPerTeam (must be equal or greater than minPlayersPerTeam)");
		
		minWaitTime       = config.getInt("minWaitTime", -1);
		if (minWaitTime < 0) throw new InvalidConfigurationException
		("Invalid minWaitTime: " + minWaitTime);
		
		maxWaitTime       = config.getInt("maxWaitTime", -1);
		if (maxWaitTime < 0) throw new InvalidConfigurationException
		("Invalid maxWaitTime: " + maxWaitTime);
		
		waitingRoom = locationFromList(config.getDoubleList("waitingRoom"));

		spawns = new HashMap<Integer, Location[]>();
		Set<String> spawnMap = config.getConfigurationSection("spawns").getKeys(false);
		for (String tm : spawnMap) {
			int t = 0;
			try {
				t = Integer.parseInt(tm);
			} catch (NumberFormatException e) {
				throw new InvalidConfigurationException
				("Invalid team " + tm + " in spawns (Teams are identified by integers)");
			}
			
			//Try to convert that list from whatever format it is...
			@SuppressWarnings("unchecked")
			List<List<?>> spawnList = (List<List<?>>) config.getConfigurationSection("spawns").getList(tm);

			if (spawnList.size() <= 0) throw new InvalidConfigurationException
			("Invalid team " + tm + " in spawns (A team needs at least one spawn point)");
			
			if (spawnList.get(0) instanceof List<?>) {
				spawns.put(t, new Location[spawnList.size()]);
				for (int i = 0; i < spawnList.size(); i++)
					spawns.get(t)[i] = locationFromList(spawnList.get(i));
			} else if (spawnList.get(0) instanceof Number) {
				spawns.put(t, new Location[1]);
				spawns.get(t)[0] = locationFromList(spawnList);
			} else throw new InvalidConfigurationException
			("Invalid spawn list (Spawns must be a set of coordinates)");
		}

		flags = new HashMap<Integer, Location>();
		Set<String> flagMap = config.getConfigurationSection("flags").getKeys(false);
		for (String tm : flagMap) {
			int t = 0;
			try {
				t = Integer.parseInt(tm);
			} catch (NumberFormatException e) {
				throw new InvalidConfigurationException
				("Invalid team " + tm + " in flags (Teams are identified by integers)");
			}
			
			flags.put(t, locationFromList(config.getConfigurationSection("flags").getDoubleList(tm)));
		}
		
		shops = new ArrayList<Location>();
		//Try to convert that list from whatever format it is...
		@SuppressWarnings("unchecked")
		List<List<?>> shopList = (List<List<?>>) config.getList("shops");

		if (shopList.size() > 0) {
			if (shopList.get(0) instanceof List<?>) {
				for (int i = 0; i < shopList.size(); i++)
					shops.add(locationFromList(shopList.get(i)));
			} else if (shopList.get(0) instanceof Number) {
				shops.add(locationFromList(shopList));
			} else throw new InvalidConfigurationException
			("Invalid shop (Shops must be a set of coordinates)");
		}
		
		for (int i = 1; i <= teamCount; i++) {
			if (!spawns.containsKey(i) || spawns.get(i).length <= 0) throw new InvalidConfigurationException
			("No spawn points defined for team " + i);
			
			if (!flags.containsKey(i)) throw new InvalidConfigurationException
			("No flag location defined for team " + i);
		}
	}
	
	private Location locationFromList(List<?> config) throws InvalidConfigurationException {
		if (config.size() != 3) throw new InvalidConfigurationException
		("Invalid location definition: " + Arrays.toString(config.toArray()) + " (Must contain exactly 3 elements (X, Y and Z))");
		
		//Integer workaround
		List<Double> list = new ArrayList<Double>();
		for (int i = 0; i < 3; i++)
			list.add(Double.parseDouble(config.get(i).toString()));
		return new Location(world, list.get(0), list.get(1), list.get(2));
	}
	
	public String toString() {
		String r = "world = " + world.getName() + "\n";
		r       += "teamCount = " + teamCount + "\n";
		r       += "minPlayersPerTeam = " + minPlayersPerTeam + "\n";
		r       += "maxPlayersPerTeam = " + maxPlayersPerTeam + "\n";
		r       += "minWaitTime = " + minWaitTime + "\n";
		r       += "maxWaitTime = " + maxWaitTime + "\n";
		r       += "waitingRoom = X=" + waitingRoom.getX() + " Y=" + waitingRoom.getY() + " Z=" + waitingRoom.getZ() + "\n";
		
		r       += "spawns:\n";
		for (int team : spawns.keySet()) {
			r   += "  Team " + team + ":\n";
			for (Location spawn : spawns.get(team))
				r += "    » X=" + spawn.getX() + " Y=" + spawn.getY() + " Z=" + spawn.getZ() + "\n";
		}
		
		r       += "flags:\n";
		for (int team : flags.keySet())
			r   += "  Team " + team + ": X=" + flags.get(team).getX() + " Y=" + flags.get(team).getY() + " Z=" + flags.get(team).getZ() + "\n";

		r       += "shops:\n";
		for (Location shop : shops)
			r   += "  » X=" + shop.getX() + " Y=" + shop.getY() + " Z=" + shop.getZ() + "\n";
		
		return r;
	}
}
