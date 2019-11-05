package api.game;

import api.main.HanabiClient;
import sjson.JSONArray;
import sjson.JSONData;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;


public class Hand extends JSONArray
{

	public Hand(Card[] cards) throws JSONException
	{
		super();
		int n = Game.getInstance().getNumberOfCardsPerPlayer();
		if (cards.length<n-1 || cards.length>n)
			throw new JSONException("Hand contains wrong number of cards: "+cards.length);
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
		int n = Game.getInstance().getNumberOfCardsPerPlayer();
		//FIX (-2)
		if (size()<n-2 || size()>n)
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

	public String toString()
	{
		StringBuffer sb = new StringBuffer("{");
		for (JSONData c:this)
			sb.append(c).append(",");
		return sb.replace(sb.length()-1,sb.length(),"}").toString();
	}
}
