import api.client.Main;
import api.client.StatisticState;
import api.game.*;
import api.client.AbstractAgent;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Agente che segue questa strategia:
 * <ol>
 *     <li>
 *         Gioca una carta con 100% di playability
 *     </li>
 *     <li>
 *         Se hai hint token ed è possibile rendere giocabile al 100% una carta di un compagno fallo
 *     </li>
 *     <li>
 *         Se hai hint token e un compagno ha una carta con 0% uselessness indicagliela
 *     </li>
 *     <li>
 *         Scarta una carta con 100% di uselessness se non hai 8 hint token
 *     </li>
 *     <li>
 *         Se hai hint token dai il suggerimento che coinvolge più carte
 *     </li>
 *     Da qui in poi spero di non arrivarci mai
 *     <li>
 *         Scarto carta con uselessness maggiore (sopra un certo limite)
 *     </li>
 *     <li>
 *         Gioco carta con playability maggiore
 *     </li>
 * </ol>
 */
public class Strategy1Agent extends AbstractAgent {

	public Strategy1Agent(boolean log, String logpath) throws FileNotFoundException
	{
		super(log,logpath);
	}

	public void notifyTurn(Turn turn)
	{
		super.notifyTurn(turn);
		log(turn+"\n");
	}

	public void notifyState(State state)
	{
		super.notifyState(state);
		try {
			StatisticState sstate = new StatisticState(state, stats);
			log(""+sstate);
//			stats.printPossibilities(System.out);
		/*	System.out.println("Suggerimenti possibili:");
			for (String player: Game.getInstance().getPlayers())
			{
				if (!player.equals(Main.playerName))
				{
					System.out.println(player);
					for (Action h:this.getPossibleHints(player))
						System.out.println("\t"+h);
				}
			}*/
		}
		catch (JSONException e){}
	}

	@Override
	public Action chooseAction()
	{
		Action action = play100();
		if (action == null)
		{
	/*		action = hint100();
		if (action == null)
			action = hint0();
		if (action == null)*/
			log("Impossibile giocare una carta sicura");
			action = discard100();
		}
		if (action == null) {
			log("Impossibile scartare una carta sicura");
			action = hintMost();
		}
		if (action == null) {
			log("Impossibile suggerire");
			action = discardMost();
		}
		if (action == null) {
			log("Impossibile scartare");
			action = playMost();
		}
		log(action.toString());
		return action;
	}

	public Action play100() {
		double[] p = stats.getPlayability(Main.playerName);

		try {
			for (int i = 0; i < p.length; i++) {
				if (p[i] == 1)
					return new Action(Main.playerName, ActionType.PLAY, i);
			}
		} catch (JSONException e) {
			log(e);
		}
		return null;
	}

/*	public Action hint100() {
		try {
			if (stats.getLastState().getHintTokens() > 0) {
				String[] players = sortPlayers();
				List<Action> l;
				double[] p, p1;
				Hand handPlayerToHint;
				for (String name : players) {
					handPlayerToHint = stats.getLastState().getHand(name);
					l = getPossibleHints(name);
					p = stats.getPlayability(name);
					for (Action a : l) {
						p1 = stats.getPlayability(name,a);
						for (int i = 0; i < p.length; i++) {
							if (p1[i] == 1 && p[i] < 1) {
								if (a.getType().equals(ActionType.HINT_COLOR)) {
									if (!handPlayerToHint.getCard(i).isColorRevealed())
										return a;
								} else { //hint value
									if (!handPlayerToHint.getCard(i).isValueRevealed())
										return a;
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Action hint0() {
		try {
			if (stats.getLastState().getHintTokens() > 0) {
				JSONArray players = sortPlayers();
				List<Action> l;
				double[] p, p1;
				Hand handPlayerToHint;
				for (JSONData name : players) {
					handPlayerToHint = stats.getLastState().getHand(name.toString());
					l = getPossibleHints(name.toString());
					p = stats.getUselessness(name.toString()); //uselessness prima dell'aiuto
					for (Action a : l) //testo tutte le azioni tra quelle possibili
					{
						p1 = stats.getUselessness(name.toString(), a); //uselessness dopo l'aiuto
						for (int i = 0; i < p.length; i++) {
							if (p1[i] == 0 && p[i] > 0)
								if (a.getType().equals(ActionType.HINT_COLOR)) {
									if (!handPlayerToHint.getCard(i).isColorRevealed())
										return a;
								} else { //hint value
									if (!handPlayerToHint.getCard(i).isValueRevealed())
										return a;
								}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
*/
	public Action discard100() {
		double[] p = stats.getUselessness(Main.playerName);

		if (stats.getLastState().getHintTokens() < 8) {
			try {
				for (int i = 0; i < p.length; i++) {
					if (p[i] == 1)
						return new Action(Main.playerName, ActionType.DISCARD, i);
				}
			} catch (JSONException e) {
				log(e);
			}
		}
		return null;
	}

	public Action hintMost() {
		try {
			State current = stats.getLastState();
			if (current.getHintTokens() > 0) {
				String[] players = sortPlayers();
				List<Action> l;
				Action best = null;
				int bestcont = 0, cont = 0;
				Hand hand;
				Card box;
				for (String name : players) {
					l = getPossibleHints(name);
					hand = current.getHand(name);
					for (Action a : l) {
						cont = 0;
						if (a.getType() == ActionType.HINT_COLOR) {
							for (JSONData card : hand.toJSON()) {
								box = (Card) card;
								if (!box.isColorRevealed() && box.getColor() == a.getColor())
									cont++;
							}
							if (cont > bestcont) {
								bestcont = cont;
								best = a;
							}
						} else if (a.getType() == ActionType.HINT_VALUE) {
							for (JSONData card : hand.toJSON()) {
								box = (Card) card;
								if (!box.isValueRevealed() && box.getValue() == a.getValue())
									cont++;
							}
							if (cont > bestcont) {
								bestcont = cont;
								best = a;
							}
						} else
							throw new JSONException("Wrong action type");
					}
				}
				return best;
			}
		} catch (JSONException e) {
			log(e);
		}
		return null;
	}

	public Action discardMost() {
		try {
			double[] p = stats.getUselessness(Main.playerName);
			int card = 0;
			double max = p[card];
			double box;
			for (int i = 1; i < p.length; i++) {
				box = p[i];
				if (box > max) {
					max = box;
					card = i;
				}
			}
			return new Action(Main.playerName, ActionType.DISCARD, card);
		} catch (JSONException e) {
			log(e);
		}
		return null;
	}

	public Action playMost() {
		try {
			double[] p = stats.getPlayability(Main.playerName);
			int card = 0;
			double max = p[card];
			double box;
			for (int i = 1; i < p.length; i++) {
				box = p[i];
				if (box > max) {
					max = box;
					card = i;
				}
			}
			return new Action(Main.playerName, ActionType.PLAY, card);
		} catch (JSONException e) {
			log(e);
		}
		return null;
	}

	public static void main(String[] args) throws Exception
	{
		boolean log = false;
		String logpath = null;
		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-l"))
			{
				log = true;
			}
			else if (args[i].equals("-f"))
			{
				i++;
				logpath = args[i];
			}
		}
		AbstractAgent agent = new Strategy1Agent(log,logpath);
		Main.setAgent(agent);
		Main.main(args);
	}
}

