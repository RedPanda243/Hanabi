package api.game;

import api.main.HanabiClient;
import sjson.JSONArray;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;


public class Hand extends JSONArray
{

	public Hand(Card[] cards) throws JSONException
	{
		super();
		int n = HanabiClient.getInstance().getGame().getNumberOfCardsPerPlayer();
		if (size()<n-1 || size()>n)
			throw new JSONException("Hand contains wrong number of cards");
		for(Card c:cards)
			add(c);
	}

	public Hand(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Hand(Reader r) throws JSONException
	{
		super(r);
		int n = HanabiClient.getInstance().getGame().getNumberOfCardsPerPlayer();
		if (size()<n-1 || size()>n)
			throw new JSONException("Hand contains wrong number of cards");
		for (int i=0; i<this.size(); i++)
			this.replace(i,new Card(this.get(i).toString()));
	}

	public Hand clone()
	{
		try
		{
			return new Hand(super.clone().toString(0));
		}
		catch (JSONException e){return null;}
	}

	public Card getCard(int i)
	{
		return (Card)get(i);
	}

	public Hand setCard(int i, Card c)
	{
		replace(i,c);
		return this;
	}
}
