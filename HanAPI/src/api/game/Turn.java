package api.game;

import sjson.JSONData;
import sjson.JSONException;
import sjson.JSONObject;

import java.io.Reader;

public class Turn extends TypedJSON<JSONObject>
{
	public Turn(Action a, Card d) throws JSONException
	{
		json = new JSONObject();
		setAction(a);
		setDrawn(d);
	}

	public Turn(Action a)
	{
		json = new JSONObject();
		setAction(a);
	}

	public Turn(Reader reader) throws JSONException
	{
		json = new JSONObject(reader);
		JSONData d = json.get("action");
		if (d == null)
			throw new JSONException("Attributo \"action\" mancante");
		Action a = new Action(d.toString(0));
		setAction(a);
		if (a.getType() == ActionType.PLAY || a.getType() == ActionType.DISCARD)
		{
			d = json.get("drawn");
			if (d!=null)
				setDrawn(new Card(d.toString(0)));
		}
	}

	public Action getAction()
	{
		return (Action)json.get("action");
	}

	public Card getDrawn()
	{
		JSONData c = json.get("drawn");
		if (c == null)
			return null;
		else
			return (Card)c;
	}

	public Turn setAction(Action action)
	{
		json.set("action",action);
		return this;
	}

	public Turn setDrawn(Card card) throws JSONException
	{
		ActionType type = getAction().getType();
		if (type == ActionType.HINT_COLOR || type == ActionType.HINT_VALUE)
			throw new JSONException("I suggerimenti non fanno pescare carte");
		json.set("drawn",card);
		return this;
	}

	public String toString()
	{
		Card c = getDrawn();
		return getAction().toString()+(c==null?"":"\nIl giocatore pesca "+c.toString());
	}
}
