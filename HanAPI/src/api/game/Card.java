package api.game;

import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;
import java.io.StringReader;

/**
 * Classe che rappresenta una carta dal punto di vista di un giocatore.
 * @author Francesco Pandolfi, Mihail Bida
 */
public class Card extends JSONObject
{

	public Card(Color color, int value) throws JSONException
	{
		super();
		setColor(color);
		setValue(value);
		setColorRevealed(false);
		setValueRevealed(false);
	}

	public Card(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	/**
	 * Costruisce una carta a partire da un json (ottenuto dal server di gioco)
	 * @throws JSONException se il json non è conforme ad una carta Hanabi
	 **/
	public Card(Reader r) throws JSONException
	{
		super(r);


		String s = getString("color"); //get color
		if (s == null)
			throw new JSONException("Missing color!");
		else if (Color.fromString(s)==null && !s.equals(""))
			throw new JSONException("Unrecognized color! Color must be green, red, yellow, blue, white or void string \"\"");

		s = getString("value"); //get value
		if (s == null)
			throw new JSONException("Missing value");
		else
		{
			try
			{
				setValue(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("Unrecognized value! Value must be an int 0<=x<=5");
			}
		}

		s = getString("value_revealed"); //get value_revealed
		if (s == null)
			throw new JSONException("Missing value_revealed");
		else if (!(s.equals("false")||s.equals("true")))
			throw new JSONException("value_revealed must be a boolean");

		s = getString("color_revealed"); //get color_revealed
		if (s == null)
			throw new JSONException("Missing color_revealed");
		else if (!(s.equalsIgnoreCase("false")||s.equalsIgnoreCase("true")))
			throw new JSONException("color_revealed must be a boolean");


	}

	public Card clone()
	{
		try
		{
			return new Card(super.clone().toString(0));
		}
		catch(JSONException e)
		{
			//Impossibile
			return null;
		}
	}

	/**
	 * Due Card sono uguali se lo sono i loro colori e valori. Un colore o un valore sconosciuto non &egrave; mai uguale ad un altro.
	 * @return true se il parametro &egrave; una Card e se &egrave; uguale a questa carta.
	 **/
	public boolean equals(Object o){
		if(o instanceof Card){
			Card c = (Card)o;
			if (c.getColor() == null || getColor() == null || c.getValue() == 0 || getValue() == 0)
				return false;
			return c.getColor() == getColor() && c.getValue()==getValue();
		}
		return false;
	}

	public double equals(Card c, State state)
	{
		return 0; //TODO restituisce la probabilità che questa carta sia uguale a c
	}

	public Color getColor()
	{
		return Color.fromString(getString("color"));
	}

	/**
	 *@return Il numero di volte che la carta compare nel mazzo
	 */
	public int getCount(){return (getValue()==1?3:(getValue()<5?2:1));}

	public int getValue() {
		return Integer.parseInt(getString("value"));
	}

	public boolean isValueRevealed()
	{
		return Boolean.parseBoolean(getString("value_revealed"));
	}

	public boolean isColorRevealed()
	{
		return Boolean.parseBoolean(getString("color_revealed"));
	}

	public Card setColor(Color color)
	{
		if (color==null)
			set("color","");
		else
			set("color",color.toString().toLowerCase());
		return this;
	}

	public Card setValue(int v) throws JSONException
	{
		if (v<0 || v>5)
			throw new JSONException("Wrong value!");
		set("value",""+v);
		return this;
	}

	public Card setColorRevealed(boolean b)
	{
		set("color_revealed",""+b);
		return this;
	}

	public Card setValueRevealed(boolean b)
	{
		set("value_revealed",""+b);
		return this;
	}

	/**
	 * Ridefinisce il toString() di JSONData
	 * @return la rappresentazione testuale della carta
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (getColor()!=null)
			sb.append(getColor());
		else
			sb.append("   ");
		sb.append("-");
		if (getValue()>0)
			sb.append(getValue());
		else
			sb.append("   ");
		return sb.toString();
	}
}


