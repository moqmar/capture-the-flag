package de.momar.bukkit.ctf;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class GameScoreboard {

	private Game game;
	private ScoreboardManager scoma;
	private Scoreboard scobo;
	private Team[] scote;
	private Objective sidebar;
	private Objective tablist;
	private Objective health;
	
	public GameScoreboard(Game g) {
		game = g; System.out.println(game);
		scoma = Bukkit.getScoreboardManager();
		scobo = scoma.getNewScoreboard();
		scote = new Team[game.getParameters().getTeamCount() + 1];
		for (int i = 1; i <= game.getParameters().getTeamCount(); i++) {
			scote[i] = scobo.registerNewTeam(game.getTeamName(i));
			
			//TODO: Add configuration options
			scote[i].setPrefix(game.getTeamPrefix(i));
			scote[i].setCanSeeFriendlyInvisibles(true);
			scote[i].setAllowFriendlyFire(false);
		}
		
		sidebar = scobo.registerNewObjective(game.getParameters().getWorld().getName().replace(" ", "_") + "-sidebar", "dummy");
		sidebar.setDisplayName($.t("scoreboardTitle", "Flags collected"));
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		tablist = scobo.registerNewObjective(game.getParameters().getWorld().getName().replace(" ", "_") + "-tablist", "dummy");
		tablist.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		
		health = scobo.registerNewObjective(game.getParameters().getWorld().getName().replace(" ", "_") + "-health", "health");
		health.setDisplayName("§4❤");
		health.setDisplaySlot(DisplaySlot.BELOW_NAME);
		
		update();
	}
	
	public Scoreboard getScoreboard() { return scobo; }
	
	public void update() {
		for (int i = 1; i <= game.getParameters().getTeamCount(); i++) {
			sidebar.getScore(game.getTeamName(i)).setScore(game.getFlagsCollected()[i]);
		}
		for (int i = 0; i < game.getPlayers().size(); i++) {
			tablist.getScore(game.getPlayers().get(i).player.getName()).setScore(game.getFlagsCollectedByPlayer(game.getPlayers().get(i).player.getUniqueId()));
		}
	}
	
}
