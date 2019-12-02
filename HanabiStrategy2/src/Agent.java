import api.client.*;
import api.game.*;
import sjson.JSONException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	 *         Se ho hint token dai il suggerimento (a qualsiasi giocatore) che diminuisce del massimo l'entropia delle carte.
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
		try {
			Action action = null;
			int tokens = stats.getLastState().getHintTokens();
			if (tokens > 0) {
				action = hintForPlay();
				if (action == null)
					action = securePlay();
			}
			if (action == null && tokens < 8)
				action = secureDiscard();
			if (action == null && tokens > 0)
				action = bestHint();
			if (action == null)
				action = securePlay();
			if (action == null)
				action = discardBest();
			return action;
		}catch(JSONException e)
		{
			log(e);
			return null;
		}
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

	private Action hintForPlay() throws JSONException
	{
		Action hint = null;
		for(String comrade: sortPlayers())
		{
			log("HintForPlay: "+comrade);
			if (hint != null)
				break;
			Hand hand = stats.getLastState().getHand(comrade);
			ArrayList<Integer> playableCards = new ArrayList<>();
			for (int i=0; i<hand.size(); i++)
			{
				if (stats.isPlayable(hand.getCard(i)) && stats.getPlayability(comrade)[i]<1)
					playableCards.add(i);
			}
			log("\tCarte giocabili: "+playableCards);

			if (playableCards.size() == 0)
				continue;

			int indexplay = 0;
			if (playableCards.size() > 1)
			{
				int[] cont = new int[playableCards.size()]; //Per ogni carta, contatore di quante carte sarebbero rese giocabili
				Arrays.fill(cont,0);
				for (int i = 0; i<playableCards.size(); i++)
				{
					Card card = hand.getCard(playableCards.get(i));
					if (card.getValue()<5) //Rende giocabili altre carte
					{
						Card newplayable = new Card(card.getColor(),card.getValue()+1);
						for (String s: sortPlayers())
							cont[i] = cont[i] + countCardsInHand(newplayable,s);
					}
					log("\t\tLa carta "+playableCards.get(i)+" ne renderebbe giocabili altre "+cont[i]);
				}
				for (int i=1; i<cont.length; i++)
				{
					if (cont[i]>cont[indexplay])
						indexplay = i;
				}
			}
			int hintedCard = playableCards.get(indexplay);
			Card card = hand.getCard(hintedCard);
			//So quale carta devo suggerire a quale giocatore ma non so se suggerirne il colore o il valore
			//Decido in base all'aumento di playability
			double p = stats.getPlayability(comrade)[hintedCard]; //playability attuale
			double p_v,p_c;
			Action h_v = new Action(Main.playerName,comrade,card.getValue());
			Statistics box = stats.getStatisticsIf(new Turn(h_v,h_v.getCardsToReveal(stats.getLastState())));
			p_v = box.getPlayability(comrade)[hintedCard]-p;
			Action h_c = new Action(Main.playerName,comrade,card.getColor());
			box = stats.getStatisticsIf(new Turn(h_c,h_c.getCardsToReveal(stats.getLastState())));
			p_c = box.getPlayability(comrade)[hintedCard]-p;

			if (p_v>p_c)
				hint = h_v;
			else if (p_c>p_v)
				hint = h_c;
			else if (p_v != 0)
				hint = h_v;
		}

		return hint;
	}

	private Action securePlay() throws JSONException
	{
		ArrayList<Integer> playable = new ArrayList<>();
		Hand hand = stats.getLastState().getHand(Main.playerName);
		for (int i=0; i<hand.size(); i++)
		{
			if (stats.getPlayability(Main.playerName)[i]==1)
				playable.add(i);
		}
		if (playable.size()==0)
			return null;
		int p = 0;
		if (playable.size()>1)
		{
			int[] cont = new int[playable.size()]; //Per ogni carta giocabile, contatore di quante carte sarebbero rese giocabili
			Arrays.fill(cont,0);
			for (int i=0; i<playable.size(); i++)
			{
				Card card = hand.getCard(playable.get(i));
				if (card.getValue()<5) //Rende giocabili altre carte
				{
					Card newplayable = new Card(card.getColor(),card.getValue()+1);
					for (String s: Game.getInstance().getPlayers())
						cont[i]+=countCardsInHand(newplayable,s);
				}
			}
			for (int i=1; i<cont.length; i++)
			{
				if (cont[i]>cont[p])
					p = i;
			}
		}
		return new Action(Main.playerName,ActionType.PLAY,playable.get(p));
	}

	private Action secureDiscard() throws JSONException
	{
		double[] u = stats.getUselessness(Main.playerName);
		for (int i=0; i<stats.getLastState().getHand(Main.playerName).size(); i++)
		{
			if (u[i] == 1)
				return new Action(Main.playerName,ActionType.DISCARD,i);
		}
		return null;
	}

	private Action bestHint() throws JSONException
	{
		Action best = null;
		double best_e = 0;
		for (String comrade:sortPlayers())
		{
			double ae = 0; //somma attuale delle entropie della carte di comrade
			for (double d:stats.getCardEntropy(comrade))
				ae += d;
			double e; //appoggio per nuove entropie dovute al suggerimento
			for (Action hint:getPossibleHints(comrade))
			{
				Statistics box = stats.getStatisticsIf(new Turn(hint,hint.getCardsToReveal(stats.getLastState())));
				e = 0;
				for (double d:box.getCardEntropy(comrade))
					e += d;

				if (ae-e>best_e)
				{
					best_e = ae-e;
					best = hint;
				}
			}
		}
		return best;
	}

	private Action discardBest() throws JSONException
	{
		int best = 0;
		double[] u = stats.getUselessness(Main.playerName);
		for (int i=0; i<stats.getLastState().getHand(Main.playerName).size(); i++)
		{
			if (u[i] > u[best])
				best = i;
		}
		return new Action(Main.playerName,ActionType.DISCARD,best);
	}

	private int countCardsInHand(Card card, String player)
	{
		int cont = 0;
		Hand hand = stats.getLastState().getHand(player);
//		log(card.toString());
		for (Card c: hand)
		{
//			log(c.toString());
			if (c.equals(card))
				cont++;

		}
		return cont;
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
