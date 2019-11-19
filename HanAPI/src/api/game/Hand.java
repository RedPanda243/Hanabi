package api.game;

import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

/**
 * Rappresenta la mano di un giocatore come json array di Card
 */
public class Hand extends TypedJSON<JSONArray> implements Iterable<Card>
{

	public Hand(Card[] cards)
	{
		json = new JSONArray();
		for(Card c:cards)
			json.add(c);
	}

	public Hand(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Hand(Reader r) throws JSONException
	{
		json = new JSONArray(r);

		for (int i=0; i<json.size(); i++)
			json.replace(i,new Card(json.get(i).toString()));

		checkHand();
	}


	private void checkHand() throws JSONException
	{
		int n = Game.getInstance().getNumberOfCardsPerPlayer();
		if (json.size()<n-2 || json.size()>n)
			throw new JSONException("La mano contiene un numero di carte sbagliato");
	}

	public Hand clone()
	{
		try
		{
			return new Hand(super.clone().toString(0));
		}
		catch (JSONException e){return null;}
	}

	public Hand addCard(Card card)
	{
		json.add(card);
		return this;
	}

	public Card getCard(int i)
	{
		return (Card)json.get(i);
	}

	public Hand setCard(int i, Card c)
	{
		json.replace(i,c);
		return this;
	}

	public int size()
	{
		return json.size();
	}

	public Hand removeCard(int card)
	{
		json.remove(card);
		return this;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (JSONData c:json)
			sb.append(c).append(",");
		return sb.replace(sb.length()-1,sb.length(),"}").toString();
	}

	@Override
	public Iterator<Card> iterator()
	{
		return new Iterator<Card>()
		{
			int index = 0;
			@Override
			public boolean hasNext()
			{
				return (index<size());
			}

			@Override
			public Card next()
			{
				index++;
				return getCard(index-1);
			}
		};
	}
}
