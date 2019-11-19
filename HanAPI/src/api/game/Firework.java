package api.game;

import sjson.JSONArray;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;

/**
 *  Classe che implementa un Firework Hanabi, rappresentato come json array di {@link Card}
 */
public class Firework extends TypedJSON<JSONArray>
{
	public Firework()
	{
		json = new JSONArray();
	}

	public Firework(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Firework(Reader reader) throws JSONException
	{
		json = new JSONArray(reader);
		if (json.size()>5)
			throw new JSONException("Un Firework può avere massimo 5 carte");

		for(int i=0; i<json.size(); i++)
			json.replace(i, new Card(json.get(i).toString(0)));

		checkFirework();
	}

	/**
	 * Aggiunge una carta al Firework
	 * @param card la carta da aggiungere
	 * @return questo Firework modificato
	 * @throws JSONException se la carta non &egrave; dello stesso colore di quelle nel Firework o se il suo valore non &egrave; {@link Firework#peak()}+1
	 */
	public Firework addCard(Card card) throws JSONException
	{
		if (peak()>0 && card.getColor()!=getCard(0).getColor())
			throw new JSONException("Il colore della carta da aggiungere deve essere "+getCard(0).getColor());

		if (card.getValue()!=peak()+1)
			throw new JSONException("Il valore della carta da aggiungere deve essere "+(peak()+1));
		json.add(card);
		return this;
	}

	/**
	 * Verifica l'integrità del Firework
	 * @throws JSONException se il Firework non inizia con una carta di valore 1, se i valori delle carte non sono crescenti o se le carte hanno colori diversi
	 */
	private void checkFirework() throws JSONException
	{
		if (json.size()>0 && getCard(0).getValue()!=1)
			throw new JSONException("Un Firework deve iniziare con una carta di valore 1");
		for (int i=1; i<json.size(); i++)
		{
			if (getCard(i).getValue()!=getCard(i-1).getValue()+1)
				throw new JSONException("Le carte di uno stesso Firework devono avere valore crescente");
			if (getCard(i).getColor()!=getCard(i-1).getColor())
				throw new JSONException("Le carte di uno stesso Firework devono avere lo stesso colore");
		}
	}

	/**
	 * @return il colore delle carte di questo Firework
	 */
	public Color getColor()
	{
		if (json.size()==0)
			return null;
		return getCard(0).getColor();
	}

	/**
	 * @param i L'indice della carta nel Firework, che corrisponde anche al suo valore -1
	 * @return La carta desiderata
	 */
	private Card getCard(int i)
	{
		return (Card)json.get(i);
	}

	/**
	 * @return il valore della carta in cima al Firework
	 */
	public int peak()
	{
		if (json.size() == 0)
			return 0;
		return getCard(json.size()-1).getValue();
	}
}
