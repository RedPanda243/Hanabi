package api.game;

import sjson.*;

import java.io.Reader;

/**
 * Rappresenta il primo messaggio che viene inviato dal server quando ha trovato tutti i giocatori. Dà informazioni generali e
 * immutabili sulla partita. In una JVM può esistere un Game alla volta. Molte classi del package api.game sfruttano l'oggetto
 * Game.instance
 */
public class Game extends JSONObject
{

	private static Game instance = null;

	public Game(Reader reader) throws JSONException
	{
		super(reader);
		if (instance != null)
			throw new JSONException("Game instance already exists");
		setPlayers(getArray("players"));
		instance = this;
	}

	public void close()
	{
		instance = null;
	}

	public int getNumberOfCardsPerPlayer()
	{
		return getPlayers().size()>3?4:5;
	}

	public String getPlayer(int index)
	{
		return getPlayers().getString(index);
	}


	public JSONArray getPlayers()
	{
		return getArray("players");
	}

	public int getPlayerTurn(String player)
	{
		JSONData d = new JSONString(player);
		int i = 0;
		for(JSONData ad: getPlayers())
		{
			if (ad.equals(d))
				return i;
			i++;
		}
		return -1;
	}

	public boolean isPlaying(String player)
	{
		return (getPlayerTurn(player)!=-1);
	}

	public Game setPlayers(JSONArray array) throws JSONException
	{
		if (array.size()<2 || array.size()>5)
			throw new JSONException("Unacceptable number of players");
		for(JSONData d:array)
		{
			if (d.getJSONType()!=Type.STRING)
				throw new JSONException("Player's names must be string");
		}
		set("players",array);
		return this;
	}

	public static Game getInstance()
	{
		return instance;
	}
}
