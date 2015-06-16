package de.momar.bukkit.ctf.database;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.*;
import de.momar.bukkit.ctf.CaptureTheFlag;

public class PlayerInfo {
	private static PlayerInfo instance;
	public static synchronized PlayerInfo getInstance() {
		if (PlayerInfo.instance == null) PlayerInfo.instance = new PlayerInfo();
		return PlayerInfo.instance;
	}
	
	private MongoClient mongoClient;
	private MongoDatabase mongoDatabase;
	MongoCollection<Document> mongoCollection;
	private PlayerInfo() {
		//Connect to database...
		MongoClientURI mongoURI = new MongoClientURI(CaptureTheFlag.getConfiguration().getString("mongo.connection"));
		mongoClient     = new MongoClient();
		mongoDatabase   = mongoClient.getDatabase(mongoURI.getDatabase());
		mongoCollection = mongoDatabase.getCollection("capture-the-flag");
	}
	
	public void close() {
		mongoClient.close();
	}
	
	public int getInfo(UUID player, PlayerInfoType info) {
		FindIterable<Document> result = mongoCollection.find(eq("uuid", player.toString()));
		if (result.first() == null) return 0;
		else return result.first().getInteger(getKeyFromType(info), 0);
	}
	
	public void putInfo(UUID player, PlayerInfoType info, int value) {
		UpdateResult result = mongoCollection.updateOne(eq("uuid", player.toString()), new Document(getKeyFromType(info), value));
		if (result.getModifiedCount() < 1) {
			mongoCollection.insertOne(new Document("uuid", player.toString())
			                               .append(getKeyFromType(info), value));
		}
	}
	
	public static String getKeyFromType(PlayerInfoType type) {
		String key = ""; switch (type) {
		case MONEY:      key = "money";      break;
		case KILLS:      key = "kills";      break;
		case DEATHS:     key = "deaths";     break;
		case GAMES_WON:  key = "games_won";  break;
		case GAMES_LOST: key = "games_lost"; break;
		case GAMES_QUIT: key = "games_quit"; break;
		}
		return key;
	}
}
