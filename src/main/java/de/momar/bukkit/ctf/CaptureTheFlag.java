package de.momar.bukkit.ctf;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CaptureTheFlag extends JavaPlugin {

	public void onEnable() {
		saveDefaultConfig();
		try {
			getLogger().log(Level.INFO, "\n==========================================================================================\n\n" + 
					new GameParameters("world", getConfig().getConfigurationSection("test")).toString() + "\n\n");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		getServer().shutdown();
	}
	public void onDisable() {
		
	}
	
	public static FileConfiguration getConfiguration() {
		return Bukkit.getPluginManager().getPlugin("CaptureTheFlag").getConfig();
	}
	
	
	
}
