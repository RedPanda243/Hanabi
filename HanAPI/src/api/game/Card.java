package api.game;

import sjson.JSONConvertible;
import sjson.JSONException;
import sjson.JSONObject;

/**
 * Classe che rappresenta una carta dal punto di vista di un giocatore.
 * @author Francesco Pandolfi, Mihail Bida
 */
public class Card extends JSONConvertible<JSONObject>
{
	private Color color;
	private int value;
	protected boolean valueRevealed;
	protected boolean colorRevealed;

	/**
	 * Costruisce una carta a partire da un json (ottenuto dal server di gioco)
	 * @throws JSONException se il json non è conforme ad una carta Hanabi
	 **/
	public Card(JSONObject object) throws JSONException
	{
		super(object);

		String s = object.optString("color");
		if (s == null)
			throw new JSONException("Missing color!");
		else if (Color.fromString(s)==null && !s.equals(""))
			throw new JSONException("Unrecognized color! Color must be green, red, yellow, blue, white or void string \"\"");

		color = Color.fromString(s);

		s = object.optString("value");
		if (s == null)
			throw new JSONException("Missing value");
		else
		{
			try
			{
				int v = Integer.parseInt(s);
				if (v<0 || v>5)
					throw new NumberFormatException();
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unrecognized value! Value must be an int 0<=x<=5");
			}
		}

		value = Integer.parseInt(s);

		s = object.optString("value_revealed");
		if (s == null)
			throw new JSONException("Missing value_revealed");
		else if (!(s.equalsIgnoreCase("false")||s.equalsIgnoreCase("true")))
			throw new JSONException("value_revealed must be a boolean");

		valueRevealed = Boolean.parseBoolean(s);

		s = object.optString("color_revealed");
		if (s == null)
			throw new JSONException("Missing color_revealed");
		else if (!(s.equalsIgnoreCase("false")||s.equalsIgnoreCase("true")))
			throw new JSONException("color_revealed must be a boolean");

		colorRevealed = Boolean.parseBoolean(s);
	}

	public Card clone()
	{
		return (Card)super.clone();
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

	/**
	 * Ridefinisce il toString() di JSONData
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
}


