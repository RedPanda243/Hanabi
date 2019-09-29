package api.game;

import sjson.JSONArray;
import sjson.JSONException;

import java.io.Reader;

public class Firework extends JSONArray
{
	public Firework(Reader reader) throws JSONException
	{
		super(reader);
		if (size()>5)
			throw new JSONException("Firework can have max 5 cards");

		for(int i=0; i<size(); i++)
			replace(i,new Card(get(i).toString(0)));
		if (size()>1 && !checkColor(getCard(0).getColor()))
			throw new JSONException("Cards in firework must have same color");

	}

	public Firework addCard(Card card) throws JSONException
	{
		if (checkColor(card.getColor()))
		{
			if (card.getValue()==peak()+1)
				add(card);
			else
				throw new JSONException("Expecting a "+(peak()+1)+" card");
		}
		else
			throw new JSONException("Card color must be "+getCard(0).getColor());
		return this;
	}

	public boolean checkColor(Color c)
	{
		for (int i=0; i<size(); i++)
		{
			if (getCard(i).getColor()!=c)
				return false;
		}
		return true;
	}

	private Card getCard(int i)
	{
		return (Card)get(i);
	}

	public int peak()
	{
		return getCard(size()-1).getValue();
	}
}
