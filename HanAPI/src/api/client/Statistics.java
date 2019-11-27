package api.client;

import api.game.*;
import sjson.JSONException;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Questa classe mantiene per ogni carta in mano al giocatore una lista di possibili coppie valore-colore che la carta pu&ograve;
 * assumere.
 */
@SuppressWarnings({"unchecked"})
public class Statistics implements Cloneable
{
	private List<State> history;
	private List<Card> played; //Contiene le sole carte nei Fireworks. Se una carta viene giocata ma provoca un errore è da considerarsi scartata!
	private List<Card> discarded;
	private List<Card> ownedByOthers;
	private List<Hint>[][] hints;
	private Map<String,double[]> playbox;
	private Map<String,double[]> discbox;
	private Map<String,double[]> entrbox;

//	private List<Hint>[] hints;

	public Statistics()
	{
		history = new ArrayList<>();
		played = new ArrayList<>();
		discarded = new ArrayList<>();
		ownedByOthers = new ArrayList<>();
		hints = new List[Game.getInstance().getPlayers().length][Game.getInstance().getNumberOfCardsPerPlayer()];
		for (int i=0; i<hints.length; i++)
		{
			for (int j=0; j<hints[0].length; j++)
				hints[i][j] = new ArrayList<>();
		}
		playbox = new HashMap<>();
		discbox = new HashMap<>();
		entrbox = new HashMap<>();
	}

	public void addState(State state)
	{
		int expected = this.currentTurn();
		if(state.getOrder() == expected)
			history.add(state);
		else
			throw new IllegalStateException("Errore nell'attributo \"order\": mi aspetto "+expected+" e ho "+state.getOrder());
		if (expected == 0)
			firstState(state);
	}

	public List<Card> calcCards(String player, int i)
	{
		try {
			List<Card> list = Card.getAllCards();
			if (!player.equals(Main.playerName))
			{//devo riaggiungere alle carte possibili quelle possedute da player (che di default ho messo in ownedByOthers)
				for (Card card: getLastState().getHand(player))
					list.add(card);
			}
			for (Card c : played)
				list.remove(c);
			for (Card c : discarded)
				list.remove(c);
			for (Card c : ownedByOthers)
				list.remove(c);
			for (Hint h : hints[Game.getInstance().getPlayerTurn(player)][i])
				h.apply(list);
			return list;
		}
		catch(JSONException e)
		{
			return null;
		}
	}

	public Statistics clone()
	{
		try
		{
			super.clone();
		}
		catch(CloneNotSupportedException e){}
		Statistics clone = new Statistics();
		for (State state:history)
			clone.history.add(state.clone());
		for (Card card:played)
			clone.played.add(card.clone());
		for (Card card:discarded)
			clone.discarded.add(card.clone());
		for (Card card:ownedByOthers)
			clone.ownedByOthers.add(card.clone());
		for (int i=0; i<Game.getInstance().getPlayers().length; i++)
		{
			for (int j=0; j<Game.getInstance().getNumberOfCardsPerPlayer(); j++)
			{
				for (Hint h: hints[i][j])
					clone.hints[i][j].add(h.clone());
			}
		}
		return clone;
	}

	public int currentTurn()
	{
		return history.size();
	}

	public State getLastState()
	{
		return getState(currentTurn()-1);
	}

	public List<Hint>[] getHints(String player)
	{
		return hints[Game.getInstance().getPlayerTurn(player)];
	}

	/**
	 * Le Playability calcolate per gli altri giocatori non sono mai affidabili!!!
	 * @param player
	 * @return
	 */
	public double[] getPlayability(String player)
	{
		double[] p = playbox.get(player);
		if (p == null) {
			p = new double[getLastState().getHand(player).size()];
			List<Card> possibleCards;
			double cont;
			for (int i = 0; i < p.length; i++) {
				possibleCards = calcCards(player, i);
				cont = 0;
				for (Card card : possibleCards) {
					if (isPlayable(card))
						cont++;
				}
				p[i] = cont / possibleCards.size();
			}
			playbox.put(player,p);
		}
		return p;
	}

	public State getState(int turn)
	{
		return history.get(turn);
	}

