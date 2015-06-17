package de.momar.bukkit.ctf;

import org.bukkit.entity.Player;

public class CTFPlayer {
	public Player player;
	private Flag f; //Flag
	private int t;  //Team
	
	public CTFPlayer(Player p) {
		player = p;
		f = null;
		t = 0;
	}

	public void setFlag(Flag flag) { f = flag; }
	public Flag getFlag() { return f; }
	
	public void setTeam(int team) { t = team; }
	public int  getTeam() { return t; }
}
