package game;

import agents.Agent;
import main.HanabiServer;
import sjson.JSONObject;
import sjson.JSONObjectConvertible;

import java.util.Stack;

/**
 * Classe che rappresenta una carta. Questa classe è usata solo all'interno del server, il che giustifica la presenza dei metodi
 * getter per colore e valore in quanto non accessibili dagli agenti. I metodi toString() e toJSON() invece richiedono lo stato
 * corrente a HanabiServer.instance e mostrano le informazioni di conseguenza.
 */
public class Card implements Cloneable, JSONObjectConvertible
{
	private Color color;//the card's color
	private int value;//the number on the card
	private boolean valueRevealed = false;
	private boolean colorRevealed = false;
	private Agent owner = null; //null = Deck

	/**
	 * Costruisce una carta di colore e valore dati
	 * @throws IllegalArgumentException se il valore della carta non è compreso tra 1 e 5 inclusi
	 **/
	public Card(Color c, int val) throws IllegalArgumentException{
		if(val<1 || val>5) throw new IllegalArgumentException("Card value out of range");
		color = c;
		value = val;
	}

	/**
	 * Crea una copia
	 * @return una copia della carta.
	 * @see Cloneable
	 */
	public Card clone()
	{
		Card c;
		try
		{
			c = (Card)super.clone();
			c.color = this.color;
			c.value = this.value;
			c.colorRevealed = this.colorRevealed;
			c.valueRevealed = this.valueRevealed;
			c.owner = this.owner;
		}
		catch(CloneNotSupportedException e)
		{
			c = null;
		}
		return c;
	}

	/**
	 * Un giocatore non deve poter accedere a questo metodo.
	 * @return Il colore della carta come oggetto Color.
	 */
	public Color getColor(){return color;}

	/**
	 * Un giocatore non deve poter accedere a questo metodo.
	 * @return Il valore della carta come int.
	 */
	public int getValue(){return value;}

	/**
	 *@return Il numero di volte che la carta compare nel mazzo
	 */
	public int getCount(){return (value==1?3:(value<5?2:1));}

	/**
	 * @param s lo stato corrente
	 * @return restituisce true se allo stato corrente il colore di questa carta va mostrato, false altrimenti
	 */
	public boolean haveToShowColor(State s)
	{
		return owner==null || !(s.getCurrentPlayer()>-1 && s.getPlayers()[s.getCurrentPlayer()]==owner && !colorRevealed);
	}

	/**
	 * @param s lo stato corrente
	 * @return restituisce true se allo stato corrente il valore di questa carta va mostrato, false altrimenti
	 */
	public boolean haveToShowValue(State s)
	{
		return owner==null || !(s.getCurrentPlayer()>-1 && s.getPlayers()[s.getCurrentPlayer()]==owner && !valueRevealed);
	}

	/**
	 * Rivela il colore della carta. Una volta usato questo metodo il possessore della carta ne può vedere il colore.
	 */
	public void revealColour()
	{
		colorRevealed = true;
	}

	/**
	 * Rivela il valore della carta. Una volta usato questo metodo il possessore della carta ne può vedere il valore.
	 */
	public void revealValue()
	{
		valueRevealed = true;
	}

	/**
	 * Permette di impostare il possessore della carta
	 * @param a Il giocatore che possiede la carta
	 */
	public void setOwner(Agent a) {
		owner = a;
	}

	@Override
	public JSONObject toJSON()
	{
		State s = HanabiServer.instance.getCurrentState();
		JSONObject obj = new JSONObject();
		String c="",v="";
		if (haveToShowColor(s))
			c = ""+color.toString();
		if (haveToShowValue(s))
			v = ""+value;
		obj.put("color",c);
		obj.put("value",v);
		return obj;
	}

	/**
	 * Restituisce le informazioni relative alla carta come testo. Per sapere se nascondere colore o numero ottiene lo stato corrente
	 * da HanabiServer.
	 * @return la rappresentazione testuale della carta
	 */
	public String toString()
	{
		State s = HanabiServer.instance.getCurrentState();
		StringBuilder sb = new StringBuilder();
		if (haveToShowColor(s))
			sb.append(color);
		else
			sb.append("   ");
		sb.append("-");
		if (haveToShowValue(s))
			sb.append(value);
		else
			sb.append("   ");
		return sb.toString();
	}

	/**
	 * Crea un nuovo array che rappresenta un mazzo
	 * @return Un array di Card corrispondente al mazzo standard di Hanabi.
	 **/
	public static Card[] getDeck(){return deck.clone();}

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato invertendo 2 carte random nel mazzo per 1000 volte
	 * @return Uno Stack di Card in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	public static Stack<Card> shuffledDeck(){
		Card[] deck = getDeck();
		java.util.Random r = new java.util.Random();
		for(int i = 0; i<1000; i++){
			int a = r.nextInt(50);
			int b = r.nextInt(50);
			Card c = deck[a];
			deck[a]= deck[b];
			deck[b]=c;
		}
		Stack<Card> shuffle = new Stack<Card>();
		for(Card c: deck) shuffle.push(c);
		return shuffle;
	}

	/**
	 * Due Card sono uguali se lo sono i loro colori e valori
	 **/
	public boolean equals(Object o){
		if(o!=null && o instanceof Card){
			Card c = (Card)o;
			return c.color == color && c.value==value;
		}
		return false;
	}
/*
	public int hashCode(){
		return 5* color.ordinal()+value;
	}
*/

	private static Card[] deck = {
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


}