	public double[] getUselessness(String player)
	{
		double[] u = discbox.get(player);
		if (u == null)
		{
			u = new double[getLastState().getHand(player).size()];
			List<Card> possibleCards;
			double cont;
			for (int i = 0; i < u.length; i++) {
				possibleCards = calcCards(player, i);
				cont = 0;
				for (Card card : possibleCards) {
					if (isUseless(card))
						cont++;
				}
				u[i] = cont / possibleCards.size();
			}
			discbox.put(player,u);
		}
		return u;
	}



	public double[] getCardEntropy(String player)
	{
		double[] e = entrbox.get(player);
		if (e == null) {
			e = new double[getLastState().getHand(player).size()];
			Map<Card, Double> prob;
			for (int i = 0; i < e.length; i++)
			{
				List<Card> possibleCards =calcCards(player, i);
				prob = fromCountToProb(possibleCards);
				e[i] = 0;
				for (double d : prob.values())
					e[i] -= d * Math.log(d);
				e[i] = e[i] / Math.log(2);
			}
			entrbox.put(player,e);
		}
		return e;
	}

	public Statistics getStatisticsIf(Turn turn)
	{
		Statistics stats = this.clone();
		stats.updateTurn(turn);
		return stats;
	}

	private Map<Card,Double> fromCountToProb(List<Card> list)
	{
		HashMap<Card,Double> map = new HashMap();
		Card card;
		double tot = list.size();
		double cont;
		while(list.size()>0)
		{
			cont = 0;
			card = list.get(0);
			while(list.remove(card))
				cont++;
			map.put(card,cont/tot);
		}
		return map;
	}

/*	public void printPossibilities(PrintStream out)
	{
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		out.println("[POSSIBILITIES]:");
		for (int i=0; i<Game.getInstance().getNumberOfCardsPerPlayer(); i++)
		{
			List<Card> l = calcCards(i);
			out.print(i+":\t");
			Card card;
			for (Color color:Color.values())
			{
				for (int j=1; j<6; j++)
				{
					try {
						card = new Card(color,j);
						card.setValueRevealed(true).setColorRevealed(true);
						out.print(card+" = "+df.format((double)countCard(card,l)/l.size())+"\n\t");
					}
					catch (JSONException e){}
				}
			}
			out.println();
		}
	}
*/
	/**
	 * Aggiorna le carte possibili in funzione del Turn ricevuto.
	 * Ricorda che si ottiene un oggetto Turn solo in seguito ad una mossa degli altri giocatori
	 * @param turn
	 */
	public void updateTurn(Turn turn)
	{
//		System.out.println("[TURN]: "+turn);
		Card drawn = turn.getDrawn();
//		System.out.println("[DRAWN]: "+drawn);
		Action action = turn.getAction();
		int p = Game.getInstance().getPlayerTurn(action.getPlayer());
		playbox.clear();
		entrbox.clear();
		discbox.clear();
		if (action.getType() == ActionType.PLAY)
		{
			//Controllo se la carta è giocata con successo
			Card old_card = turn.getCard();
			if (getLastState().getFirework(old_card.getColor()).peak()+1 == old_card.getValue())
			{
				played.add(old_card);
			}
			else
			{
				discarded.add(old_card);
			}

			//Aggiorno gli hint del giocatore che ha giocato
			int i;
			for (i=action.getCard(); i<getLastState().getHand(action.getPlayer()).size()-1; i++)
			{
				hints[p][i] = hints[p][i+1];
			}
			hints[p][i] = new ArrayList<>();
			/*if (drawn!=null)
				hints[p][i] = new ArrayList<>();
			else
				hints[p][i] = null;*/

			//Se il giocatore non sono io aggiorno ownedByOthers
			if (!action.getPlayer().equals(Main.playerName))
			{//Tolgo la vecchia carta dalla lista di carte possedute dagli altri e aggiungo la nuova
				ownedByOthers.remove(old_card);
				if (drawn!=null)
					ownedByOthers.add(drawn);
			}
		}
		else if (action.getType() == ActionType.DISCARD)
		{
			//Scarto la carta
			Card old_card = turn.getCard();
			discarded.add(old_card);

			//Aggiorno gli hint del giocatore che ha giocato
			int i;
			for (i=action.getCard(); i<getLastState().getHand(action.getPlayer()).size()-1; i++)
			{
				hints[p][i] = hints[p][i+1];
			}
			hints[p][i] = new ArrayList<>();
	/*		if (drawn!=null)
				hints[p][i] = new ArrayList<>();
			else
				hints[p][i] = null;*/

			//Se il giocatore non sono io aggiorno ownedByOthers
			if (!action.getPlayer().equals(Main.playerName))
			{//Tolgo la vecchia carta dalla lista di carte possedute dagli altri e aggiungo la nuova
				ownedByOthers.remove(old_card);
				if (drawn!=null)
					ownedByOthers.add(drawn);
			}
		}
		else
		{ //Nel caso in cui il turn rappresenti un suggerimento, lo aggiungo al giocatore cui è rivolto
			Hand myHand = getLastState().getHand(Main.playerName);
			p = Game.getInstance().getPlayerTurn(action.getHinted());
			if (action.getType() == ActionType.HINT_COLOR)
			{
				for (int i=0; i<myHand.size(); i++)
					hints[p][i].add(new Hint(turn.getRevealed().contains(i),action.getColor()));
			}
			else
			{
				for (int i=0; i<myHand.size(); i++)
					hints[p][i].add(new Hint(turn.getRevealed().contains(i),action.getValue()));
			}

		}
	}


