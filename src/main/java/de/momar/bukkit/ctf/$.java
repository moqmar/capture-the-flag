package de.momar.bukkit.ctf;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Translation Provider
 * @author momar
 *
 */
public final class $ {
	private $() {}
	
	private static FileConfiguration data;
	
	/**
	 * Translate a string
	 * @param tid Translation ID (from the configuration file)
	 * @param def Original text (used if configuration doesn't contain a translation)
	 * @param rpl Replacement values: $("Hello %1!", "World") -> "Hello World!"
	 * @return Translated string
	 */
	public static String t(String tid, String def, Object... rpl) {
		//Create config file if it doesn't exist.
		if (data == null) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("CaptureTheFlag");
			
			File dataFolder = plugin.getDataFolder();
			File configFile = new File(dataFolder, "translation.yml");
			data = new YamlConfiguration();
			
			//Create Data folder
			if (!dataFolder.exists()) dataFolder.mkdir();
			//Create translation file
			if (!configFile.exists()) plugin.saveResource("translation.yml", false);
			//Load translation file
			try { data.load(configFile);
			} catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
		}
		
		String result = def;
		if (data != null && data.getString(tid, "").length() > 0) result = data.getString(tid, "");
		for (int i = 0; i < rpl.length; i++)
			result = result.replace("%" + (i+1), rpl[i].toString());
		return result;
	}
}
