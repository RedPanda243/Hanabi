package api.game;

import sjson.JSONConvertible;
import sjson.JSONException;
import sjson.JSONObject;

/**
 * Rappresenta il primo messaggio che viene inviato dal server quando ha trovato tutti i giocatori. Dà informazioni generali e
 * immutabili sulla partita.
 */
public class Game extends JSONConvertible<JSONObject>
{
	private static Game instance = null;

	private String[] players;

	public Game(JSONObject json)
	{
		super(json);
		//TODO controlla i campi del json!
		if (instance == null)
			instance = this;
		else
			throw new IllegalStateException("A game is already running");
	}

	public int getNumberOfCardsPerPlayer()
	{
		return getPlayers().length>3?4:5;
	}

	public String[] getPlayers()
	{
		return players;
	}

	public static Game getInstance()
	{
		return instance;
	}
}
