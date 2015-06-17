package de.momar.bukkit.ctf;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CaptureTheFlag extends JavaPlugin implements Listener {

	public void onEnable() {
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		
		/*try {
			getLogger().log(Level.INFO, "\n==========================================================================================\n\n" + 
					new GameParameters("world", getConfig().getConfigurationSection("test")).toString() + "\n\n");
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		getServer().shutdown();*/
		
		try {
			getServer().getPluginManager().registerEvents(
				new Game(new GameParameters("world", getConfig().getConfigurationSection("test"))),
			this);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	public void onDisable() {
		
	}
	
	public static FileConfiguration getConfiguration() {
		return Bukkit.getPluginManager().getPlugin("CaptureTheFlag").getConfig();
	}
	
	@EventHandler public void inv(InventoryClickEvent e) { System.out.println("höh?"); }
	
}
