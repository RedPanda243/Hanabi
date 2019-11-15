package api.game;

import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;
import java.io.StringReader;

/**
 * Classe che rappresenta una carta Hanabi.
 * {<br>
 *     "color" 			: colore della carta <br>
 *     "value" 			: valore della carta <br>
 *     "color_revealed" : true se il colore di questa carta &egrave; stato rivelato al possessore, false altrimenti <br>
 *     "value_revealed" : true se il valore di questa carta &egrave; stato rivelato al possessore, false altrimenti <br>
 * }
 */
@SuppressWarnings({"WeakerAccess","unused"})
public class Card extends TypedJSON<JSONObject>
{

	/**
	 * Crea una carta di colore e valore dati. I valori "color_revealed" e "value_revealed" sono impostati a false
	 * @param color valore dell'attributo "color"
	 * @param value valore dell'attributo "value"
	 * @throws JSONException se i valori passati non sono conformi a quelli di una carta Hanabi
	 */
	public Card(Color color, int value) throws JSONException
	{
		json = new JSONObject();
		setColor(color).setValue(value).setColorRevealed(false).setValueRevealed(false);
	}

	/**
	 * @see JSONObject#JSONObject(String)
	 * @param s rappresentazione testuale formattata della carta
	 * @throws JSONException se la stringa non &egrave; conforme alla rappresentazione formattata di una carta Hanabi
	 */
	public Card(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	/**
	 * @see JSONObject#JSONObject(Reader)
	 * @param r Reader da cui leggere la carta
	 * @throws JSONException se la stringa letta non &egrave; un json o se non &egrave; conforme ad una carta Hanabi
	 **/
	public Card(Reader r) throws JSONException
	{
		json = new JSONObject(r);
		checkValueRevealed();
		checkColorRevealed();
		checkColor();
		checkValue();
	}

	/**
	 * Usato nei costruttori, verifica l'integrit&agrave; del campo "color_revealed"
	 * @throws JSONException se l'attributo "color_revealed" &egrave; mancante o se non &egrave; un boolean
	 */
	private void checkColorRevealed() throws JSONException
	{
		String s = json.getString("color_revealed"); //get value_revealed
		if (s == null)
			throw new JSONException("Attributo \"color_revealed\" mancante");
		else if (!(s.equals("false")||s.equals("true")))
			throw new JSONException("L'attributo \"color_revealed\" deve avere valore boolean");
	}

	/**
	 * Usato nei costruttori, verifica l'integrit&agrave; del campo "value_revealed"
	 * @throws JSONException se l'attributo "value_revealed" &egrave; mancante o se non &egrave; un boolean
	 */
	private void checkValueRevealed() throws JSONException
	{
		String s = json.getString("value_revealed"); //get value_revealed
		if (s == null)
			throw new JSONException("Attributo \"value_revealed\" mancante");
		else if (!(s.equals("false")||s.equals("true")))
			throw new JSONException("L'attributo \"value_revealed\" deve avere valore boolean");
	}

	/**
	 * @see JSONObject#clone()
	 * @return una copia di questa Card
	 */
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

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "color"
	 * @throws JSONException se l'attributo "color" &egrave; mancante, se il suo valore non &egrave; colore previsto (o stringa vuota) o se &grave; la stringa vuota mentre "color_revealed"="true"
	 */
	private void checkColor() throws JSONException
	{
		String s = json.getString("color"); //get color
		if (s == null)
			throw new JSONException("Attributo \"color\" mancante");
		else if (Color.fromString(s)==null)
		{
			if (!s.equals(""))
				throw new JSONException("Il valore dell'attributo \"color\" deve essere \"green\", \"red\", \"yellow\", \"blue\", \"white\" o la stringa vuota\"\"");
			if (isColorRevealed())
				throw new JSONException("Il valore dell'attributo \"color\" non può essere \"\" se \"color_revealed\"=\"true\"");
		}
	}

	/**
	 * @return Il colore della carta
	 */
	public Color getColor()
	{
		return Color.fromString(json.getString("color"));
	}

	/**
	 * Consente di impostare l'attributo "color" di questa Card
	 * @param color il valore da assegnare all'attributo "color"
	 * @return questa Card modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Card setColor(Color color) throws JSONException
	{
		Color c = getColor();
		if (color==null)
			json.set("color","");
		else
			json.set("color",color.toString().toLowerCase());
		try
		{
			checkColor();
		}
		catch(JSONException e)
		{
			if (c==null)
				json.set("color","");
			else
				json.set("color",c.toString().toLowerCase());
			throw e;
		}
		return this;
	}

	/**
	 * Usato nei costruttori e nel corrispondente metodo set, verifica l'integrit&agrave; del campo "value"
	 * @throws JSONException se l'attributo "value" &egrave; mancante o se il suo valore non &egrave; un intero compreso tra 0 e 5 inclusi
	 */
	private void checkValue() throws JSONException
	{
		String s = json.getString("value"); //get value
		if (s == null)
			throw new JSONException("Attributo \"value\" mancante");
		else
		{
			try
			{
				setValue(Integer.parseInt(s));
			}
			catch (NumberFormatException e)
			{
				throw new JSONException("L'attributo \"value\" deve avere valore intero compreso tra 0 e 5 inclusi");
			}
		}
	}

	/**
	 * @return il valore della carta, 0 se sconosciuto
	 */
	public int getValue()
	{
		return Integer.parseInt(json.getString("value"));
	}

	/**
	 * Consente di impostare l'attributo "value" di questa Card
	 * @param v il valore da assegnare all'attributo "value"
	 * @return questa Card modificata
	 * @throws JSONException in caso di errore nell'impostazione
	 */
	public Card setValue(int v) throws JSONException
	{
		int v1 = getValue();
		json.set("value",""+v);
		try
		{
			checkValue();
		}
		catch(JSONException e)
		{
			json.set("value",""+v1);
			throw e;
		}
		return this;
	}

	/**
	 * @return il valore dell'attributo "value_revealed"
	 */
	public boolean isValueRevealed()
	{
		return Boolean.parseBoolean(json.getString("value_revealed"));
	}

	/**
	 * @return il valore dell'attributo "color_revealed"
	 */
	public boolean isColorRevealed()
	{
		return Boolean.parseBoolean(json.getString("color_revealed"));
	}

	/**
	 *@return Il numero di volte che la carta compare nel mazzo
	 */
	public int getCount(){return (getValue()==1?3:(getValue()<5?2:1));}


	/**
	 * Consente di impostare il valore dell'attributo "color_revealed"
	 * @param b il valore da assegnare all'attributo "color_revealed"
	 * @return questa Card modificata
	 */
	public Card setColorRevealed(boolean b)
	{
		json.set("color_revealed",""+b);
		return this;
	}

	/**
	 * Consente di impostare il valore dell'attributo "value_revealed"
	 * @param b valore da assegnare all'attributo "value_revealed"
	 * @return questa Card modificata
	 */
	public Card setValueRevealed(boolean b)
	{
		json.set("value_revealed",""+b);
		return this;
	}

	/**
	 * @return la rappresentazione testuale non formattata della carta
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


