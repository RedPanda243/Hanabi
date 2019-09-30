package api.game;

import sjson.JSONArray;
import sjson.JSONException;

import java.io.Reader;
import java.io.StringReader;

public class Firework extends JSONArray
{
	public Firework()
	{
		super();
	}

	public Firework(String s) throws JSONException
	{
		this(new StringReader(s));
	}

	public Firework(Reader reader) throws JSONException
	{
		super(reader);
		if (size()>5)
			throw new JSONException("Firework can have max 5 cards");

		for(int i=0; i<size(); i++)
			replace(i,new Card(get(i).toString(0)));
		for (int i=1; i<size(); i++)
		{
			if (getCard(i).getValue()!=getCard(i-1).getValue()-1)
				throw new JSONException("Malformed firework, messy cards!");
		}
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

	private boolean checkColor(Color c)
	{
		for (int i=0; i<size(); i++)
		{
			if (getCard(i).getColor()!=c)
				return false;
		}
		return true;
	}

	public Color getColor()
	{
		if (size()==0)
			return null;
		return getCard(0).getColor();
	}

	private Card getCard(int i)
	{
		return (Card)get(i);
	}

	public int peak()
	{
		if (size() == 0)
			return 0;
		return getCard(size()-1).getValue();
	}
}
