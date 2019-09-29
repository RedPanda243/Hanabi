package api.game;

import sjson.JSONArray;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;

/**
 * Rappresenta il primo messaggio che viene inviato dal server quando ha trovato tutti i giocatori. Dà informazioni generali e
 * immutabili sulla partita.
 */
public class Game extends JSONObject
{

	public Game(Reader reader) throws JSONException
	{
		super(reader);
	}

	public int getNumberOfCardsPerPlayer()
	{
		return getPlayers().size()>3?4:5;
	}

	public JSONArray getPlayers()
	{
		return getArray("players");
	}
}