	private int countCard(Card card, List<Card> list)
	{
		int count = 0;
		for(Card c:list)
		{
			if (card.equals(c))
				count++;
		}
		return count;
	}

	/**
	 * Inizializza le possibili carte del giocatore guardando quelle possedute dagli altri.
	 * @param state lo stato iniziale del gioco
	 */
	private void firstState(State state)
	{
		for (String name:Game.getInstance().getPlayers())
		{
			if (!name.equals(Main.playerName))
			{
				for (Card card: state.getHand(name))
					ownedByOthers.add(card);
			}
		}

//		System.out.println("[FIRSTSTATE]: ownedByOthers: "+ownedByOthers.size());

/*		for (int i=0; i<possibleCards.length; i++)
			initCards(i);

 */
	}

/*	/**
	 * @param i la posizione nella mano del giocatore della carta da resettare
	 */
/*	private void initCards(int i)
	{
//		System.out.println("[InitCards]: ownedByOthers: "+ownedByOthers.size());
		try {
			possibleCards[i] = Card.getAllCards();
		}
		catch(JSONException e){}

		for(Card c:played)
			possibleCards[i].remove(c);
		for(Card c:discarded)
			possibleCards[i].remove(c);
		for(Card c:ownedByOthers)
			possibleCards[i].remove(c);
	}
*/
	public boolean isPlayable(Card card)
	{
		State last = getLastState();
	//	System.out.println("Checking if "+card+" is playable");
		Firework fire = last.getFirework(card.getColor());
		return (card.getValue() == fire.peak()+1);
	}

	public boolean isUseless(Card card)
	{
		int count = countCard(card,played);
		if (count > 0) //Se la carta è già stata giocata allora è inutile
			return true;

		count = count + countCard(card,discarded);

		/*
			Se ho scartato card.getCount()-1 carte uguali a questa significa che questa è l'ultima che può essere giocata
			e quindi NON è inutile
		 */
		return (card.getCount()-1!=count);
	}

	public static void maintainColor(Color color, List<Card> list)
	{
		for (int i=0; i<list.size(); i++)
		{
			if (list.get(i).getColor() != color)
			{
				list.remove(i);
				i--;
			}
		}
	}

	public static void maintainValue(int value, List<Card> list)
	{
		for (int i=0; i<list.size(); i++)
		{
			if (list.get(i).getValue() != value)
			{
				list.remove(i);
				i--;
			}
		}
	}

	public static void removeColor(Color color, List<Card> list)
	{
		for (int i=0; i<list.size(); i++)
		{
			if (list.get(i).getColor() == color)
			{
				list.remove(i);
				i--;
			}
		}
	}

	public static void removeValue(int value, List<Card> list)
	{
		for (int i=0; i<list.size(); i++)
		{
			if (list.get(i).getValue() == value)
			{
				list.remove(i);
				i--;
			}
		}
	}

}
