package api.client;

import api.game.*;
import sjson.JSONArray;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractAgent
{
	protected Statistics stats;
	protected boolean log;
	protected PrintStream logfile;

	public AbstractAgent(boolean log, String logpath) throws FileNotFoundException
	{
		stats = null;
		this.log = log;
		if (logpath == null)
			logfile = null;
		else
			logfile = new PrintStream(System.getProperty("user.dir")+"/"+logpath);
	}

	public abstract Action chooseAction();

/*	public Action getAction()
	{
		Action action = chooseAction();
		stats.updateAction(action);
		return action;
	}
*/

	public void log(String s)
	{
		if (log)
			System.out.println(s);
		if (logfile!=null)
			logfile.println(s);
	}

	public void log(Exception e)
	{
		if (log)
			e.printStackTrace(System.out);
		if (logfile!=null)
			e.printStackTrace(logfile);
	}

	public void notifyState(State state)
	{
		if (stats == null)
			stats = new Statistics();
		stats.addState(state);
	}

	public void notifyTurn(Turn turn)
	{
		stats.updateTurn(turn);
	}

	public List<Action> getPossibleHints(String receiver) throws JSONException
	{
		ArrayList<Action> list = new ArrayList<>();
		Hand hand = stats.getLastState().getHand(receiver).clone();
		List<Integer> valueAdded = new ArrayList<>();
		List<Color> colorAdded = new ArrayList<>();

		int value;
		Color col;

		for(int i=0; i<hand.size(); i++)
		{
			value = hand.getCard(i).getValue();
			col = hand.getCard(i).getColor();

			if(!hand.getCard(i).isValueRevealed() && valueAdded.indexOf(value)==-1)
			{
				valueAdded.add(value);
				list.add(new Action(Main.playerName, receiver, value));
			}

			if(!hand.getCard(i).isColorRevealed() && colorAdded.indexOf(col)==-1)
			{ //colore incontrato non ancora aggiunto
				colorAdded.add(col);
				list.add(new Action(Main.playerName, receiver, col));
			}
		}
		return list;
	}

	/**
	 * Riordina i giocatori a partire dal successivo a questo Agent. Questo Agent &egrave; escluso dalla lista
	 * @return
	 */
	public String[] sortPlayers()
	{
		//Riordino i giocatori a partire dal mio successivo e tolgo me stesso
		String[] players = Game.getInstance().getPlayers();
		String[] ret = new String[players.length-1];
		int myturn = Game.getInstance().getPlayerTurn(Main.playerName);
		int cont = 0;
		for (int i=myturn+1; i<players.length; i++)
		{
			ret[cont] = players[i];
			cont++;
		}
		for (int i=0; i<myturn; i++)
		{
			ret[cont] = players[i];
			cont++;
		}
		return ret;
	}
}
