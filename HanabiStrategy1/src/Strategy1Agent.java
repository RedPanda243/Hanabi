import api.client.Main;
import api.client.StatisticState;
import api.client.Statistics;
import api.game.*;
import api.client.AbstractAgent;
import sjson.JSONData;
import sjson.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Agente che segue questa strategia:
 * <ol>
 *     <li>
 *         Gioca una carta sicura. Se ne hai più d'una gioca quella con valore pi&ugrave; alto.
 *     </li>
 *     <li>
 *         Se hai hint token dai il suggerimento che rende sicure il maggior numero di carte.
 *         Se pi&ugrave; suggerimenti rendono sicuri lo stesso numero di carte dai quello che produce
 *         la miglior diminuzione di entropia.
 *         Se nessun suggerimento porta al 100% la playability di nessuna carta passa al prossimo punto
 *     </li>
 *     <li>
 *         Se hai hint token dai il suggerimento che produce la miglior diminuzione di entropia
 *         altrimenti scarta la carta con uselessness maggiore
 *     </li>
 * </ol>
 *
 * L'algoritmo implementato ottimizza i punti 2 e 4 facendo un'unica esplorazione dei suggerimenti e applicando i controlli a
 * posteriori.
 *
 */
public class Strategy1Agent extends AbstractAgent
{

	private double SECURE_PLAYABILITY = 1;
	private boolean confirm;

	public Strategy1Agent(boolean log, String logpath,boolean confirm) throws FileNotFoundException
	{
		super(log,logpath);
		this.confirm = confirm;
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
		if (confirm)
		{
			log("\nPremi INVIO per prossimo turno...");
			try
			{
				new BufferedReader(new InputStreamReader(System.in)).readLine();
			}
			catch(IOException e){}
		}
		try {
			int tokens = stats.getLastState().getHintTokens();
			Action action = playSecure();
			List<Action> hints = new ArrayList<>();
			if (action == null) {
				log("Impossibile giocare una carta sicura");
				if (tokens > 0) {

					for (String p : sortPlayers())
						hints.addAll(getPossibleHints(p));

					if (getBestHintForPlayability(hints) > 0)
						action = getBestHintForEntropy(hints);
					if (action == null)
					{
						log("Impossibile suggerire carte sicure");
						action = getBestHintForEntropy(hints);
					}
				}
				else
				{
					log("Impossibile suggerire");
					action = discardMost();
				}
			}
			log(action.toString());
			return action;
		}
		catch(JSONException e)
		{
			log(e);
			return null;
		}
	}

	public Action playSecure() {
		double[] p = stats.getPlayability(Main.playerName);
		Hand hand = stats.getLastState().getHand(Main.playerName);
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < p.length; i++)
		{
			if (p[i] >= 1)
				list.add(i);
		}
		while(list.size()>1)
		{
			if (hand.getCard(list.get(1)).getValue()>hand.getCard(list.get(0)).getValue())
				list.remove(0);
			else
				list.remove(1);
		}
		try
		{
			if (list.size()>0)
				return new Action(Main.playerName, ActionType.PLAY, list.get(0));
		}
		catch(JSONException e)
		{
			log(e);

		}
		return null;
	}

	/**
	 * Restuisce il numero massimo di carte rese giocabili. La lista parametro viene modificata in modo da tenere solo i
	 * suggerimenti che raggiungono il valore ritornato se questo è maggiore di 0
	 * @param list
	 * @return
	 * @throws JSONException
	 */
	public int getBestHintForPlayability(List<Action> list) throws JSONException
	{
		List<Action> l = new ArrayList<>(list);
		list.clear();
		int max = 0;
		for(Action hint:l)
		{
			double[] p = stats.getPlayability(hint.getHinted());
			Statistics statsif = stats.getStatisticsIf(new Turn(hint,hint.getCardsToReveal(stats.getLastState())));
			double[] p1 = statsif.getPlayability(hint.getHinted());
			int cont = 0;
			for (int i=0; i<p.length; i++)
			{
				if (p[i]<SECURE_PLAYABILITY && p1[i]>=SECURE_PLAYABILITY)
					cont++;
			}
			if (cont>max)
			{
				max = cont;
				list.clear();
			}
			if (cont==max)
				list.add(hint);
		}
		return max;
	}

	public Action getBestHintForEntropy(List<Action> list) throws JSONException
	{
		Action best = list.get(0);
		double e,max = calcE(list.get(0));

		for (int i=1; i<list.size(); i++)
		{
			e = calcE(list.get(i));
			if (e>max)
			{
				max = e;
				best = list.get(i);
			}
		}
		return best;
	}

	/**
	 * Calcola il decremento di entropia dovuto al suggerimento passato come parametro
	 * @param hint
	 * @return
	 * @throws JSONException
	 */
	private double calcE(Action hint) throws JSONException
	{
		double c = 0;
		double[] e = stats.getCardEntropy(hint.getHinted());
		Statistics stats1 = stats.getStatisticsIf(new Turn(hint,hint.getCardsToReveal(stats.getLastState())));
		double[] e1 = stats1.getCardEntropy(hint.getHinted());
		for (int i=0; i<e.length; i++)
			c+=e[i]-e1[i];
		return c;
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
	public Action discardSecure() {
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
			for (int i = 1; i < p.length; i++) {
				if (p[i] > max) {
					max = p[i];
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
		boolean confirm = false;
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
			if (args[i].equals("-c"))
				confirm = true;
		}
		AbstractAgent agent = new Strategy1Agent(log,logpath,confirm);
		Main.setAgent(agent);
		Main.main(args);
	}
}

