package game;

import api.game.Card;
import api.game.Color;
import main.HanabiServer;
import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONString;

import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;

/**
 * Classe che rappresenta una carta. Questa classe è usata solo all'interno del server, il che giustifica la presenza dei metodi
 * getter per colore e valore in quanto non accessibili dagli agenti. I metodi toString() e toJSON() invece richiedono lo stato
 * corrente a HanabiServer.instance e mostrano le informazioni di conseguenza.
 */
public final class ServerCard extends Card
{
//	private Agent owner = null; //null = Deck

	public ServerCard(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public ServerCard(Reader reader) throws JSONException
	{
		super(reader);
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
		set("color_revealed","true");
	}

	/**
	 * Rivela il valore della carta. Una volta usato questo metodo il possessore della carta ne può vedere il valore.
	 */
	public void revealValue()
	{
		set("value_revealed","true");
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



}


