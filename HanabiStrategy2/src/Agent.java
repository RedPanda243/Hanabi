import api.client.AbstractAgent;
import api.client.Main;
import api.client.StatisticState;
import api.game.*;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class Agent extends AbstractAgent
{
	public Agent(boolean log, String logpath) throws FileNotFoundException
	{
		super(log,logpath);
	}

	public void notifyTurn(Turn turn)
	{
		super.notifyTurn(turn);
		log(turn+"\n");
	}

	/**
	 * <ol>
	 *     <li>
	 *         Se ho hint token, controllo gli altri giocatori: se uno ha una carta giocabile non sicura dagli il
	 *         suggerimento più significativo (in termini di diminuzione dell'entropia della mano) che coinvolge quella carta.
	 *         Nel caso in cui il giocatore avesse più carte giocabili non sicure indicagli quella che, se giocata, rende
	 *         giocabili il maggior numero di carte possedute dai giocatori.
	 *     </li>
	 *     <li>
	 *         Se ho hint token e ho una carta giocabile sicura la gioco. Nel caso in cui avessi più carte giocabili gioco
	 *         quella che rende giocabili il maggior numero di carte possedute dai giocatori.
	 *     </li>
	 *     <li>
	 *         Se non ho hint token massimi (8) e ho una carta sicura da scartare la scarto.
	 *         Mi piacerebbe fare in modo che nel caso in cui avessi più carte scartabili scarto quella pi&ugrave; lontana
	 *         dall'attuale picco del firework dello stesso colore, ma per farlo dovrei conoscere valore e colore della carta
	 *         che non è scontato!
	 *     </li>
	 *     <li>
	 *         Se ho hint token dai il suggerimento (a qualsiasi giocatore) che in diminuisce del massimo l'entropia delle carte.
	 *     </li>
	 *     <li>
	 *         Se ho una carta giocabile sicura la gioco. Vedi punto 2 per casi di molteplicità di carte.
	 *     </li>
	 *     <li>
	 *         Scarta la carta con uselessness maggiore.
	 *         Anche qui mi piacerebbe fare come nel punto 3.
	 *     </li>
	 * </ol>
	 * @return
	 */
	@Override
	public Action chooseAction()
	{
		Action action = null;
		int tokens = stats.getLastState().getHintTokens();
		if (tokens>0)
		{
			action = hintForPlay();
			if (action == null)
				action = securePlay();
		}
		if (action == null && tokens<8)
			action = secureDiscard();
		if (action == null && tokens>0)
			action = bestHint();
		if (action == null)
			action = securePlay();
		if (action == null)
			action = discardBest();
		return action;
	}

	public void notifyState(State state)
	{
		super.notifyState(state);
		try {
			StatisticState sstate = new StatisticState(state, stats);
			log(""+sstate);
		}
		catch (JSONException e){}
	}

	private Action hintForPlay()
	{
		Action hint = null;
		for(String comrade: sortPlayers())
		{
			Hand hand = stats.getLastState().getHand(comrade);
			ArrayList<Integer> playableCards = new ArrayList<>();
			for (int i=0; i<hand.size(); i++)
			{
				if (stats.isPlayable(hand.getCard(i)) && stats.getPlayability(comrade)[i]<1)
					playableCards.add(i);
			}
			if (playableCards.size() == 0)
				continue;
			if (playableCards.size() > 1)
			{
				int[] cont = new int[playableCards.size()];
				Arrays.fill(cont,0);
				for (int i: playableCards)
				{
					Card card = hand.getCard(i);
					if (card.getValue()<5)
					{

					}
				}
			}
		}
		return hint;
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
		AbstractAgent agent = new Agent(log,logpath);
		Main.setAgent(agent);
		Main.main(args);
	}
}
