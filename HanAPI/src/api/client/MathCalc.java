package api.client;

import api.game.Card;
import api.game.Color;
import api.game.Game;
import api.game.State;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public class MathCalc
{

	private static List<Card> cards = generateCards();

	private static List<Card> generateCards()
	{
		try
		{
			Card[] cards = {
					new Card(Color.BLUE,1),new Card(Color.BLUE,1), new Card(Color.BLUE,1),
					new Card(Color.BLUE,2),new Card(Color.BLUE,2),new Card(Color.BLUE,3),new Card(Color.BLUE,3),
					new Card(Color.BLUE,4),new Card(Color.BLUE,4),new Card(Color.BLUE,5),
					new Card(Color.RED,1),new Card(Color.RED,1), new Card(Color.RED,1),
					new Card(Color.RED,2),new Card(Color.RED,2),new Card(Color.RED,3),new Card(Color.RED,3),
					new Card(Color.RED,4),new Card(Color.RED,4),new Card(Color.RED,5),
					new Card(Color.GREEN,1),new Card(Color.GREEN,1), new Card(Color.GREEN,1),
					new Card(Color.GREEN,2),new Card(Color.GREEN,2),new Card(Color.GREEN,3),new Card(Color.GREEN,3),
					new Card(Color.GREEN,4),new Card(Color.GREEN,4),new Card(Color.GREEN,5),
					new Card(Color.WHITE,1),new Card(Color.WHITE,1), new Card(Color.WHITE,1),
					new Card(Color.WHITE,2),new Card(Color.WHITE,2),new Card(Color.WHITE,3),new Card(Color.WHITE,3),
					new Card(Color.WHITE,4),new Card(Color.WHITE,4),new Card(Color.WHITE,5),
					new Card(Color.YELLOW,1),new Card(Color.YELLOW,1), new Card(Color.YELLOW,1),
					new Card(Color.YELLOW,2),new Card(Color.YELLOW,2),new Card(Color.YELLOW,3),new Card(Color.YELLOW,3),
					new Card(Color.YELLOW,4),new Card(Color.YELLOW,4),new Card(Color.YELLOW,5)
			};
			return Arrays.asList(cards);
		}
		catch(JSONException e){return null;}
	}

	/*
	public static int countDiscardedCards(State state, Color color, int value)
	{
		JSONArray discards = state.getDiscards();
		int cont=0;
		Card box;
		for (JSONData d:discards)
		{
			box = (Card)d;
			if (color!=null && color !=box.getColor())
				continue;
			if (value>0 && value!=box.getValue())
				continue;
			cont++;
		}
		return cont;
	}
	*/

	private static int count(List<Card> cards, Card counted)
	{
		int cont = 0;
		for (Card c:cards)
		{
			if (c.equals(counted))
				cont++;
		}
		return cont;
	}

	private static int count(List<Card> cards, int counted_value)
	{
		int cont = 0;
		for (Card c:cards)
		{
			if (c.getValue() == counted_value)
				cont++;
		}
		return cont;
	}

	private static int count(List<Card> cards, Color counted_color)
	{
		int cont = 0;
		for (Card c:cards)
		{
			if (counted_color == c.getColor())
				cont++;
		}
		return cont;
	}

	private static List<Card> preserveCards(List<Card> cards, Color... colors)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		boolean remove;
		for (int i=0; i<l.size(); i++)
		{
			remove = true;
			for (int j=0; j<colors.length && remove; j++)
				remove = colors[j]!=null && l.get(i).getColor()!=colors[j];

			if (remove) {
				l.remove(i);
				i--;
			}
		}


		return l;
	}

	private static List<Card> preserveCards(List<Card> cards, int... values)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		boolean remove;
		for (int i=0; i<l.size(); i++)
		{
			remove = true;
			for (int j=0; j<values.length && remove; j++)
				remove = values[j]>0 && l.get(i).getValue()!=values[j];

			if (remove) {
				l.remove(i);
				i--;
			}
		}

		return l;
	}

	private static List<Card> preserveCards(List<Card> cards, JSONArray othercards)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		for (JSONData d: othercards)
			l.remove(d);
		return l;
	}

	private static List<Card> removeCards(List<Card> cards, Color... colors)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		boolean remove;
		for (int i=0; i<l.size(); i++)
		{
			remove = false;
			for (int j=0; j<colors.length && !remove; j++)
				remove = colors[j]==null || l.get(i).getColor()==colors[j];

			if (remove) {
				l.remove(i);
				i--;
			}
		}
		return l;
	}

	private static List<Card> removeCards(List<Card> cards, int... values)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		boolean remove;
		for (int i=0; i<l.size(); i++)
		{
			remove = false;
			for (int j=0; j<values.length && !remove; j++)
				remove = values[j]==0 || l.get(i).getValue()==values[j];

			if (remove) {
				l.remove(i);
				i--;
			}
		}
		return l;
	}

	private static List<Card> removeCards(List<Card> cards, JSONArray othercards)
	{
		ArrayList<Card> l = new ArrayList<>(cards);
		for (JSONData d: othercards)
			l.remove(d);
		return l;
	}

	public static List<Card> getAllCards()
	{
		return new ArrayList<>(cards);
	}

	/**
	 * Restituisce la probabilit&agrave; che la carta indicata sia giocabile.
	 * @param state
	 * @param card
	 * @param owner
	 * @return
	 */
	public static double getCardPlayability(State state, int card, String owner)
	{
		List<Card> possible = getPossibleCards(state,card,owner); //Lista di possibili carte in base a quello che conosco di colore e valore
		double den = possible.size();
		double num = 0;
		//Elimino tutte le carte che so che non devono essere giocate
		for (Color color: Color.values())
		{
			if (state.getFirework(color).peak() < 5)
				num += preserveCards(preserveCards(possible,color),state.getFirework(color).peak()+1).size();
		}
		return num/den;
	}

	public static double getCardUselessness(State state, int card, String owner)
	{
		// Cerco la lista di possibili carte in base a quello che conosco di colore e valore.
		List<Card> possible = getPossibleCards(state, card, owner);
		double den = possible.size();
		double num = 0;
		List<Card> l;
		for (Card c:possible)
		{
			l = new ArrayList<>(possible);
			l.remove(c);
			if (l.contains(c))
				num++;
			else
			{
				for(JSONData d: Game.getInstance().getPlayers())
				{
					if (!d.toString().equals(owner) && state.getHand(d.toString()).has(c))
					{
						num++;
						break;
					}
				}
			}
		}
		/* Per ognuna delle carte possibili devo vedere se ne esiste una copia (anche tra le carte in mano ai miei compagni!).
		 * Se esiste allora è inutile altrimenti no. Conta le inutili e dividi per le possibili
		 */
		return num/den;
	}

	/**
	 * Indicata una carta in mano ad un qualsiasi giocatore, restituisce l'insieme delle possibili carte che quella indicata può essere.
	 * L'insieme restituito tiene conto anche dei doppioni, quindi è possibile trovarci ripetizioni della stessa carta.
	 * @param state
	 * @param card
	 * @param owner
	 * @return
	 */
	public static List<Card> getPossibleCards(State state, int card, String owner) //TODO OTTIMIZZA!!!!
	{
		Card c = state.getHand(owner).getCard(card);
		Color color = c.getColor();
		int value = c.getValue();

		List<Card> l = getAllCards();

		l = preserveCards(l,color);
		l = preserveCards(l,value);
		l = removeCards(l,state.getDiscards());
		for(JSONData d: Game.getInstance().getPlayers())
		{
			if (!d.toString().equals(owner))
				l = removeCards(l,state.getHand(d.toString()));
		}
		for (Color co: Color.values())
			l = removeCards(l,state.getFirework(co));

		return l;
	}
}

