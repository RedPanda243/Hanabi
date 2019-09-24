package game;

import api.game.Card;
import api.game.Color;
import main.HanabiServer;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONString;

import java.util.Stack;

/**
 * Classe che rappresenta una carta. Questa classe è usata solo all'interno del server, il che giustifica la presenza dei metodi
 * getter per colore e valore in quanto non accessibili dagli agenti. I metodi toString() e toJSON() invece richiedono lo stato
 * corrente a HanabiServer.instance e mostrano le informazioni di conseguenza.
 */
public final class ServerCard extends Card
{
//	private Agent owner = null; //null = Deck

	public ServerCard(JSONObject obj) throws JSONException
	{
		super(obj);
		if (getColor() == null || getValue()==0)
			throw new JSONException("Color and value must be known");
	}

	/**
	 * Crea una copia
	 * @return una copia della carta.
	 * @see Cloneable
	 */
	public ServerCard clone()
	{
		ServerCard c = (ServerCard) super.clone();
//		c.owner = owner;
		return c;
	}

	/**
	 * @param s lo stato corrente.
	 * @return restituisce true se allo stato corrente il colore di questa carta va mostrato, false altrimenti
	 */
/*	public boolean haveToShowColor(ServerState s)
	{
		return owner==null || !(s.getCurrentPlayer()>-1 && s.getPlayers()[s.getCurrentPlayer()]==owner && !colorRevealed);
	}

	/**
	 * @param s lo stato corrente
	 * @return restituisce true se allo stato corrente il valore di questa carta va mostrato, false altrimenti
	 */
/*	public boolean haveToShowValue(ServerState s)
	{
		return owner==null || !(s.getCurrentPlayer()>-1 && s.getPlayers()[s.getCurrentPlayer()]==owner && !valueRevealed);
	}
*/
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
/*	public void setOwner(Agent a) {
		owner = a;
	}
 */

	/**
	 * Restituisce le informazioni relative alla carta come testo.
	 * @return la rappresentazione testuale della carta
	 */
	public String toString()
	{
		/*
		ServerState s = HanabiServer.instance.getCurrentState();
		StringBuilder sb = new StringBuilder();
		if (haveToShowColor(s))
			sb.append(getColor());
		else
			sb.append("   ");
		sb.append("-");
		if (haveToShowValue(s))
			sb.append(getValue());
		else
			sb.append("   ");
		return sb.toString();
		 */
		return getColor()+"("+isColorRevealed()+")-"+getValue()+"("+isValueRevealed()+")";
	}

	/**
	 * Crea un nuovo array che rappresenta il mazzo. I riferimenti contenuti nei due array puntano alle stesse ServerCard
	 * @return Un array di ServerCard corrispondente al mazzo standard di Hanabi.
	 **/
	public static ServerCard[] getDeck(){return deck.clone();}

	/**
	 * Il mescolamento delle carte del mazzo &egrave; simulato invertendo 2 carte random nel mazzo per 1000 volte
	 * @return Uno Stack di ServerCard in ordine casuale rappresentante un mazzo di carte mescolato.
	 **/
	public static Stack<ServerCard> shuffledDeck(){
		ServerCard[] deck = getDeck();
		java.util.Random r = new java.util.Random();
		for(int i = 0; i<1000; i++){
			int a = r.nextInt(50);
			int b = r.nextInt(50);
			ServerCard c = deck[a];
			deck[a]= deck[b];
			deck[b]=c;
		}
		Stack<ServerCard> shuffle = new Stack<ServerCard>();
		for(ServerCard c: deck) shuffle.push(c);
		return shuffle;
	}
/*
	public int hashCode(){
		return 5* color.ordinal()+value;
	}
*/

	/**
	 * Metodo di comodo per creare una ServerCard a partire da color e value
	 * @param color
	 * @param value
	 * @return
	 */
	private static ServerCard createCard(Color color, int value)
	{
		JSONObject obj = new JSONObject();
		obj.put("color",new JSONString(color.toString()));
		obj.put("value", new JSONString(""+value));
		obj.put("value_revealed",new JSONString("false"));
		obj.put("color_revealed",new JSONString("false"));
		try
		{
			return new ServerCard(obj);
		}
		catch(JSONException e)
		{
			System.err.println("Error while generating deck");
			e.printStackTrace(System.err);
			System.exit(1);
			return null;
		}
	}

	private static ServerCard[] deck = {
			createCard(Color.BLUE,1),createCard(Color.BLUE,1), createCard(Color.BLUE,1),
			createCard(Color.BLUE,2),createCard(Color.BLUE,2),createCard(Color.BLUE,3),createCard(Color.BLUE,3),
			createCard(Color.BLUE,4),createCard(Color.BLUE,4),createCard(Color.BLUE,5),
			createCard(Color.RED,1),createCard(Color.RED,1), createCard(Color.RED,1),
			createCard(Color.RED,2),createCard(Color.RED,2),createCard(Color.RED,3),createCard(Color.RED,3),
			createCard(Color.RED,4),createCard(Color.RED,4),createCard(Color.RED,5),
			createCard(Color.GREEN,1),createCard(Color.GREEN,1), createCard(Color.GREEN,1),
			createCard(Color.GREEN,2),createCard(Color.GREEN,2),createCard(Color.GREEN,3),createCard(Color.GREEN,3),
			createCard(Color.GREEN,4),createCard(Color.GREEN,4),createCard(Color.GREEN,5),
			createCard(Color.WHITE,1),createCard(Color.WHITE,1), createCard(Color.WHITE,1),
			createCard(Color.WHITE,2),createCard(Color.WHITE,2),createCard(Color.WHITE,3),createCard(Color.WHITE,3),
			createCard(Color.WHITE,4),createCard(Color.WHITE,4),createCard(Color.WHITE,5),
			createCard(Color.YELLOW,1),createCard(Color.YELLOW,1), createCard(Color.YELLOW,1),
			createCard(Color.YELLOW,2),createCard(Color.YELLOW,2),createCard(Color.YELLOW,3),createCard(Color.YELLOW,3),
			createCard(Color.YELLOW,4),createCard(Color.YELLOW,4),createCard(Color.YELLOW,5)
	};


}


