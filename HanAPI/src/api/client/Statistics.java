package api.client;

import api.game.*;
import sjson.JSONException;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Questa classe mantiene per ogni carta in mano al giocatore una lista di possibili coppie valore-colore che la carta pu&ograve;
 * assumere.
 */
@SuppressWarnings({"unchecked"})
public class Statistics
{
	private List<State> history;
	private List<Card>[] possibleCards;
	private List<Card> played; //Contiene le sole carte nei Fireworks. Se una carta viene giocata ma provoca un errore è da considerarsi scartata!
	private List<Card> discarded;
	private List<Card> ownedByOthers;

	public Statistics()
	{
		history = new ArrayList<>();
		possibleCards = new List[Game.getInstance().getNumberOfCardsPerPlayer()];
		played = new ArrayList<>();
		discarded = new ArrayList<>();
		ownedByOthers = new ArrayList<>();
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

	public int currentTurn()
	{
		return history.size();
	}

	public State getLastState()
	{
		return getState(currentTurn()-1);
	}

	public double[] getPlayability(String player)
	{
		Hand hand = getLastState().getHand(player);
		double[] p = new double[hand.size()];
		if (player.equals(Main.playerName))
		{
			double cont;
			for (int i = 0; i < p.length; i++) {
				cont = 0;
				for (Card card : possibleCards[i]) {
					if (isPlayable(card))
						cont++;
				}
				p[i] = cont / possibleCards[i].size();
			}
		}
		else
		{
			for (int i=0; i<p.length; i++)
			{
				if (isPlayable(hand.getCard(i)))
					p[i] = 1;
				else
					p[i] = 0;
			}
		}
		return p;
	}

	public State getState(int turn)
	{
		return history.get(turn);
	}

	public double[] getUselessness(String player)
	{
		Hand hand = getLastState().getHand(player);
		double[] u = new double[hand.size()];
		if (player.equals(Main.playerName))
		{
			double cont;
			for (int i = 0; i < u.length; i++) {
				cont = 0;
				for (Card card : possibleCards[i]) {
					if (isUseless(card))
						cont++;
				}
				u[i] = cont / possibleCards[i].size();
			}
		}
		else
		{
			for (int i=0; i<u.length; i++)
			{
				if (isUseless(hand.getCard(i)))
					u[i] = 1;
				else
					u[i] = 0;
			}
		}
		return u;
	}

	public void printPossibilities(PrintStream out)
	{
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		out.println("[POSSIBILITIES]:");
		for (int i=0; i<possibleCards.length; i++)
		{
			out.print(i+":\t");
			Card card;
			for (Color color:Color.values())
			{
				for (int j=1; j<6; j++)
				{
					try {
						card = new Card(color,j);
						card.setValueRevealed(true).setColorRevealed(true);
						out.print(card+" = "+df.format((double)countCard(card,possibleCards[i])/possibleCards[i].size())+"\n\t");
					}
					catch (JSONException e){}
				}
			}
			out.println();
		}
	}

	/**
	 * Aggiorna le carte possibili in funzione del Turn ricevuto.
	 * Ricorda che si ottiene un oggetto Turn solo in seguito ad una mossa degli altri giocatori
	 * @param turn
	 */
	public void updateTurn(Turn turn)
	{
		System.out.println("[TURN]: "+turn);
		Card drawn = turn.getDrawn();
		Action action = turn.getAction();
		if (drawn!=null)
		{
			Card old_card = getLastState().getHand(action.getPlayer()).getCard(action.getCard());

			//Controllo che la vecchia carta sia stata giocata e sia legittima. Se non lo è la aggiungo alle carte scartate
			if (action.getType() == ActionType.PLAY && getLastState().getFirework(old_card.getColor()).peak() == old_card.getValue()+1)
			{
				played.add(old_card);
			}
			else
			{
				discarded.add(old_card);
			}

			//Tolgo la vecchia carta dalla lista di carte possedute dagli altri e aggiungo la nuova
			ownedByOthers.remove(old_card);
			ownedByOthers.add(drawn);
		}
		else if (action.getHinted().equals(Main.playerName))
		{ //Nel caso in cui il turn rappresenti un suggerimento, se è rivolto a me aggiorno le carte possibili
			Hand myHand = getLastState().getHand(Main.playerName);
			if (action.getType() == ActionType.HINT_COLOR)
			{
				for (int i=0; i<myHand.size(); i++)
				{
					if (turn.getRevealed().contains(i))
						maintainColor(action.getColor(),possibleCards[i]);
					else
						removeColor(action.getColor(),possibleCards[i]);
				}
			}
			else
			{
				for (int i=0; i<myHand.size(); i++)
				{
					if (turn.getRevealed().contains(i))
						maintainValue(action.getValue(),possibleCards[i]);
					else
						removeValue(action.getValue(),possibleCards[i]);
				}
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

		for (int i=0; i<possibleCards.length; i++)
			initCards(i);
	}

	/**
	 * @param i la posizione nella mano del giocatore della carta da resettare
	 */
	private void initCards(int i)
	{
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

	private boolean isPlayable(Card card)
	{
		State last = getLastState();
		Firework fire = last.getFirework(card.getColor());
		return (card.getValue() == fire.peak()+1);
	}

	private boolean isUseless(Card card)
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

	private void maintainColor(Color color, List<Card> list)
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

	private void maintainValue(int value, List<Card> list)
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

	private void removeColor(Color color, List<Card> list)
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

	private void removeValue(int value, List<Card> list)
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
