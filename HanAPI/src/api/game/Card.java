package api.game;

import sjson.JSONException;
import sjson.JSONObject;
import sjson.JSONObjectConvertible;

/**
 * Classe che rappresenta una carta dal punto di vista di un giocatore.
 * @author Francesco Pandolfi, Mihail Bida
 */
public class Card implements Cloneable, JSONObjectConvertible
{
	private Color color;
	private int value;
	private boolean valueRevealed;
	private boolean colorRevealed;
	private JSONObject obj;

	/**
	 * Costruisce una carta a partire da un json (ottenuto dal server di gioco)
	 * @throws JSONException se il json non è conforme ad una carta Hanabi
	 **/
	public Card(JSONObject object) throws JSONException
	{
		String c = object.optString("color");
		String v = object.optString("value");
		String cr = object.optString("color_revealed");
		String vr = object.optString("value_revealed");

		if (c == null || v == null || cr == null || vr == null)
			throw new JSONException("Missing field! Requested: color, value, color_revealed, value_revealed");

		if (c.equals(""))
			color = null;
		else
		{
			color = Color.fromString(c);
			if (color == null)
				throw new JSONException("Color must be green, red, yellow, blue or white");
		}

		if (v.equals(""))
			value = 0;
		else {
			try {
				value = Integer.parseInt(v);
				if (value < 1 || value > 5)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				throw new JSONException("Value must be an int 1<=x<=5");
			}
		}

		if (cr.equals("true"))
			colorRevealed = true;
		else if (cr.equals("false"))
			colorRevealed = false;
		else throw new JSONException("Color_revealed must be a boolean");

		if (vr.equals("true"))
			valueRevealed = true;
		else if (vr.equals("false"))
			valueRevealed = false;
		else throw new JSONException("Value_revealed must be a boolean");

		this.obj = object.copy();
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
			c.obj = this.obj.copy();
		}
		catch(CloneNotSupportedException e)
		{
			c = null;
		}
		return c;
	}

	/**
	 * Due Card sono uguali se lo sono i loro colori e valori. Un colore o un valore sconosciuto non &egrave; mai uguale ad un altro.
	 * @return true se il parametro &egrave; una Card e se &egrave; uguale a questa carta.
	 **/
	public boolean equals(Object o){
		if(o instanceof Card){
			Card c = (Card)o;
			if (c.color == null || color == null || c.value == 0 || value == 0)
				return false;
			return c.color == color && c.value==value;
		}
		return false;
	}

	public double equals(Card c, State state)
	{
		return 0; //TODO restituisce la probabilità che questa carta sia uguale a c
	}

	public Color getColor() {
		return color;
	}

	/**
	 *@return Il numero di volte che la carta compare nel mazzo
	 */
	public int getCount(){return (value==1?3:(value<5?2:1));}

	public int getValue() {
		return value;
	}

	public boolean isValueRevealed() {
		return valueRevealed;
	}

	public boolean isColorRevealed() {
		return colorRevealed;
	}

	@Override
	public JSONObject toJSON()
	{
		return obj.copy();
	}

	/**
	 * @return la rappresentazione testuale della carta
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (color!=null)
			sb.append(color);
		else
			sb.append("   ");
		sb.append("-");
		if (value>0)
			sb.append(value);
		else
			sb.append("   ");
		return sb.toString();
	}


/*
	public int hashCode(){
		return 5* color.ordinal()+value;
	}
*/

}


