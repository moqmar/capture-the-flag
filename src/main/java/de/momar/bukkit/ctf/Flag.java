package de.momar.bukkit.ctf;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Flag {

	Location block;
	byte color;
	int team;
	
	public Flag(Location location, byte color, int team) {
		block = location;
		this.color = color;
		this.team = team;
		update(block);
	}
	
	public Location getLocation() { return block; }
	
	public void update(Location n) {
		block.getBlock().setType(Material.AIR);
		n.getBlock().setType(Material.STANDING_BANNER);
		n.getBlock().setData(color);
		block = n;
	}
	
	public void hide() {
		block.getBlock().setType(Material.AIR);
	}
	public void show() {
		block.getBlock().setType(Material.STANDING_BANNER);
		block.getBlock().setData(color);
	}
	public boolean isVisible() { return block.getBlock().getType() == Material.STANDING_BANNER; }
	
	public int getTeam() { return team; }
	
	//TODO Effekte
	//TODO Hut kann nicht verändert werden!
	public void collect(CTFPlayer p) {
		p.player.getInventory().setHelmet(new ItemStack(Material.BANNER, 1, color));
		p.setFlag(this);
		hide();
	}
	
}
