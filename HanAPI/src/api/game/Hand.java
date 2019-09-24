package api.game;

import sjson.JSONArray;
import sjson.JSONConvertible;
import sjson.JSONException;

import java.util.Arrays;
import java.util.Iterator;

public class Hand extends JSONConvertible<JSONArray> implements Iterable<Card>
{
	protected Card[] cards;

	public Hand(JSONArray array) throws JSONException
	{
		super(array);
		cards = new Card[array.size()];
		for (int i=0; i<cards.length; i++)
		{
			cards[i] = new Card(array.optJSON(i));
		}
	}

	public Hand clone()
	{
		return (Hand)super.clone();
	}

	public Card getCard(int i)
	{
		return cards[i];
	}

	@Override
	public Iterator<Card> iterator()
	{
		return Arrays.asList(cards).iterator();
	}

	public int size()
	{
		return cards.length;
	}
}
