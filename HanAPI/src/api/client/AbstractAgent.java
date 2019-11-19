package api.client;

import api.game.Action;
import api.game.Game;
import api.game.State;
import api.game.Turn;
import sjson.JSONArray;

public abstract class AbstractAgent
{
	public abstract Action chooseAction();

	public abstract void notifyState(State state);

	public abstract void notifyTurn(Turn turn);

	/**
	 * Riordina i giocatori a partire dal successivo a questo Agent. Questo Agent &egrave; escluso dalla lista
	 * @return
	 */
	public JSONArray sortPlayers()
	{
		//Riordino i giocatori e tolgo me stesso, riordino a partire dal mio successivo
		JSONArray players = Game.getInstance().getPlayers().clone();
		int myturn = Game.getInstance().getPlayerTurn(Main.name);
		for (int i = 0; i < myturn; i++)
			players.add(players.get(i));

		for (int i = 0; i <= myturn; i++)
			players.remove(0);
		return players;
	}
}
